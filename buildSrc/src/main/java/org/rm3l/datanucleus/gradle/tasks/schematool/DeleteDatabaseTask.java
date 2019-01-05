package org.rm3l.datanucleus.gradle.tasks.schematool;

import static org.datanucleus.store.schema.SchemaTool.OPTION_DELETE_DATABASE;

public class DeleteDatabaseTask extends AbstractSchemaToolTask {
    @Override
    protected String[] withSchemaToolArguments() {
        return new String[] {
                "-" + OPTION_DELETE_DATABASE
        };
    }

    @Override
    protected void checkTaskOptionsValidity() {
        if (this.getSchemaName() == null) {
            throw new IllegalArgumentException("Missing option: schemaName");
        }
        if (this.getCatalogName() == null) {
            throw new IllegalArgumentException("Missing option: catalogName");
        }
    }

    @Override
    public String getDescription() {
        return "Deletes the specified database (catalog.schema) if the datastore supports that operation.";
    }
}
