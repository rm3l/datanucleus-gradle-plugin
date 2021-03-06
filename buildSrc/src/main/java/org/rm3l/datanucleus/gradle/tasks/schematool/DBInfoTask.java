package org.rm3l.datanucleus.gradle.tasks.schematool;

import static org.datanucleus.store.schema.SchemaTool.OPTION_DBINFO;

public class DBInfoTask extends AbstractSchemaToolTask {
    @Override
    protected String[] withSchemaToolArguments() {
        return new String[]{
                "-" + OPTION_DBINFO
        };
    }

    @Override
    public String getDescription() {
        return "Provides detailed information about the database, it’s limits and datatypes support. Only for RDBMS currently.";
    }
}
