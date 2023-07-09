# auto-service-ksp

A [KSP](https://github.com/google/ksp) implementation of [AutoService](https://github.com/google/auto/tree/master/service).

## Usage

Simply add the auto-service-ksp Gradle Plugin.

[![Maven Central](https://img.shields.io/maven-central/v/dev.zacsweers.autoservice/auto-service-ksp.svg)](https://mvnrepository.com/artifact/dev.zacsweers.autoservice/auto-service-ksp)
```kotlin
plugins {
  id("com.google.devtools.ksp") version "<version>"
  // ...
}

dependencies {
  ksp("dev.zacsweers.autoservice:auto-service-ksp:<auto-service-ksp version>")

  // NOTE: It's important that you _don't_ use compileOnly here, as it will fail to resolve at compile-time otherwise
  implementation("com.google.auto.service:auto-service-annotations:<autoservice version>")
}
```

Then annotate your service implementation with `@AutoService` as your normally would in source files.

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
