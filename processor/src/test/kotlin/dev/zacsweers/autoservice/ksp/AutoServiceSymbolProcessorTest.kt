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
@file:OptIn(ExperimentalCompilerApi::class)

package dev.zacsweers.autoservice.ksp

import com.google.common.truth.Truth.assertThat
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.writeTo
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.configureKsp
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import com.tschuchort.compiletesting.kspIncremental
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.tschuchort.compiletesting.useKsp2
import java.io.File
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@OptIn(ExperimentalCompilerApi::class)
@RunWith(Parameterized::class)
class AutoServiceSymbolProcessorTest(
    private val incremental: Boolean,
    private val useKSP2: Boolean,
) {

  companion object {
    @JvmStatic
    @Parameters(name = "incremental={0},useKSP2={1}")
    fun data(): Collection<Array<Any>> {
      return listOf(arrayOf(true, false), arrayOf(false, true))
    }
  }

  private fun newCompilation(): KotlinCompilation {
    return KotlinCompilation().apply {
      if (useKSP2) {
        useKsp2()
      } else {
        languageVersion = "1.9"
      }
      inheritClassPath = true
      configureKsp(useKSP2) {
        symbolProcessorProviders += AutoServiceSymbolProcessor.Provider()
        incremental = this@AutoServiceSymbolProcessorTest.incremental
      }
    }
  }

  @Test
  fun smokeTest() {
    val source =
        kotlin(
            "CustomCallable.kt",
            """
      package test
      import com.google.auto.service.AutoService
      import java.util.concurrent.Callable

      @AutoService(Callable::class)
      class CustomCallable : Callable<String> {
        override fun call(): String = "Hello world!"
      }
    """,
        )

    val compilation = newCompilation().apply { sources = listOf(source) }
    val result = compilation.compile()
    assertThat(result.exitCode).isEqualTo(ExitCode.OK)
    val generatedSourcesDir = compilation.kspSourcesDir
    val generatedFile =
        File(generatedSourcesDir, "resources/META-INF/services/java.util.concurrent.Callable")
    assertThat(generatedFile.exists()).isTrue()
    assertThat(generatedFile.readText()).isEqualTo("test.CustomCallable\n")
  }

  @Test
  fun smokeTestForJava() {
    val source =
        SourceFile.java(
            "CustomCallable.java",
            """
      package test;
      import com.google.auto.service.AutoService;
      import java.util.concurrent.Callable;

      @AutoService(Callable.class)
      public class CustomCallable implements Callable<String> {
        @Override public String call() { return "Hello world!"; }
      }
    """,
        )

    val compilation = newCompilation().apply { sources = listOf(source) }
    val result = compilation.compile()
    assertThat(result.exitCode).isEqualTo(ExitCode.OK)
    val generatedSourcesDir = compilation.kspSourcesDir
    val generatedFile =
        File(generatedSourcesDir, "resources/META-INF/services/java.util.concurrent.Callable")
    assertThat(generatedFile.exists()).isTrue()
    assertThat(generatedFile.readText()).isEqualTo("test.CustomCallable\n")
  }

  @Test
  fun errorOnNoServiceInterfacesProvided() {
    val source =
        kotlin(
            "CustomCallable.kt",
            """
        package test
        import com.google.auto.service.AutoService
        import java.util.concurrent.Callable

        @AutoService
        class CustomCallable : Callable<String> {
            override fun call(): String = "Hello world!"
        }
      """
                .trimIndent(),
        )

    val compilation = newCompilation().apply { sources = listOf(source) }
    val result = compilation.compile()
    assertThat(result.exitCode).isEqualTo(ExitCode.COMPILATION_ERROR)
    assertThat(result.messages)
        .contains(
            """
                No service interfaces specified by @AutoService annotation!
                You can provide them in annotation parameters: @AutoService(YourService::class)
            """
                .trimIndent())
  }

  class TacoGenerator(private val codeGenerator: CodeGenerator) : SymbolProcessor {
    class Provider : SymbolProcessorProvider {
      override fun create(environment: SymbolProcessorEnvironment) =
          TacoGenerator(environment.codeGenerator)
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
      resolver
          .getSymbolsWithAnnotation("test.GenerateInterface")
          .filterIsInstance<KSClassDeclaration>()
          .forEach { annotated ->
            FileSpec.get(
                    annotated.packageName.asString(),
                    TypeSpec.interfaceBuilder("I${annotated.simpleName.asString()}")
                        .addOriginatingKSFile(annotated.containingFile!!)
                        .build(),
                )
                .writeTo(codeGenerator, aggregating = false)
          }
      return emptyList()
    }
  }

  @Ignore("https://github.com/google/ksp/issues/1916")
  @Test
  fun deferredTypes() {
    // This tests handling error types in multiple rounds to ensure we defer correctly when
    // encountering error types. This works by having a simple processor that generates an
    // interface with a given name from an annotated class, then an AutoService-annotated
    // class that implements that interface.
    val generateAnnotation =
        kotlin(
            "GenerateInterface.kt",
            """
      package test
      annotation class GenerateInterface
    """,
        )
    val source =
        kotlin(
            "CustomCallable.kt",
            """
      package test
      import com.google.auto.service.AutoService
      import java.util.concurrent.Callable
      import test.IExample

      @GenerateInterface
      class Example

      @AutoService(IExample::class, Callable::class)
      class ExampleImpl : IExample, Callable<String> {
        override fun call(): String = "Hello world!"
      }
    """,
        )

    val compilation =
        KotlinCompilation().apply {
          sources = listOf(generateAnnotation, source)
          inheritClassPath = true
          symbolProcessorProviders =
              listOf(AutoServiceSymbolProcessor.Provider(), TacoGenerator.Provider())
          kspIncremental = incremental
        }
    val result = compilation.compile()
    assertThat(result.exitCode).isEqualTo(ExitCode.OK)
    val generatedSourcesDir = compilation.kspSourcesDir
    val generatedFile = File(generatedSourcesDir, "resources/META-INF/services/test.IExample")
    assertThat(generatedFile.exists()).isTrue()
    assertThat(generatedFile.readText()).isEqualTo("test.ExampleImpl\n")
  }
}
