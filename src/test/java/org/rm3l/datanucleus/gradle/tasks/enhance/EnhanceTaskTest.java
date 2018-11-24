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
import static org.rm3l.datanucleus.gradle.utils.TestUtils.gradle;

@ExtendWith(DataNucleusPluginTestExtension.class)
class EnhanceTaskTest {

    @Test
    @DisplayName("should not succeed enhancing domain classes if no build had been performed beforehand")
    void test_enhance_without_build_does_not_succeed(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
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
                        "    log4jConfiguration null\n" + //Ignored if null
                        "    jdkLogConfiguration null\n" + //Ignored if null
                        "    targetDirectory null\n" + //Ignored if null
                        "  }\n" +
                        "}\n")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        //This does not make the build fail. Instead, a stacktrace is output by DataNucleus Enhancer
        final BuildResult result = gradle(tempDir, "enhance");
        assertNotNull(result);
        final BuildTask enhanceTask = result.task(":enhance");
        assertNotNull(enhanceTask);
        final String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains(
                "Class \"org.rm3l.datanucleus.gradle.test.domain.Person\" was not found in the CLASSPATH. " +
                        "Please check your specification and your CLASSPATH."));
        assertTrue(output.contains("DataNucleus Enhancer completed with success for 0 classes."));
    }

    @Test
    @DisplayName("should succeed enhancing domain classes even if called twice")
    void test_enhance_after_build_succeeds(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
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
                        "    generateConstructor true\n" +
                        "    generatePK true\n" +
                        "    ignoreMetaDataForMissingClasses false\n" +
                        "    detachListener true\n" +
                        "    fork false\n" +
                        "    quiet false\n" +
                        "    verbose false\n" +
                        "    log4jConfiguration \"${rootProject.projectDir}/log4j.properties\"\n" + //ignored if null
                        "    jdkLogConfiguration \"${rootProject.projectDir}/jul.properties\"\n" + //ignored if null
                        "    targetDirectory \"${buildDir}\"\n" + //Defaults to "buildDir" (the output directory) if null or unspecified
                        "  }\n" +
                        "}\n")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        BuildResult result = gradle(tempDir, "build", "enhance");
        assertNotNull(result);
        BuildTask enhanceTask = result.task(":enhance");
        assertNotNull(enhanceTask);
        assertSame(SUCCESS, enhanceTask.getOutcome());
        String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("DataNucleus Enhancer completed with success for 1 classes."));

        result = gradle(tempDir, "enhance");
        assertNotNull(result);
        enhanceTask = result.task(":enhance");
        assertNotNull(enhanceTask);
        assertSame(SUCCESS, enhanceTask.getOutcome());
        output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("DataNucleus Enhancer completed with success for 1 classes."));
    }

}
