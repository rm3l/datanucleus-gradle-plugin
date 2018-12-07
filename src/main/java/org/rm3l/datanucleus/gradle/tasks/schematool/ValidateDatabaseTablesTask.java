package org.rm3l.datanucleus.gradle.tasks.schematool;

import org.datanucleus.store.schema.SchemaAwareStoreManager;

import java.util.Properties;

public class ValidateDatabaseTablesTask extends AbstractSchemaToolTask {
    @Override
    void doExecuteSchemaToolOperation(SchemaAwareStoreManager storeManager, Properties properties) {
//        storeManager.validateSchemaForClasses();
    }
}
