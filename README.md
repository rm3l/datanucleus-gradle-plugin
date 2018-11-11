[![Travis branch](https://img.shields.io/travis/rm3l/datanucleus-gradle-plugin/master.svg)](https://travis-ci.org/rm3l/datanucleus-gradle-plugin)
[![Code Coverage](https://codecov.io/gh/rm3l/datanucleus-gradle-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/rm3l/datanucleus-gradle-plugin)
[![License](https://img.shields.io/badge/license-MIT-green.svg?style=flat)](https://github.com/rm3l/datanucleus-gradle-plugin/blob/master/LICENSE)

[![GitHub watchers](https://img.shields.io/github/watchers/rm3l/datanucleus-gradle-plugin.svg?style=social&label=Watch)](https://github.com/rm3l/datanucleus-gradle-plugin)
[![GitHub stars](https://img.shields.io/github/stars/rm3l/datanucleus-gradle-plugin.svg?style=social&label=Star)](https://github.com/rm3l/datanucleus-gradle-plugin)
[![GitHub forks](https://img.shields.io/github/forks/rm3l/datanucleus-gradle-plugin.svg?style=social&label=Fork)](https://github.com/rm3l/datanucleus-gradle-plugin)

Unofficial Gradle Plugin for [DataNucleus](http://www.datanucleus.org/) JPA and JDO Provider.

This defines a Gradle plugin for introducing DataNucleus specific tasks and capabilities into and end-user Gradle Project build.

Currently the only capability added is for bytecode enhancement of the user domain model, although other capabilities are planned.

## Getting Started

This plugin is published to the Gradle Plugins repo.

* Build script snippet for plugins DSL for Gradle 2.1 and later:

Grab via Gradle, by applying the plugin (and configure it) in your `build.gradle`:

```groovy
plugins {
  id "org.rm3l.datanucleus-gradle-plugin" version "1.0-SNAPSHOT"
}
```

* Build script snippet for use in older Gradle versions or where dynamic configuration is required:

```groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "gradle.plugin.org.rm3l:datanucleus-gradle-plugin:1.0-SNAPSHOT"
  }
}

apply plugin: "org.rm3l.datanucleus-gradle-plugin"
```

Note that this plugin automatically applies the Gradle [JavaPlugin](https://docs.gradle.org/current/userguide/java_plugin.html), as
this is a prerequisite here.

## Tasks

This plugin aims at supporting the same set of operations provided
by the official [DataNucleus Maven Plugin](https://github.com/datanucleus/datanucleus-maven-plugin):

* Bytecode Enhancement
* SchemaTool

### Bytecode Enhancement

A noteworthy behavior of most JPA providers is to "enhance" the domain JPA classes.
This technique, also known as weaving, allows to modify the resulting bytecode of your domain classes,
in order to essentially add the following capabilities:

* lazy state initialization
* object dirty state tracking, i.e. the ability to track object updates (including collections mutations),
and translate such updates into JPQL DML queries, which are translated into database-specific SQL queries
* automatic bi-directional mapping, i.e., ensuring that both sides of a relationship are set properly
* optionally, performance optimizations

Some providers such as DataNucleus have chosen to require all domain classes to be enhanced before any use.
This means that enhancement has to be done beforehand at build time, or at any time between compile time and packaging time.
It is still possible to do it at run-time, but this requires using an appropriate ClassLoader to make sure
the enhanced classes are effectively being used.

On the other hand, other JPA providers do not make enhancement a mandatory prerequisite,
and can do it on-the-fly at run-time.
They still allow to perform enhancement at build time, but this may not be the default behavior.

Performing bytecode enhancement at build time clearly has a performance benefit
over the use of slow proxies or reflection that might be done at run-time.

This plugin supports build-time enhancement. The benefit is that the enhanced classes are what
gets added to the final built Jar artifact.

To use the plugin in your Gradle project, after applying it as depicted above,
you need to configure it, e.g.:

```groovy
datanucleus {
  enhance {
    api 'JPA'
    persistenceUnitName 'myPersistenceUnit'
    //... other options are possible
  }
}
```

Applying this plugin provides an `enhance` task that supports the same
set of enhancement options as in the official datanucleus-maven-plugin, i.e.:

| Property        | Default value           | Description  |
|-----------------|-------------------------|--------------|
| `persistenceUnitName`      | | Name of the persistence-unit to enhance. **Mandatory** |
| `log4jConfiguration`      | | Config file location for Log4J (if using it) |
| `jdkLogConfiguration`      | | Config file location for JDK1.4 logging (if using it) |
| `api`      | `JDO` | API to enhance to (`JDO` or `JPA`). **Mandatory** |
| `verbose`      | `false` | Verbose output? |
| `quiet`      | `false` | No output? |
| `targetDirectory`      | | Where the enhanced classes are written (default is to overwrite them) |
| `fork`      | `true` | Whether to fork the enhancer process |
| `generatePK`      | `true` | Generate a PK class (of name `{MyClass}_PK`) for cases where there are multiple PK fields yet no *IdClass* is defined. |
| `generateConstructor`      | `true` | Generate a default constructor if not defined for the class being enhanced. |
| `detachListener`      | `false` | Whether to enhance classes to make use of a detach listener for attempts to access an un-detached field. |
| `ignoreMetaDataForMissingClasses`      | `false` | Whether to ignore when we have metadata specified for classes that are not found (e.g in *orm.xml*) |


Note that by default, the `classes` task is automatically marked as depending on the `enhance` task, so that the latter is automatically run
when you run a build. This way, your resulting artifacts will contain the enhanced classes.


## TODO

* [ ] Make the `enhance` task [Cacheable](https://docs.gradle.org/current/userguide/build_cache.html#sec:task_output_caching_details)
  , and ensure it is marked as *UP-TO-DATE* if there is no change to JPA Domain source files
* [ ] Support DataNucleus SchemaTool operations


## Credits

* [Gradle Build Tool](https://gradle.org/)
* [DataNucleus Maven Plugin](https://github.com/datanucleus/datanucleus-maven-plugin)
* [Hibernate Gradle Plugin](https://github.com/hibernate/hibernate-orm/tree/master/tooling/hibernate-gradle-plugin)

## Developed by

* Armel Soro
  * [keybase.io/rm3l](https://keybase.io/rm3l)
  * [rm3l.org](https://rm3l.org) - &lt;armel+dn_gradle_plugin@rm3l.org&gt; - [@rm3l](https://twitter.com/rm3l)
  * [paypal.me/rm3l](https://paypal.me/rm3l)
  * [coinbase.com/rm3l](https://www.coinbase.com/rm3l)

## License

    The MIT License (MIT)

    Copyright (c) 2018 Armel Soro

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.

