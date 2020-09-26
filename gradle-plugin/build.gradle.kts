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
  `java-gradle-plugin`
  kotlin("jvm")
  kotlin("kapt")
  id("com.vanniktech.maven.publish")
}

gradlePlugin {
  plugins {
    plugins.create("auto-service-ksp") {
      id = "dev.zacsweers.autoservice"
      implementationClass = "dev.zacsweers.autoservice.gradle.AutoServiceKspGradlePlugin"
    }
  }
}

dependencies {
  compileOnly(gradleApi())
}
