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
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import java.io.IOException
import java.util.SortedSet

@AutoService(SymbolProcessorProvider::class)
public class AutoServiceSymbolProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
    AutoServiceSymbolProcessor(environment)
}

private class AutoServiceSymbolProcessor(
  environment: SymbolProcessorEnvironment
) : SymbolProcessor {

  private companion object {
    val AUTO_SERVICE_NAME = AutoService::class.qualifiedName!!
  }

  private val codeGenerator = environment.codeGenerator
  private val logger = environment.logger

  /**
   * Maps the class names of service provider interfaces to the
   * class names of the concrete classes which implement them plus their KSFile (for incremental
   * processing).
   *
   * For example,
   * ```
   * "com.google.apphosting.LocalRpcService" -> "com.google.apphosting.datastore.LocalDatastoreService"
   * ```
   */
  private val providers: Multimap<String, Pair<String, KSFile>> = HashMultimap.create()

  private val verify = environment.options["autoserviceKsp.verify"]?.toBoolean() == true
  private val verbose = environment.options["autoserviceKsp.verbose"]?.toBoolean() == true

  /**
   * - For each class annotated with [AutoService
   *   - Verify the [AutoService] interface value is correct
   *   - Categorize the class by its service interface
   * - For each [AutoService] interface
   *   - Create a file named `META-INF/services/<interface>`
   *   - For each [AutoService] annotated class for this interface
   *     - Create an entry in the file
   */
  override fun process(resolver: Resolver): List<KSAnnotated> {
    val autoServiceType = resolver.getClassDeclarationByName(
      resolver.getKSNameFromString(AUTO_SERVICE_NAME))
      ?.asType(emptyList())
      ?: run {
        logger.error("@AutoService type not found on the classpath.")
        return emptyList()
      }

    resolver.getSymbolsWithAnnotation(AUTO_SERVICE_NAME)
      .asSequence()
      .filterIsInstance<KSClassDeclaration>()
      .forEach { providerImplementer ->
        val annotation = providerImplementer.annotations.find { it.annotationType.resolve() == autoServiceType }
          ?: run {
            logger.error("@AutoService annotation not found", providerImplementer)
            return@forEach
          }

        val argumentValue = annotation.arguments
          .find { it.name?.getShortName() == "value" }!!.value

        @Suppress("UNCHECKED_CAST")
        val providerInterfaces = try {
          argumentValue as? List<KSType>
            ?: listOf(argumentValue as KSType)
        } catch (exception: ClassCastException) {
          logger.error("No 'value' member value found!", annotation)
          return@forEach
        }

        if (providerInterfaces.isEmpty()) {
          logger.error("No service interfaces provided for element!", providerImplementer)
        }

        for (providerType in providerInterfaces) {
          val providerDecl = providerType.declaration.closestClassDeclaration()!!
          if (checkImplementer(providerImplementer, providerType)) {
            providers.put(providerDecl.toBinaryName(), providerImplementer.toBinaryName() to providerImplementer.containingFile!!)
          } else {
            val message = "ServiceProviders must implement their service provider interface. " +
              providerImplementer.qualifiedName +
              " does not implement " +
              providerDecl.qualifiedName
            logger.error(message, providerImplementer)
            return@forEach
          }
        }
      }
    generateAndClearConfigFiles()
    return emptyList()
  }

  private fun checkImplementer(
    providerImplementer: KSClassDeclaration,
    providerType: KSType
  ): Boolean {
    if (!verify) {
      return true
    }
    return providerImplementer.getAllSuperTypes().any { it.isAssignableFrom(providerType) }
  }

  private fun generateAndClearConfigFiles() {
    for (providerInterface in providers.keySet()) {
      val resourceFile = "META-INF/services/$providerInterface"
      log("Working on resource file: $resourceFile")
      try {
        val allServices: SortedSet<String> = Sets.newTreeSet()
        val foundImplementers = providers[providerInterface]
        val newServices: Set<String> = HashSet(foundImplementers.map { it.first })
        allServices.addAll(newServices)
        log("New service file contents: $allServices")
        val ksFiles = foundImplementers.map { it.second }
        log("Originating files: ${ksFiles.map(KSFile::fileName)}")
        val dependencies = Dependencies(true, *ksFiles.toTypedArray())
        codeGenerator.createNewFile(
          dependencies,
          "",
          resourceFile,
          ""
        ).bufferedWriter().use { writer ->
          for (service in allServices) {
            writer.write(service)
            writer.newLine()
          }
        }
        log("Wrote to: $resourceFile")
      } catch (e: IOException) {
        logger.error("Unable to create $resourceFile, $e")
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
   * Returns the binary name of a reference type. For example,
   * {@code com.google.Foo$Bar}, instead of {@code com.google.Foo.Bar}.
   */
  private fun KSClassDeclaration.toBinaryName(): String {
    return toClassName().reflectionName()
  }

  private fun KSClassDeclaration.toClassName(): ClassName {
    require(!isLocal()) {
      "Local/anonymous classes are not supported!"
    }
    val pkgName = packageName.asString()
    val typesString = qualifiedName!!.asString().removePrefix("$pkgName.")

    val simpleNames = typesString
      .split(".")
    return ClassName(pkgName, simpleNames)
  }
}