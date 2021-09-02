package dev.zacsweers.autoservice.ksp

import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspIncremental
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class AutoServiceSymbolProcessorTest(private val incremental: Boolean) {

  companion object {
    @JvmStatic
    @Parameterized.Parameters(name = "incremental={0}")
    fun data() : Collection<Array<Any>> {
      return listOf(
        arrayOf(true),
        arrayOf(false)
      )
    }
  }

  @Test
  fun smokeTest() {
    val source = SourceFile.kotlin("CustomCallable.kt", """
      package test
      import com.google.auto.service.AutoService
      import java.util.concurrent.Callable
      
      @AutoService(Callable::class)
      class CustomCallable : Callable<String> {
        override fun call(): String = "Hello world!"
      }
    """)

    val compilation = KotlinCompilation().apply {
      sources = listOf(source)
      inheritClassPath = true
      symbolProcessorProviders = listOf(AutoServiceSymbolProcessorProvider())
      kspIncremental = incremental
    }
    val result = compilation.compile()
    assertThat(result.exitCode).isEqualTo(ExitCode.OK)
    val generatedSourcesDir = compilation.kspSourcesDir
    val generatedFile = File(generatedSourcesDir,
      "resources/META-INF/services/java.util.concurrent.Callable")
    assertThat(generatedFile.exists()).isTrue()
    assertThat(generatedFile.readText()).isEqualTo("test.CustomCallable\n")
  }

  @Test
  fun smokeTestForJava() {
    val source = SourceFile.java("CustomCallable.java", """
      package test;
      import com.google.auto.service.AutoService;
      import java.util.concurrent.Callable;

      @AutoService(Callable.class)
      public class CustomCallable implements Callable<String> {
        @Override public String call() { return "Hello world!"; }
      }
    """)

    val compilation = KotlinCompilation().apply {
      sources = listOf(source)
      inheritClassPath = true
      symbolProcessorProviders = listOf(AutoServiceSymbolProcessorProvider())
      kspIncremental = incremental
    }
    val result = compilation.compile()
    assertThat(result.exitCode).isEqualTo(ExitCode.OK)
    val generatedSourcesDir = compilation.kspSourcesDir
    val generatedFile = File(generatedSourcesDir,
      "resources/META-INF/services/java.util.concurrent.Callable")
    assertThat(generatedFile.exists()).isTrue()
    assertThat(generatedFile.readText()).isEqualTo("test.CustomCallable\n")
  }
}