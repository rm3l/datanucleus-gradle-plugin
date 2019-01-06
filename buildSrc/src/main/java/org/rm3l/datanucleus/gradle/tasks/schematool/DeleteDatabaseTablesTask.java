package org.rm3l.datanucleus.gradle.tasks.schematool;

import static org.datanucleus.store.schema.SchemaTool.OPTION_DELETE_TABLES_FOR_CLASSES;

public class DeleteDatabaseTablesTask extends AbstractSchemaToolTask {
    @Override
    protected String[] withSchemaToolArguments() {
        return new String[]{
                "-" + OPTION_DELETE_TABLES_FOR_CLASSES
        };
    }

    @Override
    public String getDescription() {
        return "Deletes all database tables required for the classes defined by the input data.";
    }
}
