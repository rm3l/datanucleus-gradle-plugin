package org.rm3l.datanucleus.gradle.tasks.schematool;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.rm3l.datanucleus.gradle.utils.DataNucleusPluginTestExtension;
import org.rm3l.datanucleus.gradle.utils.ExpectedSystemExit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.rm3l.datanucleus.gradle.utils.TestUtils.*;

@ExpectedSystemExit
class ValidateDatabaseTablesTaskTest {

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
    @DisplayName("should succeed creating the database against an in-memory datastore")
    @Disabled("Disabled due to classloader issues with DN")
    void test_schematool_CreateDatabase_does_succeed(@DataNucleusPluginTestExtension.TempDir Path tempDir) throws IOException {
        final Path buildGradle = tempDir.resolve("build.gradle");
        Files.write(buildGradle,
                ("plugins { id 'org.rm3l.datanucleus-gradle-plugin' }\n\n" +
                        "repositories {\n" +
                        "  mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "  compile 'org.datanucleus:datanucleus-accessplatform-jpa-rdbms:" + DN_JPA_RDBMS_VERSION + "'\n" +
                        "  compile 'com.h2database:h2:" + H2_VERSION + "'\n" +
                        "  testCompile 'junit:junit:" + JUNIT_VERSION + "'\n" +
                        "}\n" +
                        "\n" +
                        "datanucleus {\n" +
//                        "  enhance {\n" +
//                        "    api 'JPA'\n" +
//                        "    persistenceUnitName 'myPersistenceUnitForTest'\n" +
//                        "  }\n" +
                        "  schemaTool {\n" +
                        "    api 'JPA'\n" +
                        "    persistenceUnitName 'myPersistenceUnit'\n" +
                        "  }\n" +
                        "}\n")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        //This does not make the build fail. Instead, a stacktrace is output by DataNucleus Enhancer
        BuildResult result = gradle(tempDir, "build", "enhance", "validate");
        assertNotNull(result);
        BuildTask createDatabaseTask = result.task(":createDatabase");
        assertNotNull(createDatabaseTask);
        assertSame(SUCCESS, createDatabaseTask.getOutcome());
//        String output = result.getOutput();
//        assertNotNull(output);
//        assertTrue(output.contains("DataNucleus Enhancer completed with success for 1 classes."));
//
//        result = gradle(tempDir, "enhanceCheck");
//        assertNotNull(result);
//        createDatabaseTask = result.task(":enhanceCheck");
//        assertNotNull(createDatabaseTask);
//        assertSame(SUCCESS, createDatabaseTask.getOutcome());
//        output = result.getOutput();
//        assertNotNull(output);
//        assertTrue(output.contains("DataNucleus Enhancer completed with success for 1 classes."));

    }
}
