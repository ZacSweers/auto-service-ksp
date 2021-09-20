/*
 * Copyright (C) 2020 Zac Sweers
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

object Dependencies {

  const val guava = "com.google.guava:guava:30.1.1-jre"

  object AutoService {
    private const val version = "1.0"
    const val annotations = "com.google.auto.service:auto-service-annotations:$version"
    const val compiler = "com.google.auto.service:auto-service:$version"
    const val ksp = "dev.zacsweers.autoservice:auto-service-ksp:1.0.0"
  }

  object Kotlin {
    const val version = "1.5.31"
    const val dokkaVersion = "1.5.30"
    const val jvmTarget = "1.8"
    val defaultFreeCompilerArgs = listOf("-Xjsr305=strict", "-progressive")
    const val compilerEmbeddable = "org.jetbrains.kotlin:kotlin-compiler-embeddable:$version"

    object Ksp {
      const val version = "1.5.30-1.0.0"
      const val api = "com.google.devtools.ksp:symbol-processing-api:$version"
      const val ksp = "com.google.devtools.ksp:symbol-processing:$version"
    }
  }

  object KotlinPoet {
    private const val version = "1.10.0"
    const val kotlinPoet = "com.squareup:kotlinpoet:$version"
  }

  object Testing {
    private const val kctVersion = "1.4.4"
    const val compileTesting = "com.github.tschuchortdev:kotlin-compile-testing:$kctVersion"
    const val kspCompileTesting = "com.github.tschuchortdev:kotlin-compile-testing-ksp:$kctVersion"
    const val junit = "junit:junit:4.13.2"
    const val truth = "com.google.truth:truth:1.1.2"
  }
}
