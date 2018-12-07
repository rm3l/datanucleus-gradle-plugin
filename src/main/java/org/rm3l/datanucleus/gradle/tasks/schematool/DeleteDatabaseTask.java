package org.rm3l.datanucleus.gradle.tasks.schematool;

import org.datanucleus.store.schema.SchemaAwareStoreManager;

import java.util.Properties;

public class DeleteDatabaseTask extends AbstractSchemaToolTask {
    @Override
    void doExecuteSchemaToolOperation(SchemaAwareStoreManager storeManager, Properties properties) {
        storeManager.deleteDatabase(catalogName.get(), schemaName.get(), properties);
    }
}
