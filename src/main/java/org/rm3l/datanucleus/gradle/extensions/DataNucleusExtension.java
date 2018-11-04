package org.rm3l.datanucleus.gradle.extensions;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.util.ConfigureUtil;

import java.util.ArrayList;
import java.util.List;

public class DataNucleusExtension {

    private final Project project;

    /**
     * The source sets that hold persistent model.  Default is project.sourceSets.main
     */
    private List<SourceSet> sourceSets;

    /**
     * Configuration for bytecode enhancement.  Private; see instead {@link #enhance(groovy.lang.Closure)}
     */
    private EnhanceExtension enhance;

    public DataNucleusExtension(Project project) {
        this.project = project;
        this.sourceSets = new ArrayList<>();
        final JavaPluginConvention javaConvention =
                project.getConvention().getPlugin(JavaPluginConvention.class);
        this.sourceSets.add(javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME));
    }

    /**
     * Add a single SourceSet.
     *
     * @param sourceSet The SourceSet to add
     */
    public void setSourceSet(SourceSet sourceSet) {
        if (sourceSets == null) {
            sourceSets = new ArrayList<>();
        }
        sourceSets.add(sourceSet);
    }

    public void enhance(Closure closure) {
        enhance = new EnhanceExtension();
        ConfigureUtil.configure(closure, enhance);
    }

    public EnhanceExtension getEnhance() {
        return enhance;
    }
}
