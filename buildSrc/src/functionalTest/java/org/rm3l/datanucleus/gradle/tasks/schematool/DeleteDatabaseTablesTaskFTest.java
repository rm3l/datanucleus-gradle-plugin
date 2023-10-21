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
class DeleteDatabaseTablesTaskFTest {

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
    @DisplayName("should succeed deleting the database tables against an in-memory datastore")
    void test_DeleteDBTables_does_succeed(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
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
        BuildResult result = gradle(tempDir, "build", "deleteDatabaseTables");
        assertNotNull(result);
        BuildTask deleteDatabaseTablesTask = result.task(":deleteDatabaseTables");
        assertNotNull(deleteDatabaseTablesTask);
        assertSame(SUCCESS, deleteDatabaseTablesTask.getOutcome());
        String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("DataNucleus SchemaTool : Deletion of the schema for classes"));
        assertTrue(output.contains("DataNucleus SchemaTool completed successfully"));
    }

    @Test
    @DisplayName("should succeed running the 'deleteDatabaseTables' task from CLI")
    void test_run_deleteDatabaseTables_task_cli_succeeds(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
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
                "deleteDatabaseTables",
                "--api", "JPA",
                "--persistence-unit-name", "myPersistenceUnit",
                "--jdk-log-conf", TestUtils.getAbsolutePath(jdkLogConfFile),
                "--log4j-conf", TestUtils.getAbsolutePath(log4jConfFile));
        assertNotNull(result);
        BuildTask deleteDatabaseTablesTask = result.task(":deleteDatabaseTables");
        assertNotNull(deleteDatabaseTablesTask);
        assertSame(SUCCESS, deleteDatabaseTablesTask.getOutcome());
    }
}
