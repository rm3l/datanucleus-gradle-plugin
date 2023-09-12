package org.rm3l.datanucleus.gradle.extensions.schematool;

import groovy.lang.Closure;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.util.ConfigureUtil;
import org.rm3l.datanucleus.gradle.DataNucleusApi;
import org.rm3l.datanucleus.gradle.extensions.DataNucleusExtension;
import org.rm3l.datanucleus.gradle.tasks.schematool.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused"})
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
    }

    public Boolean getSkip() {
        return skip;
    }

    public SchemaToolExtension skip(Boolean skip) {
        this.skip = skip;
        return this;
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

    @SuppressWarnings("unchecked")
    @Nonnull
    private <T extends AbstractSchemaToolTask> T getOrBuildTask(@Nonnull final TaskContainer projectTasks,
        @Nonnull final String taskName,
        @Nonnull final Class<T> taskType) {
        final Task exitingTask = projectTasks.findByName(taskName);
        if (exitingTask != null) {
            final T exitingTaskCast = (T) exitingTask;
            this.configureTask(exitingTaskCast);
            return exitingTaskCast;
        }
        return projectTasks.create(taskName, taskType, this::configureTask);
    }

    public void configureExtensionAndTasks(final Closure<?> closure) {
        ConfigureUtil.configure(closure, this);

        final TaskContainer projectTasks = datanucleusExtension.getProject().getTasks();

        final List<AbstractSchemaToolTask> schemaToolTasks = new ArrayList<>();
        final Map<String, Class<? extends AbstractSchemaToolTask>> taskTypesByNames = new HashMap<>();
        taskTypesByNames.put(CREATE_DATABASE, CreateDatabaseTask.class);
        taskTypesByNames.put(DELETE_DATABASE, DeleteDatabaseTask.class);
        taskTypesByNames.put(CREATE_DATABASE_TABLES, CreateDatabaseTablesTask.class);
        taskTypesByNames.put(DELETE_DATABASE_TABLES, DeleteDatabaseTablesTask.class);
        taskTypesByNames.put(DELETE_THEN_CREATE_DATABASE_TABLES, DeleteThenCreateDatabaseTablesTask.class);
        taskTypesByNames.put(VALIDATE_DATABASE_TABLES, ValidateDatabaseTablesTask.class);
        taskTypesByNames.put(DBINFO, DBInfoTask.class);
        taskTypesByNames.put(SCHEMAINFO, SchemaInfoTask.class);

        for (final Entry<String, Class<? extends AbstractSchemaToolTask>> taskTypeByNameEntry : taskTypesByNames
            .entrySet()) {
            schemaToolTasks.add(
                getOrBuildTask(projectTasks,
                    taskTypeByNameEntry.getKey(),
                    taskTypeByNameEntry.getValue()));
        }

        for (final AbstractSchemaToolTask enhancementDependentTask : schemaToolTasks) {
            enhancementDependentTask.dependsOn("classes");
        }
    }

    @SuppressWarnings("Duplicates")
    private <T extends AbstractSchemaToolTask> void configureTask(T task) {
        final Boolean schemaToolExtensionSkip = this.getSkip();
        boolean skip = false;
        if (this.datanucleusExtension.getSkip() != null) {
            skip = this.datanucleusExtension.getSkip();
        }
        if (schemaToolExtensionSkip != null) {
            skip = schemaToolExtensionSkip;
        }
        task.setSkip(skip);

        task.setPersistenceUnitName(this.getPersistenceUnitName());
        task.setLog4jConfiguration(this.getLog4jConfiguration());
        task.setJdkLogConfiguration(this.getJdkLogConfiguration());
        task.setApi(this.getApi());
        task.setVerbose(this.isVerbose());
        task.setDdlFile(this.getDdlFile());
        task.setPersistenceUnitName(this.getPersistenceUnitName());
        task.setCatalogName(this.getCatalogName());
        task.setSchemaName(this.getSchemaName());
        task.setCompleteDdl(this.isCompleteDdl());
        task.setIgnoreMetaDataForMissingClasses(this.isIgnoreMetaDataForMissingClasses());
    }

}
