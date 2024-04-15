/*
 * Copyright (c) 2020 Zac Sweers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ksp)
  alias(libs.plugins.mavenPublish)
}

tasks.test {
  // KSP2 needs more memory to run
  minHeapSize = "1024m"
  maxHeapSize = "1024m"
}

dependencies {
  ksp(libs.autoService.ksp)
  compileOnly(libs.ksp.api)

  implementation(libs.autoService.annotations)
  implementation(libs.kotlinpoet)
  implementation(libs.guava)

  testImplementation(libs.junit)
  testImplementation(libs.kct.core)
  testImplementation(libs.kct.ksp)
  testImplementation(libs.ksp.api)
  testImplementation(libs.truth)
}
