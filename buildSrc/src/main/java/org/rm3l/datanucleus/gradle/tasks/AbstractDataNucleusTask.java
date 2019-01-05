package org.rm3l.datanucleus.gradle.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;

/**
 * Root of all DataNucleus tasks
 */
public abstract class AbstractDataNucleusTask extends DefaultTask {

    @Override
    @Internal
    public abstract String getDescription();

    @SuppressWarnings("NullableProblems")
    @Override
    public final void setDescription(String description) {
        throw new UnsupportedOperationException(
                "Description cannot be overridden. Override #getDescription() method instead");
    }
}
