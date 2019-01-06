//MIT License
//
//Copyright (c) 2018 Armel Soro
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in all
//copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//SOFTWARE.

package org.rm3l.datanucleus.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.rm3l.datanucleus.gradle.utils.DataNucleusPluginTestExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.rm3l.datanucleus.gradle.utils.TestUtils.*;

@SuppressWarnings("Duplicates")
@ExtendWith(DataNucleusPluginTestExtension.class)
class DataNucleusPluginFTest {

    @Test
    @DisplayName("should add DN extension to the build")
    void test_add_extension(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
        final Path buildGradle = tempDir.resolve("build.gradle");
        Files.write(buildGradle,
                ("plugins { id 'org.rm3l.datanucleus-gradle-plugin' }\n\n" +
                        "repositories {\n" +
                        "  mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "  compile 'org.datanucleus:datanucleus-accessplatform-jpa-rdbms:" + DN_JPA_RDBMS_VERSION + "'\n" +
                        "  testCompile 'junit:junit:" + JUNIT_VERSION + "'\n" +
                        "}\n")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
        final BuildResult result = gradle(tempDir, "--debug", "build");
        Assertions.assertNotNull(result);

        final String output = result.getOutput();
        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.contains("Adding DataNucleus extensions to the build "));

        final BuildResult taskListResult = gradle(tempDir, "tasks");
        Assertions.assertNotNull(result);
        final String taskListResultOutput = taskListResult.getOutput();
        Assertions.assertNotNull(taskListResultOutput);
        final String taskListOutputLower = taskListResultOutput.toLowerCase();
        Assertions.assertTrue(taskListOutputLower.contains("DataNucleus Enhancement tasks".toLowerCase()));
        Assertions.assertTrue(taskListOutputLower.contains("DataNucleus SchemaTool tasks".toLowerCase()));
    }

    @ParameterizedTest(name = "\"{0}\"")
    @ValueSource(strings = {"enhance", "enhanceCheck",
            "testEnhance", "testEnhanceCheck",
            "createDatabase", "createDatabaseTables",
            "deleteDatabase", "deleteDatabaseTables",
            "deleteThenCreateDatabaseTables",
            "validateDatabaseTables",
            "dbinfo", "schemainfo"})
    @DisplayName("DN Tasks CLI Options")
    void test_tasks_CLI_Options(String taskName, @DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
        Assertions.assertNotNull(taskName);
        Assertions.assertFalse(taskName.isEmpty());

        final Path buildGradle = tempDir.resolve("build.gradle");
        Files.write(buildGradle,
                ("plugins { id 'org.rm3l.datanucleus-gradle-plugin' }\n\n" +
                        "repositories {\n" +
                        "  mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "  compile 'org.datanucleus:datanucleus-accessplatform-jpa-rdbms:" + DN_JPA_RDBMS_VERSION + "'\n" +
                        "  testCompile 'junit:junit:" + JUNIT_VERSION + "'\n" +
                        "}\n")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        final BuildResult result = gradle(tempDir, "-q", "help", "--task", taskName);
        Assertions.assertNotNull(result);
        final String output = result.getOutput();
        Assertions.assertNotNull(output);
        Assertions.assertFalse(output.trim().isEmpty());

    }

    @Test
    @DisplayName("should enhance domain classes when building the project")
    void test_build_auto_enhances(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
        final Path buildGradle = tempDir.resolve("build.gradle");
        Files.write(buildGradle,
                ("plugins { id 'org.rm3l.datanucleus-gradle-plugin' }\n\n" +
                        "repositories {\n" +
                        "  mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "  compile 'org.datanucleus:datanucleus-accessplatform-jpa-rdbms:" + DN_JPA_RDBMS_VERSION + "'\n" +
                        "  testCompile 'junit:junit:" + JUNIT_VERSION + "'\n" +
                        "}\n" +
                        "\n" +
                        "datanucleus {\n" +
                        "  enhance {\n" +
                        "    api 'JPA'\n" +
                        "    persistenceUnitName 'myPersistenceUnit'\n" +
                        "  }\n" +
                        "}\n")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        final BuildResult result = gradle(tempDir, "build");
        Assertions.assertNotNull(result);
        final BuildTask enhanceTask = result.task(":enhance");
        Assertions.assertNotNull(enhanceTask);
        Assertions.assertSame(TaskOutcome.SUCCESS, enhanceTask.getOutcome());

        final BuildTask enhanceCheckTask = result.task(":enhanceCheck");
        Assertions.assertNull(enhanceCheckTask);

        final String output = result.getOutput();
        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.contains("DataNucleus Enhancer completed with success for 1 classes."));
    }

    @Test
    @DisplayName("should enhance domain test classes when building the project")
    void test_build_auto_testEnhances(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
        final Path buildGradle = tempDir.resolve("build.gradle");
        Files.write(buildGradle,
                ("plugins { id 'org.rm3l.datanucleus-gradle-plugin' }\n\n" +
                        "repositories {\n" +
                        "  mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "  compile 'org.datanucleus:datanucleus-accessplatform-jpa-rdbms:" + DN_JPA_RDBMS_VERSION + "'\n" +
                        "  testCompile 'junit:junit:" + JUNIT_VERSION + "'\n" +
                        "}\n" +
                        "\n" +
                        "datanucleus {\n" +
                        "  testEnhance {\n" +
                        "    api 'JPA'\n" +
                        "    persistenceUnitName 'myPersistenceUnitForTest'\n" +
                        "  }\n" +
                        "}\n")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        final BuildResult result = gradle(tempDir, "build");
        Assertions.assertNotNull(result);
        final BuildTask enhanceTask = result.task(":testEnhance");
        Assertions.assertNotNull(enhanceTask);
        Assertions.assertSame(TaskOutcome.SUCCESS, enhanceTask.getOutcome());

        final String output = result.getOutput();
        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.contains("DataNucleus Enhancer completed with success for 1 classes."));
    }

    @Test
    @DisplayName("should enhance both domain test and main classes when building the project")
    void test_build_auto_enhances_both_main_and_test(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
        final Path buildGradle = tempDir.resolve("build.gradle");
        Files.write(buildGradle,
                ("plugins { id 'org.rm3l.datanucleus-gradle-plugin' }\n\n" +
                        "repositories {\n" +
                        "  mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "  compile 'org.datanucleus:datanucleus-accessplatform-jpa-rdbms:" + DN_JPA_RDBMS_VERSION + "'\n" +
                        "  testCompile 'junit:junit:" + JUNIT_VERSION + "'\n" +
                        "}\n" +
                        "\n" +
                        "datanucleus {\n" +
                        "  enhance {\n" +
                        "    api 'JPA'\n" +
                        "    persistenceUnitName 'myPersistenceUnit'\n" +
                        "  }\n" +
                        "\n" +
                        "  testEnhance {\n" +
                        "    api 'JPA'\n" +
                        "    persistenceUnitName 'myPersistenceUnitForTest'\n" +
                        "  }\n" +
                        "}\n")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        final BuildResult result = gradle(tempDir, "build");
        Assertions.assertNotNull(result);

        final BuildTask enhanceTask = result.task(":enhance");
        Assertions.assertNotNull(enhanceTask);
        Assertions.assertSame(TaskOutcome.SUCCESS, enhanceTask.getOutcome());

        final BuildTask testEnhanceTask = result.task(":testEnhance");
        Assertions.assertNotNull(testEnhanceTask);
        Assertions.assertSame(TaskOutcome.SUCCESS, testEnhanceTask.getOutcome());

        final String output = result.getOutput();
        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.contains("DataNucleus Enhancer completed with success for 1 classes."));

        final BuildTask enhanceCheckTask = result.task(":enhanceCheck");
        Assertions.assertNull(enhanceCheckTask);

        final BuildTask testEnhanceCheckTask = result.task(":testEnhanceCheck");
        Assertions.assertNull(testEnhanceCheckTask);
    }

    @Test
    void testSkip_From_Parent_Propagates_to_Children_Tasks(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
        final Path buildGradle = tempDir.resolve("build.gradle");
        Files.write(buildGradle,
                ("plugins { id 'org.rm3l.datanucleus-gradle-plugin' }\n\n" +
                        "repositories {\n" +
                        "  mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "  compile 'org.datanucleus:datanucleus-accessplatform-jpa-rdbms:" + DN_JPA_RDBMS_VERSION + "'\n" +
                        "  testCompile 'junit:junit:" + JUNIT_VERSION + "'\n" +
                        "}\n" +
                        "\n" +
                        "datanucleus {\n" +
                        "  skip true\n" +
                        "  enhance {\n" +
                        "    api 'JPA'\n" +
                        "    persistenceUnitName 'myPersistenceUnit'\n" +
                        "  }\n" +
                        "\n" +
                        "  testEnhance {\n" +
                        "    api 'JPA'\n" +
                        "    persistenceUnitName 'myPersistenceUnitForTest'\n" +
                        "  }\n" +
                        "}\n")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        final BuildResult result = gradle(tempDir, "build", "--debug");
        Assertions.assertNotNull(result);

        final BuildTask enhanceTask = result.task(":enhance");
        Assertions.assertNotNull(enhanceTask);
        Assertions.assertSame(TaskOutcome.SUCCESS, enhanceTask.getOutcome());

        final BuildTask testEnhanceTask = result.task(":testEnhance");
        Assertions.assertNotNull(testEnhanceTask);
        Assertions.assertSame(TaskOutcome.SUCCESS, testEnhanceTask.getOutcome());

        final String output = result.getOutput();
        Assertions.assertNotNull(output);
        Assertions.assertFalse(output.contains("DataNucleus Enhancer completed with success for 1 classes."));
        Assertions.assertTrue(output.contains("Enhancement Task Execution skipped as requested"));
    }

    @Test
    void testSkip_From_Parent_Can_Be_Overridden_In_Children_Tasks(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
        final Path buildGradle = tempDir.resolve("build.gradle");
        Files.write(buildGradle,
                ("plugins { id 'org.rm3l.datanucleus-gradle-plugin' }\n\n" +
                        "repositories {\n" +
                        "  mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "  compile 'org.datanucleus:datanucleus-accessplatform-jpa-rdbms:" + DN_JPA_RDBMS_VERSION + "'\n" +
                        "  testCompile 'junit:junit:" + JUNIT_VERSION + "'\n" +
                        "}\n" +
                        "\n" +
                        "datanucleus {\n" +
                        "  skip true\n" +
                        "  enhance {\n" +
                        "    skip false\n" +
                        "    api 'JPA'\n" +
                        "    persistenceUnitName 'myPersistenceUnit'\n" +
                        "  }\n" +
                        "}\n")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        final BuildResult result = gradle(tempDir, "build", "--debug");
        Assertions.assertNotNull(result);

        final BuildTask enhanceTask = result.task(":enhance");
        Assertions.assertNotNull(enhanceTask);
        Assertions.assertSame(TaskOutcome.SUCCESS, enhanceTask.getOutcome());

        final String output = result.getOutput();
        Assertions.assertNotNull(output);
        Assertions.assertTrue(output.contains("DataNucleus Enhancer completed with success for 1 classes."));
        Assertions.assertFalse(output.contains("Enhancement Task Execution skipped as requested"));
    }

}
