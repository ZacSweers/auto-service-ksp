Changelog
=========

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
