/*
 * Copyright (C) 2023 Zac Sweers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.zacsweers.autoservice.ksp

import com.google.auto.service.AutoService
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.Sets
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.isLocal
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.writeTo
import java.io.IOException
import java.util.SortedSet

public class AutoServiceSymbolProcessor(environment: SymbolProcessorEnvironment) : SymbolProcessor {

  private companion object {
    const val AUTO_SERVICE_NAME = "com.google.auto.service.AutoService"
  }

  private val codeGenerator = environment.codeGenerator
  private val logger = environment.logger

  /**
   * Maps the class names of service provider interfaces to the class names of the concrete classes
   * which implement them plus their KSFile (for incremental processing).
   *
   * For example,
   * ```
   * "com.google.apphosting.LocalRpcService" -> "com.google.apphosting.datastore.LocalDatastoreService"
   * ```
   */
  private val providers: Multimap<String, Spec> = HashMultimap.create()

  private val verify = environment.options["autoserviceKsp.verify"]?.toBoolean() == true
  private val verbose = environment.options["autoserviceKsp.verbose"]?.toBoolean() == true

  /**
   * - For each class annotated with [AutoService
   *     - Verify the [AutoService] interface value is correct
   *     - Categorize the class by its service interface
   * - For each [AutoService] interface
   *     - Create a file named `META-INF/services/<interface>`
   *     - For each [AutoService] annotated class for this interface
   *         - Create an entry in the file
   */
  override fun process(resolver: Resolver): List<KSAnnotated> {
    val autoServiceType =
        resolver
            .getClassDeclarationByName(resolver.getKSNameFromString(AUTO_SERVICE_NAME))
            ?.asType(emptyList())
            ?: run {
              val message = "@AutoService type not found on the classpath, skipping processing."
              if (verbose) {
                logger.warn(message)
              } else {
                logger.info(message)
              }
              return emptyList()
            }

    val deferred = mutableListOf<KSAnnotated>()

    resolver
        .getSymbolsWithAnnotation(AUTO_SERVICE_NAME)
        .filterIsInstance<KSClassDeclaration>()
        .forEach { providerImplementer ->
          val annotation =
              providerImplementer.annotations.find {
                it.annotationType.resolve() == autoServiceType
              }
                  ?: run {
                    logger.error("@AutoService annotation not found", providerImplementer)
                    return@forEach
                  }

          val argumentValue =
              annotation.arguments.find { it.name?.getShortName() == "value" }!!.value

          @Suppress("UNCHECKED_CAST")
          val providerInterfaces =
              try {
                argumentValue as? List<KSType> ?: listOf(argumentValue as KSType)
              } catch (exception: ClassCastException) {
                logger.error("No 'value' member value found!", annotation)
                return@forEach
              }

          if (providerInterfaces.isEmpty()) {
            val message =
                """
                No service interfaces specified by @AutoService annotation!
                You can provide them in annotation parameters: @AutoService(YourService::class)
              """
                    .trimIndent()

            logger.error(message, annotation)
          }

          for (providerType in providerInterfaces) {
            if (providerType.isError) {
              deferred += providerImplementer
              return@forEach
            }

            val providerDecl = providerType.asClass()

            if (providerImplementer.classKind == ClassKind.OBJECT &&
                providerDecl.classKind != ClassKind.INTERFACE) {
              val message =
                  "Kotlin objects are only supported by delegation, ${providerDecl.qualifiedName?.asString()} must be an interface"
              logger.error(message, providerImplementer)
              return@forEach
            }

            when (checkImplementer(providerImplementer, providerType)) {
              ValidationResult.VALID -> {
                val binaryName = providerImplementer.toBinaryName()
                val ksFile = providerImplementer.containingFile!!

                providers.put(
                    providerDecl.toBinaryName(),
                    when (providerImplementer.classKind) {
                      ClassKind.OBJECT -> {
                        val providerSupertype =
                            providerImplementer.superTypes
                                .firstOrNull { it.resolve().asClass() == providerDecl }
                                ?.toTypeName(
                                    providerImplementer.typeParameters.toTypeParameterResolver())

                        Spec.WithProxy(
                            binaryName,
                            ksFile,
                            providerSupertype ?: providerType.toTypeName(),
                            providerImplementer.toClassName(),
                            "${binaryName.replace('$', '_')}_ServiceLoaderProxy")
                      }
                      else -> Spec(binaryName, ksFile)
                    })
              }
              ValidationResult.INVALID -> {
                val message =
                    "ServiceProviders must implement their service provider interface. " +
                        providerImplementer.qualifiedName?.asString() +
                        " does not implement " +
                        providerDecl.qualifiedName?.asString()
                logger.error(message, providerImplementer)
              }
              ValidationResult.DEFERRED -> {
                deferred += providerImplementer
              }
            }
          }
        }
    generateAndClearConfigFiles()
    return deferred
  }

  private tailrec fun KSType.asClass(): KSClassDeclaration =
      when (val decl = declaration) {
        !is KSTypeAlias -> decl.closestClassDeclaration()!!
        else -> decl.type.resolve().asClass()
      }

  private fun checkImplementer(
      providerImplementer: KSClassDeclaration,
      providerType: KSType,
  ): ValidationResult {
    if (!verify) {
      return ValidationResult.VALID
    }
    for (superType in providerImplementer.getAllSuperTypes()) {
      if (superType.isAssignableFrom(providerType)) {
        return ValidationResult.VALID
      } else if (superType.isError) {
        return ValidationResult.DEFERRED
      }
    }
    return ValidationResult.INVALID
  }

  private fun generateAndClearConfigFiles() {
    for (providerInterface in providers.keySet()) {
      val foundImplementers = providers[providerInterface]
      val resourceFile = "META-INF/services/$providerInterface"
      log("Working on resource file: $resourceFile")
      try {
        val allServices: SortedSet<String> = Sets.newTreeSet()
        val newServices: Set<String> =
            HashSet(
                foundImplementers.map { (it as? Spec.WithProxy)?.proxyName ?: it.binaryName })
        allServices.addAll(newServices)
        log("New service file contents: $allServices")
        val ksFiles = foundImplementers.map { it.ksFile }
        log("Originating files: ${ksFiles.map(KSFile::fileName)}")
        val dependencies = Dependencies(true, *ksFiles.toTypedArray())
        codeGenerator.createNewFile(dependencies, "", resourceFile, "").bufferedWriter().use {
            writer ->
          for (service in allServices) {
            writer.write(service)
            writer.newLine()
          }
        }
        log("Wrote to: $resourceFile")
      } catch (e: IOException) {
        logger.error("Unable to create $resourceFile, $e")
      }

      for (spec in foundImplementers.filterIsInstance<Spec.WithProxy>()) {
        val className = ClassName.bestGuess(spec.proxyName)

        try {
          FileSpec.get(
                  className.packageName,
                  TypeSpec.classBuilder(className.simpleName)
                      .addModifiers(KModifier.INTERNAL)
                      .addSuperinterface(spec.providerType, CodeBlock.of("%T", spec.providerImpl))
                      .build())
              .writeTo(codeGenerator, aggregating = false, originatingKSFiles = listOf(spec.ksFile))

          log("Wrote object provider: $className")
        } catch (e: IOException) {
          logger.error("Unable to create object provider for: $className, $e")
        }
      }
    }
    providers.clear()
  }

  private fun log(message: String) {
    if (verbose) {
      logger.logging(message)
    }
  }

  /**
   * Returns the binary name of a reference type. For example, {@code com.google.Foo$Bar}, instead
   * of {@code com.google.Foo.Bar}.
   */
  private fun KSClassDeclaration.toBinaryName(): String {
    return toClassName().reflectionName()
  }

  private fun KSClassDeclaration.toClassName(): ClassName {
    require(!isLocal()) { "Local/anonymous classes are not supported!" }
    val pkgName = packageName.asString()
    val typesString = qualifiedName!!.asString().removePrefix("$pkgName.")

    val simpleNames = typesString.split(".")
    return ClassName(pkgName, simpleNames)
  }

  private enum class ValidationResult {
    VALID,
    INVALID,
    DEFERRED,
  }

  private open class Spec(
      val binaryName: String,
      val ksFile: KSFile,
  ) {
    class WithProxy(
        binaryName: String,
        ksFile: KSFile,
        val providerType: TypeName,
        val providerImpl: ClassName,
        val proxyName: String,
    ) : Spec(binaryName, ksFile)
  }

  @AutoService(SymbolProcessorProvider::class)
  public class Provider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        AutoServiceSymbolProcessor(environment)
  }
}
