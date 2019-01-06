package org.rm3l.datanucleus.gradle.utils;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.datanucleus.metadata.PersistenceUnitMetaData;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;

import javax.lang.model.element.Modifier;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.IOException;
import java.lang.annotation.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static org.rm3l.datanucleus.gradle.utils.TestUtils.DOMAIN_PACKAGE_NAME_IN_TEST_PROJECT;

public class DataNucleusPluginTestExtension implements ParameterResolver, BeforeEachCallback, AfterEachCallback {

    private static final String KEY = "temp_dir";
    private final PersistenceUnitMetadataProvider persistenceUnitMetadataProvider;

    public DataNucleusPluginTestExtension() {
        this(null);
    }

    public DataNucleusPluginTestExtension(final PersistenceUnitMetadataProvider persistenceUnitMetadataProvider) {
        this.persistenceUnitMetadataProvider = persistenceUnitMetadataProvider;
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        System.out.println("--> Running test: " + extensionContext.getDisplayName() + "...");

        final Path tempDir = getOrComputeTempDirIfAbsent(extensionContext);

        // Prepare build.gradle
        final Path settingsGradle = tempDir.resolve("settings.gradle");
        Files.write(settingsGradle,
                ("rootProject.name = '" +
                        extensionContext.getRequiredTestClass() + "#" +
                        extensionContext.getRequiredTestMethod() + "'\n" +
                        "enableFeaturePreview(\"IMPROVED_POM_SUPPORT\")\n").getBytes(StandardCharsets.UTF_8),
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
        persistenceUnitMetaData.setProvider(org.datanucleus.api.jpa.PersistenceProviderImpl.class.getCanonicalName());
        persistenceUnitMetaData.setExcludeUnlistedClasses(true);

        //Also create a persistence.xml file
        final Path testSourceSetDir = tempDir.resolve("src").resolve("test");
        final Path testJavaSourceSetDir = testSourceSetDir.resolve("java");
        Files.createDirectories(testJavaSourceSetDir);

        Path metaInfTestResourcesSet = testSourceSetDir.resolve("resources").resolve("META-INF");
        Files.createDirectories(metaInfTestResourcesSet);
        final PersistenceUnitMetaData testPersistenceUnitMetaData =
                new PersistenceUnitMetaData("myPersistenceUnitForTest", "RESOURCE_LOCAL", null);
        testPersistenceUnitMetaData.addClassName(DOMAIN_PACKAGE_NAME_IN_TEST_PROJECT + ".Person");
        testPersistenceUnitMetaData.setExcludeUnlistedClasses(true);
        testPersistenceUnitMetaData.setProvider(org.datanucleus.api.jpa.PersistenceProviderImpl.class.getCanonicalName());


        if (this.persistenceUnitMetadataProvider != null) {
            this.persistenceUnitMetadataProvider
                    .customizePersistenceUnitMetadata(persistenceUnitMetaData, testPersistenceUnitMetaData);
        }

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

        Files.write(metaInfTestResourcesSet.resolve("persistence.xml"),
                (("<persistence xmlns=\"http://xmlns.jcp.org/xml/ns/persistence\"\n" +
                        "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "  xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/persistence\n" +
                        "  http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd\"\n" +
                        "  version=\"2.2\">\n\n") +
                        testPersistenceUnitMetaData.toString("  ", "  ") +
                        "\n\n" +
                        "</persistence>")
                        .getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws IOException {
        System.out.println("--> ... Done running test: " + extensionContext.getDisplayName());

        final Path tempDir = extensionContext.getStore(Namespace.create(DataNucleusPluginTestExtension.class,
                extensionContext.getRequiredTestClass(),
                extensionContext.getRequiredTestMethod())) //
                .get(KEY, Path.class);
        if (tempDir != null) {
            Files.walkFileTree(tempDir, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        return parameterContext.isAnnotated(TempDir.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> parameterType = parameterContext.getParameter().getType();
        if (parameterType != Path.class) {
            throw new ParameterResolutionException(
                    "Can only resolve parameter of type " + Path.class.getName() + " but was: " + parameterType.getName());
        }
        return getOrComputeTempDirIfAbsent(extensionContext);
    }

    private Path getOrComputeTempDirIfAbsent(ExtensionContext extensionContext) {
        return extensionContext.getStore(Namespace.create(DataNucleusPluginTestExtension.class,
                extensionContext.getRequiredTestClass(),
                extensionContext.getRequiredTestMethod())) //
                .getOrComputeIfAbsent(KEY,
                        key -> {
                            try {
                                return Files.createTempDirectory(DataNucleusPluginTestExtension.class.getSimpleName());
                            } catch (IOException e) {
                                throw new IllegalStateException(e);
                            }
                        },
                        Path.class);
    }

    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface TempDir {
    }

    @FunctionalInterface
    public interface PersistenceUnitMetadataProvider {
        void customizePersistenceUnitMetadata(PersistenceUnitMetaData persistenceUnitMetaData,
                                              PersistenceUnitMetaData testPersistenceUnitMetaData);
    }

}
