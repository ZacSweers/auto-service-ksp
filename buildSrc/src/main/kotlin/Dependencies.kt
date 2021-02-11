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

  const val guava = "com.google.guava:guava:30.0-jre"

  object AutoService {
    private const val version = "1.0-rc7"
    const val annotations = "com.google.auto.service:auto-service-annotations:$version"
    const val processor = "com.google.auto.service:auto-service:$version"
  }

  object Kotlin {
    const val version = "1.4.30"
    const val dokkaVersion = "1.4.20"
    const val jvmTarget = "1.8"
    val defaultFreeCompilerArgs = listOf("-Xjsr305=strict", "-progressive")
    const val compilerEmbeddable = "org.jetbrains.kotlin:kotlin-compiler-embeddable:$version"

    object Ksp {
      private const val version = "1.4.30-1.0.0-alpha02"
      const val api = "com.google.devtools.ksp:symbol-processing-api:$version"
      const val ksp = "com.google.devtools.ksp:symbol-processing:$version"
    }
  }

  object KotlinPoet {
    private const val version = "1.7.2"
    const val kotlinPoet = "com.squareup:kotlinpoet:$version"
  }

  object Testing {
    const val compileTesting = "com.github.tschuchortdev:kotlin-compile-testing:1.3.4"
    const val kspCompileTesting = "com.github.tschuchortdev:kotlin-compile-testing-ksp:1.3.4"
    const val junit = "junit:junit:4.13.1"
    const val truth = "com.google.truth:truth:1.1"
  }
}
