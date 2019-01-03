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
import org.gradle.api.tasks.options.Option;
import org.gradle.api.tasks.options.OptionValues;
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

    private Boolean skip;
    private String persistenceUnitName;
    private File log4jConfiguration;
    private File jdkLogConfiguration;
    private DataNucleusApi api;
    private Boolean verbose;
    private Boolean ignoreMetaDataForMissingClasses;
    private String catalogName;
    private String schemaName;
    private Boolean completeDdl;
    private File ddlFile;

    public AbstractSchemaToolTask() {
        //Instruct Gradle to always run this task on demand, bypassing the task cache
        super.getOutputs().upToDateWhen(element -> false);
    }

    @Input
    @Optional
    public Boolean getSkip() {
        return skip;
    }

    @Option(option = "skip", description = "Whether to skip execution")
    public void setSkip(Boolean skip) {
        this.skip = skip;
    }

    @Input
    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    @Option(option = "persistence-unit-name", description = "Name of the persistence-unit to enhance. Mandatory")
    public void setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    @InputFile
    @Optional
    public File getLog4jConfiguration() {
        return log4jConfiguration;
    }

    @Option(option = "log4j-conf", description = "Config file location for Log4J (if using it)")
    public void setLog4jConfiguration(String log4jConfiguration) {
        this.setLog4jConfiguration(
                java.util.Optional.ofNullable(log4jConfiguration).map(File::new).orElse(null));
    }

    public void setLog4jConfiguration(File log4jConfiguration) {
        this.log4jConfiguration = log4jConfiguration;
    }

    @InputFile
    @Optional
    public File getJdkLogConfiguration() {
        return jdkLogConfiguration;
    }

    @Option(option = "jdk-log-conf", description = "Config file location for JDK logging (if using it)")
    public void setJdkLogConfiguration(String jdkLogConfiguration) {
        this.setJdkLogConfiguration(
                java.util.Optional.ofNullable(jdkLogConfiguration).map(File::new).orElse(null));
    }

    public void setJdkLogConfiguration(File jdkLogConfiguration) {
        this.jdkLogConfiguration = jdkLogConfiguration;
    }

    @Input
    public DataNucleusApi getApi() {
        return api;
    }

    @Option(option = "api", description = "API to enhance to (JDO or JPA). Mandatory. Default is JDO.")
    public void setApi(DataNucleusApi api) {
        this.api = api;
    }

    @OptionValues("api")
    public List<DataNucleusApi> getApiTypes() {
        return new ArrayList<>(EnumSet.allOf(DataNucleusApi.class));
    }

    @Input
    @Optional
    public Boolean getVerbose() {
        return verbose;
    }

    @Option(option = "verbose", description = "Whether to be verbose or not")
    public void setVerbose(Boolean verbose) {
        this.verbose = verbose;
    }

    @Input
    @Optional
    public Boolean getIgnoreMetaDataForMissingClasses() {
        return ignoreMetaDataForMissingClasses;
    }

    @Option(option = "ignore-metadata-for-missing-classes",
            description = "Whether to ignore when we have metadata specified for classes that are not found (e.g in orm.xml)")
    public void setIgnoreMetaDataForMissingClasses(Boolean ignoreMetaDataForMissingClasses) {
        this.ignoreMetaDataForMissingClasses = ignoreMetaDataForMissingClasses;
    }

    @Input
    @Optional
    public Boolean getCompleteDdl() {
        return completeDdl;
    }

    @Option(option = "complete-ddl", description = "Whether to consider complete DDL or not")
    public void setCompleteDdl(Boolean completeDdl) {
        this.completeDdl = completeDdl;
    }

    @InputFile
    @Optional
    public File getDdlFile() {
        return ddlFile;
    }

    @Option(option = "ddl-file", description = "Path to DDL file")
    public void setDdlFile(String ddlFile) {
        this.setJdkLogConfiguration(
                java.util.Optional.ofNullable(ddlFile).map(File::new).orElse(null));
    }

    public void setDdlFile(File ddlFile) {
        this.ddlFile = ddlFile;
    }

    @Input
    @Optional
    public String getCatalogName() {
        return catalogName;
    }

    @Option(option = "catalog-name", description = "Catalog Name")
    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @Input
    @Optional
    public String getSchemaName() {
        return schemaName;
    }

    @Option(option = "schema-name", description = "Schema Name")
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    protected void checkTaskOptionsValidity() {}

    @TaskAction
    public final void performSchemaToolOperation() throws Exception {

        final Project project = getProject();
        final Logger projectLogger = project.getLogger();

        final Boolean shouldSkip = skip;
        if (shouldSkip != null && shouldSkip) {
            if (projectLogger.isDebugEnabled()) {
                projectLogger.debug("SchemaTool Task Execution skipped as requested");
            }
        } else {

            this.checkTaskOptionsValidity();

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
            if (this.api != null) {
                args.addAll(Arrays.asList("-" + OPTION_API, this.api.name()));
            }
            if (this.ddlFile != null) {
                args.addAll(Arrays.asList("-" + OPTION_DDL_FILE, this.ddlFile.getAbsolutePath()));
            }
            if (this.completeDdl != null) {
                args.addAll(Arrays.asList("-" + OPTION_COMPLETE_DDL, this.completeDdl.toString()));
            }
            if (this.completeDdl != null) {
                args.addAll(Arrays.asList("-" + OPTION_COMPLETE_DDL, this.completeDdl.toString()));
            }
            if (this.ignoreMetaDataForMissingClasses != null) {
                args.addAll(Arrays.asList("-ignoreMetaDataForMissingClasses", this.ignoreMetaDataForMissingClasses.toString()));
            }
            if (this.persistenceUnitName != null) {
                args.addAll(Arrays.asList("-pu", this.persistenceUnitName));
            }
            if (this.catalogName != null) {
                args.addAll(Arrays.asList("-catalog", this.catalogName));
            }
            if (this.schemaName != null) {
                args.addAll(Arrays.asList("-schema", this.schemaName));
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
