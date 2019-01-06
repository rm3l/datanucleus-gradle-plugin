package org.rm3l.datanucleus.gradle.tasks.schematool;

import static org.datanucleus.store.schema.SchemaTool.OPTION_SCHEMAINFO;

public class SchemaInfoTask extends AbstractSchemaToolTask {
    @Override
    protected String[] withSchemaToolArguments() {
        return new String[] {
                "-" + OPTION_SCHEMAINFO
        };
    }

    @Override
    public String getDescription() {
        return "Provides detailed information about the database schema. Only for RDBMS currently.";
    }
}
