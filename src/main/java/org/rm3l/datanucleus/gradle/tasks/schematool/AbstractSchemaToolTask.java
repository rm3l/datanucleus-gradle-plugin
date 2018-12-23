package org.rm3l.datanucleus.gradle.tasks.schematool;

import org.datanucleus.store.schema.SchemaTool;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.Optional;
import org.rm3l.datanucleus.gradle.DataNucleusApi;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.datanucleus.store.schema.SchemaTool.*;

@SuppressWarnings("unused")
public abstract class AbstractSchemaToolTask  extends DefaultTask {

    final Property<Boolean> skip;
    final Property<String> persistenceUnitName;
    final Property<File> log4jConfiguration;
    final Property<File> jdkLogConfiguration;
    final Property<DataNucleusApi> api;
    final Property<Boolean> verbose;
    final Property<Boolean> fork;
    final Property<Boolean> ignoreMetaDataForMissingClasses;
    final Property<String> catalogName;
    final Property<String> schemaName;
    final Property<Boolean> completeDdl;
    final Property<File> ddlFile;

    AbstractSchemaToolTask() {
        final ObjectFactory objects = getProject().getObjects();
        skip = objects.property(Boolean.class);
        persistenceUnitName = objects.property(String.class);
        log4jConfiguration = objects.property(File.class);
        jdkLogConfiguration = objects.property(File.class);
        api = objects.property(DataNucleusApi.class);
        verbose = objects.property(Boolean.class);
        fork = objects.property(Boolean.class);
        ddlFile = objects.property(File.class);
        ignoreMetaDataForMissingClasses = objects.property(Boolean.class);
        completeDdl = objects.property(Boolean.class);
        catalogName = objects.property(String.class);
        schemaName = objects.property(String.class);
    }

    @Input
    @Optional
    public Property<Boolean> getSkip() {
        return skip;
    }

    @Input
    public Property<String> getPersistenceUnitName() {
        return persistenceUnitName;
    }

    @Input
    @Optional
    public Property<File> getLog4jConfiguration() {
        return log4jConfiguration;
    }

    @Input
    @Optional
    public Property<File> getJdkLogConfiguration() {
        return jdkLogConfiguration;
    }

    @Input
    public Property<DataNucleusApi> getApi() {
        return api;
    }

    @Input
    @Optional
    public Property<Boolean> getVerbose() {
        return verbose;
    }

    @Input
    @Optional
    public Property<Boolean> getFork() {
        return fork;
    }

    @Input
    @Optional
    public Property<Boolean> getIgnoreMetaDataForMissingClasses() {
        return ignoreMetaDataForMissingClasses;
    }

    @Input
    @Optional
    public Property<Boolean> getCompleteDdl() {
        return completeDdl;
    }


    @Input
    @Optional
    public Property<File> getDdlFile() {
        return ddlFile;
    }


    @Input
    @Optional
    public Property<String> getCatalogName() {
        return catalogName;
    }


    @Input
    @Optional
    public Property<String> getSchemaName() {
        return schemaName;
    }

    @TaskAction
    public final void performSchemaToolOperation() throws Exception {

        final Project project = getProject();
        final Logger projectLogger = project.getLogger();

        final Boolean shouldSkip = skip.get();
        if (shouldSkip != null && shouldSkip) {
            if (projectLogger.isDebugEnabled()) {
                projectLogger.debug("SchemaTool Task Execution skipped as requested");
            }
        } else {
            final JavaPluginConvention javaConvention =
                    project.getConvention().getPlugin(JavaPluginConvention.class);
            final SourceSet main = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
            final SourceSetOutput mainOutput = main.getOutput();

            final SourceSet test = javaConvention.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);
            final SourceSetOutput testOutput = main.getOutput();

            final Stream<File> mainStream = Stream.concat(
                    Stream.concat(
                            mainOutput.getClassesDirs().getFiles().stream(),
                            Stream.of(mainOutput.getResourcesDir())
                    ),
                    main.getResources().getSrcDirs().stream()
            );
            final Stream<File> testStream = Stream.concat(
                    Stream.concat(
                            testOutput.getClassesDirs().getFiles().stream(),
                            Stream.of(testOutput.getResourcesDir())
                    ),
                    test.getResources().getSrcDirs().stream()
            );

            final Stream<File> fileStream = Stream.concat(
                    Stream.concat(mainStream, testStream),
                    Stream.concat(
                            main.getCompileClasspath().getFiles().stream(),
                            test.getCompileClasspath().getFiles().stream()
                    )
                            .filter(file -> !file.getName().startsWith("datanucleus-core"))
                            .filter(file -> !file.getName().startsWith("datanucleus-api-jpa"))
            );
            final List<String> sourcePathList = fileStream.map(File::getAbsolutePath).collect(Collectors.toList());
            final URL[] classloaderUrls = new URL[sourcePathList.size()];
            int index = 0;
            for (final String sourcePath : sourcePathList) {
                classloaderUrls[index++] = new File(sourcePath + "/").toURI().toURL();
            }
            final URLClassLoader classLoader = new URLClassLoader(classloaderUrls,
                    Thread.currentThread().getContextClassLoader());

            final List<String> args = new ArrayList<>(Arrays.asList(this.withSchemaToolArguments()));
            if (this.api.isPresent()) {
                args.addAll(Arrays.asList("-" + OPTION_API, this.api.get().name()));
            }
            if (this.ddlFile.isPresent()) {
                args.addAll(Arrays.asList("-" + OPTION_DDL_FILE, this.ddlFile.get().getAbsolutePath()));
            }
            if (this.completeDdl.isPresent()) {
                args.addAll(Arrays.asList("-" + OPTION_COMPLETE_DDL, this.completeDdl.get().toString()));
            }
            if (this.completeDdl.isPresent()) {
                args.addAll(Arrays.asList("-" + OPTION_COMPLETE_DDL, this.completeDdl.get().toString()));
            }
            if (this.ignoreMetaDataForMissingClasses.isPresent()) {
                args.addAll(Arrays.asList("-ignoreMetaDataForMissingClasses", this.ignoreMetaDataForMissingClasses.get().toString()));
            }
            if (this.persistenceUnitName.isPresent()) {
                args.addAll(Arrays.asList("-pu", this.persistenceUnitName.get()));
            }

            final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(classLoader);
                SchemaTool.main(args.toArray(new String[0]));
            } finally {
                Thread.currentThread().setContextClassLoader(originalClassLoader);
            }
        }
    }

    protected abstract String[] withSchemaToolArguments();
}
