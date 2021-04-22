Changelog
=========

Version 0.4.2
-------------

_2021-04-22_

* Update to KSP `1.4.32-alpha08`
* (Internal) Switch to `SymbolProcessorProvider` API, which means KSP `1.4.32-alpha08` is the 
  minimum required version!

Version 0.4.1
-------------

_2021-04-22_

Please skip this release and use 0.4.2

Version 0.4.0
-------------

_2021-04-09_

* Update to KSP `1.4.32-1.0.0-alpha07`
* Update Kotlin to 1.4.32
* Update AutoService to 1.0 stable

Version 0.3.3
-------------

_2021-03-01_

* Updated to KSP `1.4.30-1.0.0-alpha04`
* Errors are now reported via KSP's error logger.

Version 0.3.2
-------------

_2021-02-11_

* Updated to KSP `1.4.30-1.0.0-alpha02`

Version 0.3.1
-------------

_2021-01-11_

* Updated to KSP `1.4.20-dev-experimental-20210111`

Version 0.3.0
-------------

_2021-01-10_

* Updated to KSP `1.4.20-dev-experimental-20210107`
* The Gradle plugin is no more! Now that KSP natively handles including generated resources, it is no longer needed.
Add `auto-service-ksp` and `auto-service-annotations` dependencies directly now:
  
  ```kotlin
  dependencies {
    ksp("dev.zacsweers.autoservice:auto-service-ksp:<auto-service-ksp version>")
    implementation("com.google.auto.service:auto-service-annotations:<auto-service version>")
  }
  ```

Version 0.2.1
-------------

_2020-12-26_

* Small Gradle bugfix for KSP `1.4.20-dev-experimental-20201222`'s new generated dirs location, 
  which no longer includes a `src/` intermediate dir.

Version 0.2.0
-------------

_2020-12-26_

This introduces support for KSP's new incremental processing support. Because multiple classes can 
contribute to a single output file, this processor acts effectively as an "aggregating" processor.

Note that incremental processing itself is _not_ enabled by default and must be enabled via 
`ksp.incremental=true` Gradle property. See KSP's release notes for more details: 
https://github.com/google/ksp/releases/tag/1.4.20-dev-experimental-20201222

* KSP `1.4.20-dev-experimental-20201222`
* Kotlin `1.4.20`
* Compatible with Gradle `6.8-rc-4`

Version 0.1.2
-------------

_2020-11-10_

No functional changes, but updated the following dependencies:
* KSP `1.4.10-dev-experimental-20201110`
* Gradle `6.7`

Version 0.1.1
-------------

_2020-10-25_

No functional changes, but updated the following dependencies:
* KSP `1.4.10-dev-experimental-20201023`
* Guava `30.0-jre`
* KotlinPoet `1.7.2`

Version 0.1.0
-------------

_2020-09-26_

Initial release
