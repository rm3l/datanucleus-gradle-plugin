package org.rm3l.datanucleus.gradle.tasks.schematool;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.rm3l.datanucleus.gradle.utils.DataNucleusPluginTestExtension;
import org.rm3l.datanucleus.gradle.utils.ExpectedSystemExit;
import org.rm3l.datanucleus.gradle.utils.TestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.rm3l.datanucleus.gradle.utils.TestUtils.*;

@SuppressWarnings("Duplicates")
@ExpectedSystemExit
class ValidateDatabaseTablesTaskFTest {

    @RegisterExtension
    final DataNucleusPluginTestExtension dataNucleusPluginTestExtension
            = new DataNucleusPluginTestExtension(
            (persistenceUnitMetaData, testPersistenceUnitMetaData) -> {
                persistenceUnitMetaData.addProperty("javax.persistence.jdbc.url",
                        "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;DATABASE_TO_UPPER=FALSE");
                persistenceUnitMetaData.addProperty("javax.persistence.jdbc.driver", "org.h2.Driver");
                persistenceUnitMetaData.addProperty("javax.persistence.jdbc.user", "SA");
                persistenceUnitMetaData.addProperty("javax.persistence.jdbc.password", "");
                persistenceUnitMetaData.addProperty("datanucleus.schema.autoCreateAll", "true");
            });

    @Test
    @DisplayName("should succeed validating the database tables against an in-memory datastore")
    void test_ValidateDBTables_does_succeed(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
        final Path buildGradle = tempDir.resolve("build.gradle");
        Files.write(buildGradle,
                ("plugins { id 'org.rm3l.datanucleus-gradle-plugin' }\n\n" +
                        "repositories {\n" +
                        "  mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "  implementation 'org.datanucleus:datanucleus-accessplatform-jpa-rdbms:" + DN_JPA_RDBMS_VERSION + "'\n" +
                        "  implementation 'com.h2database:h2:" + H2_VERSION + "'\n" +
                        "  testImplementation 'junit:junit:" + JUNIT_VERSION + "'\n" +
                        "}\n" +
                        "\n" +
                        "datanucleus {\n" +
                        "  schemaTool {\n" +
                        "    api 'JPA'\n" +
                        "    persistenceUnitName 'myPersistenceUnit'\n" +
                        "  }\n" +
                        "}\n")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        //This does not make the build fail. Instead, a stacktrace is output by DataNucleus Enhancer
        BuildResult result = gradle(tempDir, "build", "validateDatabaseTables");
        assertNotNull(result);
        BuildTask validateDatabaseTablesTask = result.task(":validateDatabaseTables");
        assertNotNull(validateDatabaseTablesTask);
        assertSame(SUCCESS, validateDatabaseTablesTask.getOutcome());
        String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("DataNucleus SchemaTool : Validation of the schema for classes"));
        assertTrue(output.contains("DataNucleus SchemaTool completed successfully"));
    }

    @Test
    @DisplayName("should skip validating the database tables against an in-memory datastore")
    void test_ValidateDBTables_skip(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
        final Path buildGradle = tempDir.resolve("build.gradle");
        Files.write(buildGradle,
                ("plugins { id 'org.rm3l.datanucleus-gradle-plugin' }\n\n" +
                        "repositories {\n" +
                        "  mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "  implementation 'org.datanucleus:datanucleus-accessplatform-jpa-rdbms:" + DN_JPA_RDBMS_VERSION + "'\n" +
                        "  implementation 'com.h2database:h2:" + H2_VERSION + "'\n" +
                        "  testImplementation 'junit:junit:" + JUNIT_VERSION + "'\n" +
                        "}\n" +
                        "\n" +
                        "datanucleus {\n" +
                        "  schemaTool {\n" +
                        "    skip true\n" +
                        "    api 'JPA'\n" +
                        "    persistenceUnitName 'myPersistenceUnit'\n" +
                        "  }\n" +
                        "}\n")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        //This does not make the build fail. Instead, a stacktrace is output by DataNucleus Enhancer
        BuildResult result = gradle(tempDir, "build", "validateDatabaseTables", "--debug");
        assertNotNull(result);
        BuildTask validateDatabaseTablesTask = result.task(":validateDatabaseTables");
        assertNotNull(validateDatabaseTablesTask);
        assertSame(SUCCESS, validateDatabaseTablesTask.getOutcome());
        String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("SchemaTool Task Execution skipped as requested"));
    }

    @Test
    @DisplayName("should succeed running the 'validateDatabaseTables' task from CLI")
    void test_run_validateDatabaseTables_task_cli_succeeds(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
        final File log4jConfFile = Files.createFile(tempDir.resolve("log4j.conf")).toFile();
        final File jdkLogConfFile = Files.createFile(tempDir.resolve("jdkLog.conf")).toFile();

        final Path buildGradle = tempDir.resolve("build.gradle");
        Files.write(buildGradle,
                ("plugins { id 'org.rm3l.datanucleus-gradle-plugin' }\n\n" +
                        "repositories {\n" +
                        "  mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "  implementation 'org.datanucleus:datanucleus-accessplatform-jpa-rdbms:" + DN_JPA_RDBMS_VERSION + "'\n" +
                        "  implementation 'com.h2database:h2:" + H2_VERSION + "'\n" +
                        "  testImplementation 'junit:junit:" + JUNIT_VERSION + "'\n" +
                        "}\n" +
                        "\n")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        BuildResult enhanceResult = gradle(tempDir,
                "enhance",
                "--api", "JPA",
                "--persistence-unit-name", "myPersistenceUnit");
        assertNotNull(enhanceResult);
        BuildTask enhanceTask = enhanceResult.task(":enhance");
        assertNotNull(enhanceTask);
        assertSame(SUCCESS, enhanceTask.getOutcome());
        String output = enhanceResult.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("DataNucleus Enhancer completed with success for 1 classes."));

        BuildResult result = gradle(tempDir,
                "validateDatabaseTables",
                "--api", "JPA",
                "--persistence-unit-name", "myPersistenceUnit",
                "--jdk-log-conf", TestUtils.getAbsolutePath(jdkLogConfFile),
                "--log4j-conf", TestUtils.getAbsolutePath(log4jConfFile));
        assertNotNull(result);
        BuildTask validateDatabaseTablesTask = result.task(":validateDatabaseTables");
        assertNotNull(validateDatabaseTablesTask);
        assertSame(SUCCESS, validateDatabaseTablesTask.getOutcome());
    }
}
