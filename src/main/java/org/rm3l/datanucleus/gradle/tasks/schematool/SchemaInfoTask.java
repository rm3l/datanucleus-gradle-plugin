package org.rm3l.datanucleus.gradle.tasks.schematool;

import org.datanucleus.store.schema.SchemaAwareStoreManager;

import java.util.Properties;

import static org.datanucleus.store.schema.SchemaTool.OPTION_DELETE_CREATE_TABLES_FOR_CLASSES;
import static org.datanucleus.store.schema.SchemaTool.OPTION_SCHEMAINFO;

public class SchemaInfoTask extends AbstractSchemaToolTask {
    @Override
    protected String[] withSchemaToolArguments() {
        return new String[] {
                "-" + OPTION_SCHEMAINFO
        };
    }
}
