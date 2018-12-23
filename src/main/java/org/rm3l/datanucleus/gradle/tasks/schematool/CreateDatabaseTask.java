package org.rm3l.datanucleus.gradle.tasks.schematool;

import static org.datanucleus.store.schema.SchemaTool.OPTION_CREATE_DATABASE;

public class CreateDatabaseTask extends AbstractSchemaToolTask  {

    @Override
    protected String[] withSchemaToolArguments() {
        return new String[] {
                "-" + OPTION_CREATE_DATABASE
        };
    }
}
