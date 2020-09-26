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
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

sourceSets {
  main.configure {
    java.srcDir(project.file("$buildDir/generated/sources/version-templates/kotlin/main"))
  }
}

val version = providers.gradleProperty("VERSION_NAME")
  .forUseAtConfigurationTime()
  .get()
val copyVersionTemplatesProvider = tasks.register<Copy>("copyVersionTemplates") {
  val templatesMap = mapOf(
    "autoServiceKspVersion" to version,
    "autoServiceVersion" to Dependencies.AutoService.version
  )
  inputs.property("buildversions", templatesMap.hashCode())
  from(layout.projectDirectory.dir("version-templates"))
  into(project.layout.buildDirectory.dir("generated/sources/version-templates/kotlin/main"))
  expand(templatesMap)
  filteringCharset = "UTF-8"
}

tasks.withType<KotlinCompile>().configureEach {
  dependsOn(copyVersionTemplatesProvider)
}

dependencies {
  compileOnly(gradleApi())
}
