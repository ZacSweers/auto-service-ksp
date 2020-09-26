# auto-service-ksp

A [KSP](https://github.com/google/ksp) implementation of [AutoService](https://github.com/google/auto/tree/master/service).

## Usage

Simply add the auto-service-ksp Gradle Plugin.

```kotlin
plugins {
  id("symbol-processing") version "<version>"
  kotlin("jvm")
  id("dev.zacsweers.autoservice.ksp") version "<version>"
  // ...
}

// Optional - configure auto-service or auto-service-ksp versions used
autoServiceKsp {
  autoServiceVersion.set("<version>")
  autoServiceKspVersion.set("<version>")
}
```

Then annotate your service implementation with `@AutoService` as your normally would in source.

### Manual configuration

If you don't want to use the Gradle plugin, you can manually configure the processor with some extra steps.

```kotlin
plugins {
  id("symbol-processing") version "<version>"
  kotlin("jvm")
  // ...
}

dependencies {
  ksp("dev.zacsweers.autoservice:auto-service-ksp:<version>")
  
  // NOTE: It's important that you _don't_ use compileOnly here, as it will fail to resolve at compile-time otherwise
  implementation("com.google.auto.service:auto-service-annotations:<version>")
}
```

Note that in order for Gradle to include the generated service file in the resulting jar, you need
to add the generated resources dir to your main sourceSet and make Gradle's `processResources` task
depend on the `compileKotlin` task (or whatever your equivalents are in your project).

```kotlin
// Necessary to ensure the generated service file is included in the jar
sourceSets {
  main {
    resources {
      srcDir("build/generated/ksp/src/main/resources")
    }
  }
}

val compileKotlin = tasks.named("compileKotlin")
tasks.named<ProcessResources>("processResources").configure {
  dependsOn(compileKotlin)
}
```

## Caveats

While the AutoService _annotation processor_ will merge existing service files, but this is not 
currently implemented in this KSP implementation yet.

## Advanced

You can customize behavior via KSP arguments. Currently there are two.

```kotlin
ksp {
  arg("autoserviceKsp.verify", "true")
  arg("autoserviceKsp.verbose", "true")
}
```

`autoserviceKsp.verify` enables extra validation to ensure that the annotated type does, in fact, implement the service
it claims to.

`autoserviceKsp.verbose` enables verbose logging.

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap].

License
--------

    Copyright 2020 Zac Sweers

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


[snap]: https://oss.sonatype.org/content/repositories/snapshots/dev/zacsweers/autoservice/
