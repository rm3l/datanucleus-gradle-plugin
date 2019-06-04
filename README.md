[![Travis branch](https://img.shields.io/travis/rm3l/datanucleus-gradle-plugin/master.svg)](https://travis-ci.org/rm3l/datanucleus-gradle-plugin)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/rm3l/datanucleus-gradle-plugin.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/rm3l/datanucleus-gradle-plugin/context:java)
[![Code Coverage](https://codecov.io/gh/rm3l/datanucleus-gradle-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/rm3l/datanucleus-gradle-plugin)
[![Known Vulnerabilities](https://snyk.io/test/github/rm3l/datanucleus-gradle-plugin/badge.svg?targetFile=buildSrc%2Fbuild.gradle)](https://snyk.io/test/github/rm3l/datanucleus-gradle-plugin?targetFile=buildSrc%2Fbuild.gradle)
[![License](https://img.shields.io/badge/license-MIT-green.svg?style=flat)](https://github.com/rm3l/datanucleus-gradle-plugin/blob/master/LICENSE)

[![GitHub watchers](https://img.shields.io/github/watchers/rm3l/datanucleus-gradle-plugin.svg?style=social&label=Watch)](https://github.com/rm3l/datanucleus-gradle-plugin)
[![GitHub stars](https://img.shields.io/github/stars/rm3l/datanucleus-gradle-plugin.svg?style=social&label=Star)](https://github.com/rm3l/datanucleus-gradle-plugin)
[![GitHub forks](https://img.shields.io/github/forks/rm3l/datanucleus-gradle-plugin.svg?style=social&label=Fork)](https://github.com/rm3l/datanucleus-gradle-plugin)

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->


- [Getting Started](#getting-started)
- [Tasks](#tasks)
  - [Bytecode Enhancement](#bytecode-enhancement)
  - [SchemaTool](#schematool)
- [Contribution Guidelines](#contribution-guidelines)
  - [Source Code Layout](#source-code-layout)
  - [Building from source](#building-from-source)
  - [Publishing the plugin](#publishing-the-plugin)
- [Credits / Inspiration](#credits--inspiration)
- [Developed by](#developed-by)
- [License](#license)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

Unofficial Gradle Plugin for [DataNucleus](http://www.datanucleus.org/) [JPA](https://en.wikipedia.org/wiki/Java_Persistence_API) and [JDO](https://en.wikipedia.org/wiki/Java_Data_Objects) Provider.

This is a follow-up to a [blog post](https://rm3l.org/datanucleus-jpa-enhancement-with-gradle/) talking about performing build-time enhancement with both DataNucleus and Gradle Ant Tasks.

Heavily inspired by the official DataNucleus Maven Plugin, this one defines a Gradle plugin for introducing DataNucleus specific tasks and capabilities into an end-user Gradle Project build.

Currently the only capabilities added are for bytecode enhancement of the user domain model and schema operations, although other capabilities are planned.

## Getting Started

This plugin is published to the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/org.rm3l.datanucleus-gradle-plugin).

* Build script snippet for plugins DSL for Gradle 2.1 and later:

Grab via Gradle, by applying the plugin (and configure it) in your `build.gradle`:

```groovy
plugins {
  id "org.rm3l.datanucleus-gradle-plugin" version "1.2.0"
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
    classpath "org.rm3l:datanucleus-gradle-plugin:1.2.0"
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

Applying this plugin automatically applies the [Java Plugin](https://docs.gradle.org/current/userguide/java_plugin.html) (if not already the case)
and adds the following tasks to your project:

- Enhancement tasks
  - `enhance` : to enhance classes from the main source set. Run automatically during the build since the [`classes`](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_tasks) task depends on it.
  - `testEnhance` : to enhance classes from the test source set. Run automatically during the build since the [`testClasses`](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_tasks) task depends on it.
  - `enhanceCheck` : to check the main classes for enhancement status
  - `testEnhanceCheck` : to check the test classes for enhancement status

- Schema Tool tasks
  - `createDatabase` : to create the specified database (catalog/schema) if the datastore supports that operation
  - `deleteDatabase` : to delete the specified database (catalog.schema) if the datastore supports that operation
  - `createDatabaseTables` : to create all database tables required for the classes defined by the input data
  - `deleteDatabaseTables` : to delete all database tables required for the classes defined by the input data
  - `validateDatabaseTables` : to validate all database tables required for the classes defined by the input data
  - `deleteThenCreateDatabaseTables` : delete all database tables required for the classes defined by the input data, then create the tables
  - `dbinfo` : provide detailed information about the database, its limits and datatypes support. Only for RDBMS currently
  - `schemainfo` : provide detailed information about the database schema. Only for RDBMS currently

You can see the exhaustive list of tasks by issuing the following command from the root of your project:

```bash
./gradlew tasks
```

To see the runtime options of a particular tasks (say `createDatabaseTables`):
```bash
./gradlew -q help --task createDatabaseTables
```

### Bytecode Enhancement

A noteworthy behavior of most JPA providers is to "enhance" the domain JPA classes.
This technique, also known as weaving, allows to modify the resulting bytecode of your domain classes,
in order to essentially add the following capabilities:

* lazy state initialization
* object dirty state tracking, i.e. the ability to track object updates (including collections/maps mutations),
and translate such updates into JPQL DML queries, which are translated into database-specific SQL queries
* automatic bi-directional mapping, i.e., ensuring that both sides of a relationship are set properly
* optionally, performance optimizations

Some providers have chosen to require all domain classes to be enhanced before any use.
This means that enhancement has to be done beforehand at build time, or at any time between compile time and packaging time.
It is still possible to do it at run-time, but this requires using an appropriate ClassLoader to make sure
the enhanced classes are effectively being used.

On the other hand, other JPA providers do not make enhancement a mandatory prerequisite,
and can do it on-the-fly at run-time.
They still allow to perform enhancement at build time, but this may not be the default behavior.

Performing bytecode enhancement at build time clearly has a performance benefit
over the use of slow proxies or reflection that might be done at run-time.

This plugin supports build-time enhancement by providing Gradle tasks wrapped around the official DataNucleus enhancer. The benefit is that the enhanced classes are what gets added to the final built Jar artifact.

To use the plugin in your Gradle project, after applying it as depicted above,
you need to configure it, e.g.:

```groovy
datanucleus {
  enhance {
    api 'JPA'
    persistenceUnitName 'myPersistenceUnit'
    //... other options are possible
  }

  //
  testEnhance { //'testEnhance' task has exactly the same options as the 'enhance' one above
    api 'JPA'
    persistenceUnitName 'myTestPersistenceUnit'
    //...
  }
}
```

This provides you with the following set of additional tasks:
- `enhance` : to enhance classes from the main source set. Run automatically during the build since the [`classes`](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_tasks) task depends on it.
- `testEnhance` : to enhance classes from the test source set. Run automatically during the build since the [`testClasses`](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_tasks) task depends on it.
- `enhanceCheck` : to check the main classes for enhancement status
- `testEnhanceCheck` : to check the test classes for enhancement status

All those tasks support the same set of enhancement options as in
the official datanucleus-maven-plugin, i.e.:

| Property        | Default value           | Description  |
|-----------------|-------------------------|--------------|
| `persistenceUnitName`      | - | Name of the persistence-unit to enhance. **Mandatory** |
| `log4jConfiguration`      | - | Config file location for Log4J (if using it) |
| `jdkLogConfiguration`      | - | Config file location for JDK1.4 logging (if using it) |
| `api`      | `JDO` | API to enhance to (`JDO` or `JPA`). **Mandatory** |
| `verbose`      | `false` | Verbose output? |
| `quiet`      | `false` | No output? |
| `targetDirectory`      | - | Where the enhanced classes are written (default is to overwrite them) |
| `generatePK`      | `true` | Generate a PK class (of name `{MyClass}_PK`) for cases where there are multiple PK fields yet no *IdClass* is defined. |
| `generateConstructor`      | `true` | Generate a default constructor if not defined for the class being enhanced. |
| `detachListener`      | `false` | Whether to enhance classes to make use of a detach listener for attempts to access an un-detached field. |
| `ignoreMetaDataForMissingClasses`      | `false` | Whether to ignore when we have metadata specified for classes that are not found (e.g in *orm.xml*) |
| `skip`      | `false` | Whether to skip execution |


Note that by default, the [`classes`](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_tasks) task
is automatically marked as depending on the `enhance` task, so that the latter is automatically run when you run a build.

Similarly, the [`testClasses`](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_tasks) task
is automatically marked as depending on the `testEnhance` task.

This way, your resulting artifacts will contain the enhanced classes.

You can also perform enhancement at runtime by running the task and passing its various options. For example, below is the help menu of the `enhance` task:
```bash
❯ ./gradlew -q help --task enhance

Detailed task information for enhance

Path
     :enhance

Type
     EnhanceTask (org.rm3l.datanucleus.gradle.tasks.enhance.EnhanceTask)

Options
     --api     API to enhance to (JDO or JPA). Mandatory. Default is JDO.
               Available values are:
                    JDO
                    JPA

     --detach-listener     Whether to enhance classes to make use of a detach listener for attempts to access an un-detached field.

     --generate-constructor     Whether to generate a default constructor if not defined for the class being enhanced.

     --generate-pk     Whether to generate a PK class (of name {MyClass}_PK) for cases where there are multiple PK fields yet no IdClass is defined.

     --ignore-metadata-for-missing-classes     Whether to ignore when we have metadata specified for classes that are not found (e.g in orm.xml)

     --jdk-log-conf     Config file location for JDK logging (if using it)

     --log4j-conf     Config file location for Log4J (if using it)

     --persistence-unit-name     Name of the persistence-unit to enhance. Mandatory

     --quiet     Whether to be quiet or not

     --skip     Whether to skip execution

     --target-directory     Where the enhanced classes are written (default is to overwrite them)

     --verbose     Whether to be verbose or not

Description
     Performs enhancement of the main classes.

Group
     DataNucleus Enhancement
```

So for example, we can issue the following command to enhance classes part of a given persistence unit (named 'myPersistenceUnit'):
```bash
./gradlew enhance --api JPA --persistence-unit-name myPersistenceUnit
```

### SchemaTool

This plugin works hand-by-hand with DataNucleus SchemaTool, which currently works with
RDBMS, HBase, Excel, OOXML,  ODF, MongoDB, Cassandra datastores.

To use the plugin in your Gradle project, after applying it as depicted above,
you need to configure it, e.g.:

```groovy
datanucleus {
  schemaTool {
    api 'JPA'
    persistenceUnitName 'myPersistenceUnit'
    //... other options are possible
  }
}
```

Configuring the DSL provides you with the following set of SchemaTool tasks:
- `createDatabase` : to create the specified database (catalog/schema) if the datastore supports that operation
- `deleteDatabase` : to delete the specified database (catalog.schema) if the datastore supports that operation
- `createDatabaseTables` : to create all database tables required for the classes defined by the input data
- `deleteDatabaseTables` : to delete all database tables required for the classes defined by the input data
- `validateDatabaseTables` : to validate all database tables required for the classes defined by the input data
- `deleteThenCreateDatabaseTables` : delete all database tables required for the classes defined by the input data, then create the tables
- `dbinfo` : provide detailed information about the database, its limits and datatypes support. Only for RDBMS currently
- `schemainfo` : provide detailed information about the database schema. Only for RDBMS currently

All those tasks support the same set of options as in the official DataNucleus Maven Plugin, i.e.:

| Property        | Default value           | Description  |
|-----------------|-------------------------|--------------|
| `persistenceUnitName`      | - | Name of the persistence-unit to enhance. **Mandatory** |
| `log4jConfiguration`      | - | Config file location for Log4J (if using it) |
| `jdkLogConfiguration`      | - | Config file location for JDK1.4 logging (if using it) |
| `api`      | `JDO` | API to enhance to (`JDO` or `JPA`). **Mandatory** |
| `catalogName`      | - | Name of the catalog. **Mandatory** when using `createDatabase` or `deleteDatabase` options |
| `schemaName`      | - | Name of the schema. **Mandatory** when using `createDatabase` or `deleteDatabase` options |
| `verbose`      | `false` | Verbose output? |
| `quiet`      | `false` | No output? |
| `completeDdl`      | `false` | Whether to generate DDL including things that already exist? (for RDBMS)|
| `ddlFile`      | - | Name of an output file to dump any DDL to (for RDBMS) |
| `ignoreMetaDataForMissingClasses`      | `false` | Whether to ignore when we have metadata specified for classes that are not found (e.g in *orm.xml*) |
| `skip`      | `false` | Whether to skip execution |


Like for the Enhancement tasks depicted above, you can also perform SchemaTool operations at runtime by running the task and passing its various options.
For example, below is the help menu of the `createDatabaseTables` task:

```bash
./gradlew -q help --task createDatabaseTables

Detailed task information for createDatabaseTables

Path
     :createDatabaseTables

Type
     CreateDatabaseTablesTask (org.rm3l.datanucleus.gradle.tasks.schematool.CreateDatabaseTablesTask)

Options
     --api     API to enhance to (JDO or JPA). Mandatory. Default is JDO.
               Available values are:
                    JDO
                    JPA

     --catalog-name     Catalog Name

     --complete-ddl     Whether to consider complete DDL or not

     --ddl-file     Path to DDL file

     --ignore-metadata-for-missing-classes     Whether to ignore when we have metadata specified for classes that are not found (e.g in orm.xml)

     --jdk-log-conf     Config file location for JDK logging (if using it)

     --log4j-conf     Config file location for Log4J (if using it)

     --persistence-unit-name     Name of the persistence-unit to enhance. Mandatory

     --schema-name     Schema Name

     --skip     Whether to skip execution

     --verbose     Whether to be verbose or not

Description
     Creates all database tables required for the classes defined by the input data.

Group
     DataNucleus SchemaTool
```

## Contribution Guidelines

Contributions and issue reporting are more than welcome. So to help out, do feel free to fork this repo and open up a pull request.
I'll review and merge your changes as quickly as possible.

You can use [GitHub issues](https://github.com/rm3l/datanucleus-gradle-plugin/issues) to report bugs.
However, please make sure your description is clear enough and has sufficient instructions to be able to reproduce the issue.

### Source Code Layout

Source Code is organized as much as possible per the official Gradle conventions, as follows:

* `buildSrc` : the actual code of the Plugin, along with its own unit, integration and functional tests
* `sample-jdo` : sample JDO project serving as a reference project that can be used to test and play with the plugin
* `sample-jpa` : sample JPA project serving as a reference project that can be used to test and play with the plugin
* `sample-jpa-multiproject` : sample multi-module Gradle project serving as a reference project that can be used to test and play with the plugin

Please note that Jacoco coverage metrics displayed here are reported against the plugin code solely.

### Building from source

This can be built as a standard Gradle Project, by issuing the following command:

```bash
./gradlew build
```

This command automatically builds the plugin code (from the `buildSrc` folder) and then continues with the sample project.

### Publishing the plugin

All releases of this plugin ought to be published to the [Gradle Plugin Portal](https://plugins.gradle.org/plugin/org.rm3l.datanucleus-gradle-plugin).

To publish the plugin, you must have the appropriate rights on the Gradle Plugin Portal.
Additionally, the key and secret credentials for publishing are recommended to be put on your 
local machine in your `${HOME}/.gradle/gradle.properties`, which should contain the following two properties:

- `gradle.publish.key` : the API key for publishing, which you can grab from the Gradle Plugin Portal "API Keys" section
- `gradle.publish.secret` : the API secret for publishing, which you can grab from the Gradle Plugin Portal "API Keys" section

Publishing the plugin is then as easy as calling the `publishPlugins` task **from the `buildSrc` folder**:

```bash
../gradlew publishPlugins
``` 
 
Do not forget to make and push the corresponding tag afterwards if needed. 

## Credits / Inspiration

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

