package org.rm3l.datanucleus.gradle.tasks.enhance;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rm3l.datanucleus.gradle.utils.DataNucleusPluginTestExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.rm3l.datanucleus.gradle.utils.TestUtils.*;

@ExtendWith(DataNucleusPluginTestExtension.class)
class TestEnhanceCheckTaskTest {

    @Test
    @DisplayName("should not succeed calling enhanceCheck if no enhancement beforehand")
    void test_checkOnly_Does_Not_Succeed_If_No_Enhancement_Beforehand(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
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

        final BuildResult testEnhanceCheckResult = gradle(tempDir, "testEnhanceCheck");
        assertNotNull(testEnhanceCheckResult);
        final String output = testEnhanceCheckResult.getOutput();
        assertNotNull(output);
        assertTrue(output.contains(
                "Class \"org.rm3l.datanucleus.gradle.test.domain.Person\" was not found in the CLASSPATH. " +
                        "Please check your specification and your CLASSPATH."));

    }

    @Test
    @DisplayName("should succeed checking domain test classes if a build had been performed beforehand")
    void test_testEnhanceCheck_with_build_does_succeed(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
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
        BuildResult result = gradle(tempDir, "build", "testEnhanceCheck");
        assertNotNull(result);
        BuildTask testEnhanceCheckTask = result.task(":testEnhanceCheck");
        assertNotNull(testEnhanceCheckTask);
        String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("DataNucleus Enhancer completed with success for 1 classes."));

        result = gradle(tempDir, "testEnhanceCheck");
        assertNotNull(result);
        testEnhanceCheckTask = result.task(":testEnhanceCheck");
        assertNotNull(testEnhanceCheckTask);
        assertSame(SUCCESS, testEnhanceCheckTask.getOutcome());
        output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("DataNucleus Enhancer completed with success for 1 classes."));

    }
}
