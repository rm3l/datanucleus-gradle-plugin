package org.rm3l.datanucleus.gradle;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.datanucleus.metadata.PersistenceUnitMetaData;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.junitpioneer.jupiter.TempDirectory.TempDir;

import javax.lang.model.element.Modifier;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(TempDirectory.class)
class DataNucleusPluginTest {

    private static final String DOMAIN_PACKAGE_NAME_IN_TEST_PROJECT = "org.rm3l.datanucleus.gradle.test.domain";

    @BeforeEach
    void setUp(@TempDir Path tempDir) throws IOException {
        // Prepare build.gradle
        final Path settingsGradle = tempDir.resolve("settings.gradle");
        Files.write(settingsGradle,
                ("enableFeaturePreview(\"IMPROVED_POM_SUPPORT\")\n").getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        final Path buildGradle = tempDir.resolve("build.gradle");
        Files.write(buildGradle,
                ("plugins { id 'org.rm3l.datanucleus-gradle-plugin' }\n\n" +
                        "repositories {\n" +
                        "  mavenCentral()\n" +
                        "}\n" +
                        "\n" +
                        "dependencies {\n" +
                        "  compile 'org.datanucleus:datanucleus-accessplatform-jpa-rdbms:5.1.11'\n" +
                        "  testCompile 'junit:junit:4.12'\n" +
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

        //Create sample JPA Entity Type
        final Path mainSourceSetDir = tempDir.resolve("src").resolve("main");
        final Path mainJavaSourceSetDir = mainSourceSetDir.resolve("java");
        Files.createDirectories(mainJavaSourceSetDir);

        final TypeSpec person = TypeSpec
                .classBuilder("Person")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Entity.class)
                .addField(
                        FieldSpec.builder(Long.class, "id")
                                .addModifiers(Modifier.PRIVATE)
                                .addAnnotation(Id.class)
                                .build())
                .addField(
                        FieldSpec.builder(String.class, "name")
                                .addAnnotation(AnnotationSpec
                                        .builder(Column.class)
                                        .addMember("nullable", "$L", false)
                                        .build())
                                .addModifiers(Modifier.PRIVATE)
                                .build())
                .build();
        JavaFile.builder(DOMAIN_PACKAGE_NAME_IN_TEST_PROJECT, person)
                .build()
                .writeTo(mainJavaSourceSetDir);

        //Also create a persistence.xml file
        Path metaInfResourcesSet = mainSourceSetDir.resolve("resources").resolve("META-INF");
        Files.createDirectories(metaInfResourcesSet);
        final PersistenceUnitMetaData persistenceUnitMetaData =
                new PersistenceUnitMetaData("myPersistenceUnit", "RESOURCE_LOCAL", null);
        persistenceUnitMetaData.addClassName(DOMAIN_PACKAGE_NAME_IN_TEST_PROJECT + ".Person");
        persistenceUnitMetaData.setExcludeUnlistedClasses(true);
        Files.write(metaInfResourcesSet.resolve("persistence.xml"),
                (("<persistence xmlns=\"http://xmlns.jcp.org/xml/ns/persistence\"\n" +
                        "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "  xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/persistence\n" +
                        "  http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd\"\n" +
                        "  version=\"2.2\">\n\n") +
                        persistenceUnitMetaData.toString("  ", "  ") +
                        "\n\n" +
                        "</persistence>")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Helper method that runs a Gradle task in the testProjectDir
     * @param arguments the task arguments to execute
     * @param isSuccessExpected boolean representing whether or not the build is supposed to fail
     * @return the task's BuildResult
     */
    private BuildResult gradle(Path tempDir, boolean isSuccessExpected, String... arguments) {
        final String[] args;
        if (arguments == null) {
            args = new String[] {"tasks", "--stacktrace"};
        } else {
            args = Arrays.copyOf(arguments, arguments.length + 1);
            args[args.length - 1] = "--stacktrace";
        }

        final GradleRunner runner = GradleRunner.create()
                .withArguments(args)
                .withProjectDir(tempDir.toFile())
                .withPluginClasspath()
                .forwardOutput()
                .withDebug(true);
        return isSuccessExpected ? runner.build() : runner.buildAndFail();
    }

    private BuildResult gradle(Path tempDir, String... arguments) {
        return gradle(tempDir, true, arguments);
    }

    @Test
    @DisplayName("should enhance domain classes when building the project")
    void test_build_auto_enhances(@TempDir Path tempDir) {
        final BuildResult result = gradle(tempDir, "build");
        assertNotNull(result);
        final BuildTask enhanceTask = result.task(":enhance");
        assertNotNull(enhanceTask);
        assertSame(SUCCESS, enhanceTask.getOutcome());

        final String output = result.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("DataNucleus Enhancer completed with success for 1 classes."));
    }

    @Test
    @DisplayName("should not succeed enhancing domain classes if no build had been performed beforehand")
    void test_enhance_without_build_does_not_succeed(@TempDir Path tempDir) {
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
    void test_enhance_after_build_succeeds(@TempDir Path tempDir) {
        BuildResult result = gradle(tempDir, "build");
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
