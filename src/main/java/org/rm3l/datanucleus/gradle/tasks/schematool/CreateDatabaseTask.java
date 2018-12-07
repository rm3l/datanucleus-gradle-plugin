package org.rm3l.datanucleus.gradle.tasks.schematool;

import org.datanucleus.store.schema.SchemaAwareStoreManager;

import java.util.Properties;

public class CreateDatabaseTask extends AbstractSchemaToolTask  {
    @Override
    void doExecuteSchemaToolOperation(SchemaAwareStoreManager storeManager, Properties properties) {
        storeManager.createDatabase(catalogName.get(), schemaName.get(), properties);
    }
}
