package org.rm3l.datanucleus.gradle.tasks.schematool;

import static org.datanucleus.store.schema.SchemaTool.OPTION_CREATE_TABLES_FOR_CLASSES;

public class CreateDatabaseTablesTask extends AbstractSchemaToolTask {

    @Override
    protected String[] withSchemaToolArguments() {
        return new String[] {
            "-" + OPTION_CREATE_TABLES_FOR_CLASSES
        };
    }

    @Override
    public String getDescription() {
        return "Creates all database tables required for the classes defined by the input data.";
    }
}
