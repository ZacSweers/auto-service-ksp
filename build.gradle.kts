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
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ksp)
  alias(libs.plugins.spotless)
  alias(libs.plugins.binaryCompatibilityValidator)
  alias(libs.plugins.dokka) apply false
  alias(libs.plugins.mavenPublish) apply false
}


apiValidation { ignoredProjects += listOf("processor") }

spotless {
  format("misc") {
    target("*.gradle", "*.md", ".gitignore")
    trimTrailingWhitespace()
    indentWithSpaces(2)
    endWithNewline()
  }
  kotlin {
    target("**/*.kt")
    ktfmt(libs.versions.ktfmt.get())
    trimTrailingWhitespace()
    endWithNewline()
    licenseHeaderFile("spotless/spotless.kt")
    targetExclude("**/spotless.kt", "**/build/**")
  }
  kotlinGradle {
    target("**/*.kts")
    ktfmt(libs.versions.ktfmt.get())
    trimTrailingWhitespace()
    endWithNewline()
  }
}

subprojects {
  pluginManager.withPlugin("java") {
    configure<JavaPluginExtension> {
      toolchain {
        languageVersion.set(libs.versions.jdk.map(JavaLanguageVersion::of))
      }
    }

    project.tasks.withType<JavaCompile>().configureEach {
      options.release.set(libs.versions.jvmTarget.map(String::toInt))
    }
  }

  pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
    configure<KotlinProjectExtension> {
      explicitApi()
    }

    tasks.withType<KotlinCompile>().configureEach {
      compilerOptions {
        // TODO kotlin 1.9.0
//        progressiveMode.set(true)
        if (project.name != "sample") {
          jvmTarget.set(libs.versions.jvmTarget.map(JvmTarget::fromTarget))
        }
        freeCompilerArgs.addAll("-Xjsr305=strict", "-progressive")
      }
    }

  }

  plugins.withId("com.vanniktech.maven.publish") {
    configure<MavenPublishBaseExtension> { publishToMavenCentral(automaticRelease = true) }

    // configuration required to produce unique META-INF/*.kotlin_module file names
    tasks.withType<KotlinCompile>().configureEach {
      compilerOptions { moduleName.set(project.property("POM_ARTIFACT_ID") as String) }
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
