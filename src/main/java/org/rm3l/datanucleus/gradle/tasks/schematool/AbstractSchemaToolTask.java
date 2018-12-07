package org.rm3l.datanucleus.gradle.tasks.schematool;

import org.datanucleus.PersistenceNucleusContext;
import org.datanucleus.store.schema.SchemaAwareStoreManager;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.rm3l.datanucleus.gradle.DataNucleusApi;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.File;
import java.util.Properties;

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
    public final void performSchemaToolOperation() {

        final Project project = getProject();
        final Logger projectLogger = project.getLogger();

        final Boolean shouldSkip = skip.get();
        if (shouldSkip != null && shouldSkip) {
            if (projectLogger.isDebugEnabled()) {
                projectLogger.debug("SchemaTool Task Execution skipped as requested");
            }
        } else {
            final EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName.get());
            final PersistenceNucleusContext persistenceNucleusContext = entityManagerFactory.unwrap(PersistenceNucleusContext.class);

            final SchemaAwareStoreManager storeManager = (SchemaAwareStoreManager) persistenceNucleusContext.getStoreManager();

            final Properties properties = new Properties();
            // TODO Set any properties for schema generation

            this.doExecuteSchemaToolOperation(storeManager, properties);
        }
    }

    abstract void doExecuteSchemaToolOperation(SchemaAwareStoreManager storeManager, Properties properties);
}
