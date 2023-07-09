Changelog
=========

1.1.0
-----

_2023-07-09_

Happy couple of new years!

- **Fix**: The processor no longer errors if `@AutoService` isn't found on the classpath. By default it will log to `info` level and to `warn` level if verbose mode is enabled.
- Update Kotlin to `1.8.22`.
- Update KSP to `1.8.22-1.0.11`.
- Update Guava to `32.1.1-jre`.
- Update KotlinPoet to `1.14.2`.

1.0.0
-----

_2021-09-07_

* Stable release!
* Update to KSP `1.5.30-1.0.0`.

0.5.5
-----

_2021-09-02_

* Update KSP to `1.5.30-1.0.0-beta09`.
* **Fix:** Java sources are now supported.

Thanks to [@ganadist](https://github.com/ganadist) for contributing to this release!

0.5.4
-----

_2021-08-27_

* Update to KSP `1.5.30-1.0.0-beta08`.
* Update to Kotlin `1.5.30`.

0.5.3
-----

_2021-07-15_

* Update to KSP `1.5.21-1.0.0-beta05`.
* Update to Kotlin `1.5.21`.
* Update to Dokka `1.5.0`.
* Update to KotlinPoet `1.9.0`.
* Test against JDK 17 early access previews.

0.5.2
-----

_2021-05-27_

* Update to KSP `1.5.10-1.0.0-beta01`
* Update to Kotlin `1.5.10`

0.5.1
-----

_2021-05-13_

* Update to KSP `1.5.0-1.0.0-alpha10`

0.5.0
-----

_2021-04-29_

* Update to KSP `1.5.0-1.0.0-alpha09`
* Update Kotlin to `1.5.0`

0.4.2
-----

_2021-04-22_

* Update to KSP `1.4.32-1.0.0-alpha08`
* (Internal) Switch to `SymbolProcessorProvider` API, which means KSP `1.4.32-1.0.0-alpha08` is the
  minimum required version!

0.4.1
-----

_2021-04-22_

Please skip this release and use 0.4.2

0.4.0
-----

_2021-04-09_

* Update to KSP `1.4.32-1.0.0-alpha07`
* Update Kotlin to 1.4.32
* Update AutoService to 1.0 stable

0.3.3
-----

_2021-03-01_

* Updated to KSP `1.4.30-1.0.0-alpha04`
* Errors are now reported via KSP's error logger.

0.3.2
-----

_2021-02-11_

* Updated to KSP `1.4.30-1.0.0-alpha02`

0.3.1
-----

_2021-01-11_

* Updated to KSP `1.4.20-dev-experimental-20210111`

0.3.0
-----

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

0.2.1
-----

_2020-12-26_

* Small Gradle bugfix for KSP `1.4.20-dev-experimental-20201222`'s new generated dirs location,
  which no longer includes a `src/` intermediate dir.

0.2.0
-----

_2020-12-26_

This introduces support for KSP's new incremental processing support. Because multiple classes can
contribute to a single output file, this processor acts effectively as an "aggregating" processor.

Note that incremental processing itself is _not_ enabled by default and must be enabled via
`ksp.incremental=true` Gradle property. See KSP's release notes for more details:
https://github.com/google/ksp/releases/tag/1.4.20-dev-experimental-20201222

* KSP `1.4.20-dev-experimental-20201222`
* Kotlin `1.4.20`
* Compatible with Gradle `6.8-rc-4`

0.1.2
-----

_2020-11-10_

No functional changes, but updated the following dependencies:
* KSP `1.4.10-dev-experimental-20201110`
* Gradle `6.7`

0.1.1
-----

_2020-10-25_

No functional changes, but updated the following dependencies:
* KSP `1.4.10-dev-experimental-20201023`
* Guava `30.0-jre`
* KotlinPoet `1.7.2`

0.1.0
-----

_2020-09-26_

Initial release
