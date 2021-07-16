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
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
  dependencies {
    classpath(kotlin("gradle-plugin", version = Dependencies.Kotlin.version))
  }
}

plugins {
  id("com.google.devtools.ksp") version Dependencies.Kotlin.Ksp.version apply false
  kotlin("jvm") version Dependencies.Kotlin.version apply false
  id("org.jetbrains.dokka") version Dependencies.Kotlin.dokkaVersion  apply false
  id("com.vanniktech.maven.publish") version "0.17.0" apply false
}

subprojects {
  repositories {
    mavenCentral()
    google()
  }

  pluginManager.withPlugin("java") {
    configure<JavaPluginExtension> {
      toolchain {
        languageVersion.set(JavaLanguageVersion.of(16))
      }
    }

    project.tasks.withType<JavaCompile>().configureEach {
      options.release.set(8)
    }
  }

  pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
    configure<KotlinProjectExtension> {
      explicitApi()
    }

    tasks.withType<KotlinCompile>().configureEach {
      kotlinOptions {
        jvmTarget = Dependencies.Kotlin.jvmTarget
        @Suppress("SuspiciousCollectionReassignment")
        freeCompilerArgs += Dependencies.Kotlin.defaultFreeCompilerArgs
      }
    }

    apply(plugin = "org.jetbrains.dokka")
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
  }
}
