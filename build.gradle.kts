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
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
  dependencies {
    classpath(kotlin("gradle-plugin", version = Dependencies.Kotlin.version))
  }
}

plugins {
  id("symbol-processing") version Dependencies.Kotlin.Ksp.version
  kotlin("jvm") version Dependencies.Kotlin.version
  kotlin("kapt") version Dependencies.Kotlin.version
  id("org.jetbrains.dokka") version Dependencies.Kotlin.dokkaVersion
  id("com.vanniktech.maven.publish") version "0.13.0"
}

repositories {
  mavenCentral()
  google()
  jcenter()
}

tasks.named<DokkaTask>("dokkaHtml") {
  outputDirectory.set(rootProject.rootDir.resolve("docs/0.x"))
  dokkaSourceSets.configureEach {
    skipDeprecated.set(true)
    // TODO Dokka can't parse javadoc.io yet
//    externalDocumentationLink {
//      url.set(URL("https://javadoc.io/doc/com.google.auto.service/auto-service-annotations/latest/index.html"))
//    }
  }
}

configure<JavaPluginExtension> {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    jvmTarget = Dependencies.Kotlin.jvmTarget
    @Suppress("SuspiciousCollectionReassignment")
    freeCompilerArgs += Dependencies.Kotlin.defaultFreeCompilerArgs
  }
}

dependencies {
  kapt(Dependencies.AutoService.processor)
  compileOnly(Dependencies.Kotlin.Ksp.api)

  implementation(Dependencies.AutoService.annotations)
  implementation(Dependencies.KotlinPoet.kotlinPoet)
  implementation(Dependencies.guava)

  testImplementation(Dependencies.Kotlin.Ksp.api)
  testImplementation(Dependencies.Testing.truth)
  testImplementation(Dependencies.Testing.junit)
  testImplementation(Dependencies.Testing.kspCompileTesting)
}
