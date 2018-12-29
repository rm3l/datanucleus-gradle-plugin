package org.rm3l.datanucleus.gradle.extensions.schematool;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.util.ConfigureUtil;
import org.rm3l.datanucleus.gradle.DataNucleusApi;
import org.rm3l.datanucleus.gradle.extensions.DataNucleusExtension;
import org.rm3l.datanucleus.gradle.tasks.schematool.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SchemaToolExtension {

    public static final String CREATE_DATABASE = "createDatabase";
    public static final String DELETE_DATABASE = "deleteDatabase";
    public static final String CREATE_DATABASE_TABLES = "createDatabaseTables";
    public static final String DELETE_DATABASE_TABLES = "deleteDatabaseTables";
    public static final String DELETE_THEN_CREATE_DATABASE_TABLES = "deleteThenCreateDatabaseTables";
    public static final String VALIDATE_DATABASE_TABLES = "validateDatabaseTables";
    public static final String DBINFO = "dbinfo";
    public static final String SCHEMAINFO = "schemainfo";
    private final DataNucleusExtension datanucleusExtension;
    private final Project project;
    private final SourceSet sourceSet;

    /**
     * API for the metadata being used (JDO, JPA). Set this to JPA
     */
    private DataNucleusApi api = DataNucleusApi.JDO;

    /**
     * Whether to ignore when we have metadata specified for classes that aren’t found
     */
    private boolean ignoreMetaDataForMissingClasses = false;

    /**
     * Name of the catalog (mandatory when using createDatabase or deleteDatabase options)
     */
    private String catalogName;

    /**
     * Name of the schema (mandatory when using createDatabase or deleteDatabase options)
     */
    private String schemaName;

    /**
     * Name of the persistence-unit to generate the schema for
     * (defines the classes and the properties defining the datastore). Mandatory
     */
    private String persistenceUnitName;

    /**
     * Config file location for Log4J (if using it)
     */
    private File log4jConfiguration;

    /**
     * Config file location for java.util.logging (if using it)
     */
    private File jdkLogConfiguration;

    /**
     * Verbose output?
     */
    private boolean verbose = false;

    /**
     * Whether to fork the SchemaTool process.
     * Note that if you don’t fork the process, DataNucleus will likely struggle to determine class names from the
     * input filenames, so you need to use a persistence.xml file defining the class names directly.
     */
    private boolean fork = true;

    /**
     * Whether to generate DDL including things that already exist? (for RDBMS)
     */
    private boolean completeDdl = false;

    /**
     * Name of an output file to dump any DDL to (for RDBMS)
     */
    private File ddlFile;

    private Boolean skip = null;

    public SchemaToolExtension(DataNucleusExtension dataNucleusExtension) {
        this.datanucleusExtension = dataNucleusExtension;
        this.project = dataNucleusExtension.getProject();
        this.skip(skip);
        final JavaPluginConvention javaConvention =
                project.getConvention().getPlugin(JavaPluginConvention.class);
        this.sourceSet = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
    }

    public Boolean getSkip() {
        return skip;
    }

    public SchemaToolExtension skip(Boolean skip) {
        this.skip = skip;
        return this;
    }

    public SourceSet getSourceSet() {
        return this.sourceSet;
    }

    public DataNucleusApi getApi() {
        return api;
    }

    public SchemaToolExtension api(DataNucleusApi api) {
        this.api = api;
        return this;
    }

    public boolean isIgnoreMetaDataForMissingClasses() {
        return ignoreMetaDataForMissingClasses;
    }

    public SchemaToolExtension ignoreMetaDataForMissingClasses(boolean ignoreMetaDataForMissingClasses) {
        this.ignoreMetaDataForMissingClasses = ignoreMetaDataForMissingClasses;
        return this;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public SchemaToolExtension catalogName(String catalogName) {
        this.catalogName = catalogName;
        return this;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public SchemaToolExtension schemaName(String schemaName) {
        this.schemaName = schemaName;
        return this;
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public SchemaToolExtension persistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
        return this;
    }

    public File getLog4jConfiguration() {
        return log4jConfiguration;
    }

    public SchemaToolExtension log4jConfiguration(String log4jConfiguration) {
        if (log4jConfiguration != null) {
            this.log4jConfiguration = new File(log4jConfiguration);
        } else {
            this.log4jConfiguration = null;
        }
        return this;
    }

    public File getJdkLogConfiguration() {
        return jdkLogConfiguration;
    }

    public SchemaToolExtension jdkLogConfiguration(String jdkLogConfiguration) {
        if (jdkLogConfiguration != null) {
            this.jdkLogConfiguration = new File(jdkLogConfiguration);
        } else {
            this.jdkLogConfiguration = null;
        }
        return this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public SchemaToolExtension verbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public boolean isFork() {
        return fork;
    }

    public SchemaToolExtension fork(boolean fork) {
        this.fork = fork;
        return this;
    }

    public boolean isCompleteDdl() {
        return completeDdl;
    }

    public SchemaToolExtension completeDdl(boolean completeDdl) {
        this.completeDdl = completeDdl;
        return this;
    }

    public File getDdlFile() {
        return ddlFile;
    }

    public SchemaToolExtension ddlFile(String ddlFile) {
        if (ddlFile != null) {
            this.ddlFile = new File(ddlFile);
        } else {
            this.ddlFile = null;
        }
        return this;
    }

    public void configureExtensionAndTasks(final Closure closure) {
        ConfigureUtil.configure(closure, this);

        final TaskContainer projectTasks = datanucleusExtension.getProject().getTasks();
        final List<AbstractSchemaToolTask> schemaToolTasks = new ArrayList<>();

        schemaToolTasks.add(projectTasks.create(CREATE_DATABASE, CreateDatabaseTask.class, this::configureTask));
        schemaToolTasks.add(projectTasks.create(DELETE_DATABASE, DeleteDatabaseTask.class, this::configureTask));
        schemaToolTasks.add(projectTasks.create(CREATE_DATABASE_TABLES, CreateDatabaseTablesTask.class, this::configureTask));
        schemaToolTasks.add(projectTasks.create(DELETE_DATABASE_TABLES, DeleteDatabaseTablesTask.class, this::configureTask));
        schemaToolTasks.add(projectTasks.create(DELETE_THEN_CREATE_DATABASE_TABLES, DeleteThenCreateDatabaseTablesTask.class, this::configureTask));
        schemaToolTasks.add(projectTasks.create(VALIDATE_DATABASE_TABLES, ValidateDatabaseTablesTask.class, this::configureTask));
        schemaToolTasks.add(projectTasks.create(DBINFO, DBInfoTask.class, this::configureTask));
        schemaToolTasks.add(projectTasks.create(SCHEMAINFO, SchemaInfoTask.class, this::configureTask));

        for (final AbstractSchemaToolTask enhancementDependentTask : schemaToolTasks) {
            enhancementDependentTask.dependsOn("classes");
        }
    }

    private void configureTask(AbstractSchemaToolTask task) {
        final Boolean schemaToolExtensionSkip = this.getSkip();
        final Property<Boolean> taskSkip = task.getSkip();
        boolean skip = false;
        if (this.datanucleusExtension.getSkip() != null) {
            skip = this.datanucleusExtension.getSkip();
        }
        if (schemaToolExtensionSkip != null) {
            skip = schemaToolExtensionSkip;
        }
        taskSkip.set(skip);
        task.getPersistenceUnitName().set(this.getPersistenceUnitName());
        task.getLog4jConfiguration().set(this.getLog4jConfiguration());
        task.getJdkLogConfiguration().set(this.getJdkLogConfiguration());
        task.getApi().set(this.getApi());
        task.getVerbose().set(this.isVerbose());
        task.getFork().set(this.isFork());

        final File ddlFile = this.getDdlFile();
        final Property<File> taskDdlFile = task.getDdlFile();
        if (ddlFile != null) {
            taskDdlFile.set(ddlFile);
        }
        task.getPersistenceUnitName().set(this.getPersistenceUnitName());
        task.getCatalogName().set(this.getCatalogName());
        task.getSchemaName().set(this.getSchemaName());

        task.getCompleteDdl().set(this.isCompleteDdl());
        task.getIgnoreMetaDataForMissingClasses()
                .set(this.isIgnoreMetaDataForMissingClasses());
    }

}
