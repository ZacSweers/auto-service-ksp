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
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspIncremental
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import java.io.File
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class AutoServiceSymbolProcessorTest(private val incremental: Boolean) {

  companion object {
    @JvmStatic
    @Parameterized.Parameters(name = "incremental={0}")
    fun data(): Collection<Array<Any>> {
      return listOf(arrayOf(true), arrayOf(false))
    }
  }

  @Test
  fun smokeTest() {
    val source =
        SourceFile.kotlin(
            "CustomCallable.kt",
            """
      package test
      import com.google.auto.service.AutoService
      import java.util.concurrent.Callable

      @AutoService(Callable::class)
      class CustomCallable : Callable<String> {
        override fun call(): String = "Hello world!"
      }
    """)

    val compilation =
        KotlinCompilation().apply {
          sources = listOf(source)
          inheritClassPath = true
          symbolProcessorProviders = listOf(AutoServiceSymbolProcessor.Provider())
          kspIncremental = incremental
        }
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
    """)

    val compilation =
        KotlinCompilation().apply {
          sources = listOf(source)
          inheritClassPath = true
          symbolProcessorProviders = listOf(AutoServiceSymbolProcessor.Provider())
          kspIncremental = incremental
        }
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
        SourceFile.kotlin(
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
                .trimIndent())

    val compilation =
        KotlinCompilation().apply {
          sources = listOf(source)
          inheritClassPath = true
          symbolProcessorProviders = listOf(AutoServiceSymbolProcessor.Provider())
          kspIncremental = incremental
        }
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
}
