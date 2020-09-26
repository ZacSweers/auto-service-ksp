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

  const val guava = "com.google.guava:guava:29.0-jre"

  object AutoService {
    private const val version = "1.0-rc7"
    const val annotations = "com.google.auto.service:auto-service-annotations:$version"
    const val processor = "com.google.auto.service:auto-service:$version"
  }

  object Kotlin {
    const val version = "1.4.10"
    const val dokkaVersion = "1.4.10"
    const val jvmTarget = "1.8"
    val defaultFreeCompilerArgs = listOf("-Xjsr305=strict", "-progressive")

    object Ksp {
      const val version = "1.4.10-dev-experimental-20200924"
      const val api = "com.google.devtools.ksp:symbol-processing-api:$version"
      const val ksp = "com.google.devtools.ksp:symbol-processing:$version"
    }
  }

  object KotlinPoet {
    private const val version = "1.6.0"
    const val kotlinPoet = "com.squareup:kotlinpoet:$version"
  }

  object Testing {
    const val kspCompileTesting = "com.github.tschuchortdev:kotlin-compile-testing-ksp:1.2.11"
    const val junit = "junit:junit:4.12"
    const val truth = "com.google.truth:truth:1.0"
  }
}
