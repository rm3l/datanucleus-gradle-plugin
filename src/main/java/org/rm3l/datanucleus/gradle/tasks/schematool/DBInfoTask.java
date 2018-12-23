package org.rm3l.datanucleus.gradle.tasks.schematool;

import org.datanucleus.store.schema.SchemaAwareStoreManager;

import java.util.Properties;

import static org.datanucleus.store.schema.SchemaTool.OPTION_CREATE_DATABASE;

public class DBInfoTask extends AbstractSchemaToolTask  {
    @Override
    protected String[] withSchemaToolArguments() {
        return new String[] {
                "-" + OPTION_CREATE_DATABASE
        };
    }
}
