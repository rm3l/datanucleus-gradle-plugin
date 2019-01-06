package org.rm3l.datanucleus.gradle.tasks.enhance;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rm3l.datanucleus.gradle.utils.DataNucleusPluginTestExtension;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE;
import static org.junit.jupiter.api.Assertions.*;
import static org.rm3l.datanucleus.gradle.utils.TestUtils.*;

@SuppressWarnings("Duplicates")
@ExtendWith(DataNucleusPluginTestExtension.class)
class TestEnhanceTaskFTest {

    @Test
    @DisplayName("should succeed enhancing domain test classes even if no build had been performed beforehand")
    void test_testEnhance_without_build_does_not_succeed(@DataNucleusPluginTestExtension.TempDir Path tempDir)
            throws IOException {
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

        //This does not make the build fail. Instead, a stacktrace is output by DataNucleus Enhancer
        final BuildResult result = gradle(tempDir, "testEnhance");
        assertNotNull(result);
        final BuildTask enhanceTask = result.task(":testEnhance");
        assertNotNull(enhanceTask);
        assertSame(SUCCESS, enhanceTask.getOutcome());
        final String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("DataNucleus Enhancer completed with success for 1 classes."));
    }

    @Test
    @DisplayName("should succeed enhancing domain test classes even if called twice")
    void test_testEnhance_after_build_succeeds(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
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

        BuildResult result = gradle(tempDir, "build", "testEnhance");
        assertNotNull(result);
        BuildTask enhanceTask = result.task(":testEnhance");
        assertNotNull(enhanceTask);
        assertSame(SUCCESS, enhanceTask.getOutcome());
        String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("DataNucleus Enhancer completed with success for 1 classes."));

        result = gradle(tempDir, "testEnhance");
        assertNotNull(result);
        enhanceTask = result.task(":testEnhance");
        assertNotNull(enhanceTask);
        assertSame(UP_TO_DATE, enhanceTask.getOutcome());
    }

    @Test
    @DisplayName("should succeed running the 'testEnhance' task from CLI")
    void test_run_testEnhance_task_cli_succeeds(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
        final File log4jConfFile = Files.createFile(tempDir.resolve("log4j.conf")).toFile();
        final File jdkLogConfFile = Files.createFile(tempDir.resolve("jdkLog.conf")).toFile();
        final File targetDir = Files.createDirectories(tempDir.resolve("myTargetDir")).toFile();

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
                        "\n")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        BuildResult result = gradle(tempDir,
                "testEnhance",
                "--api", "JPA",
                "--persistence-unit-name", "myPersistenceUnit",
                "--target-directory", targetDir.getAbsolutePath(),
                "--jdk-log-conf", jdkLogConfFile.getAbsolutePath(),
                "--log4j-conf", log4jConfFile.getAbsolutePath());
        assertNotNull(result);
        BuildTask testEnhanceTask = result.task(":testEnhance");
        assertNotNull(testEnhanceTask);
        assertSame(SUCCESS, testEnhanceTask.getOutcome());
        String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("DataNucleus Enhancer completed with success for 1 classes."));
    }

}
