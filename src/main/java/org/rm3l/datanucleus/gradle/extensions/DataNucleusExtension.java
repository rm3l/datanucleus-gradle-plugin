package org.rm3l.datanucleus.gradle.extensions;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.util.ConfigureUtil;
import org.rm3l.datanucleus.gradle.tasks.EnhanceTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DataNucleusExtension {

    private final Project project;

    /**
     * The source sets that hold persistent model.  Default is project.sourceSets.main
     */
    private List<SourceSet> sourceSets;

    private final SourceSet mainSourceSet;

    /**
     * Configuration for bytecode enhancement.  Private; see instead {@link #enhance(groovy.lang.Closure)}
     */
    private final EnhanceExtension enhance;

    public DataNucleusExtension(Project project) {
        this.project = project;
        this.sourceSets = new ArrayList<>();
        final JavaPluginConvention javaConvention =
                project.getConvention().getPlugin(JavaPluginConvention.class);
        this.mainSourceSet = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        this.sourceSets.add(this.mainSourceSet);
        this.enhance = new EnhanceExtension();
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

    private void enhance(Closure closure) {
        ConfigureUtil.configure(closure, enhance);
        project.getTasks().create("enhance", EnhanceTask.class,
                task -> {
                    task.getPersistenceUnitName().set(enhance.getPersistenceUnitName());
                    task.getLog4jConfiguration().set(enhance.getLog4jConfiguration());
                    task.getJdkLogConfiguration().set(enhance.getJdkLogConfiguration());
                    task.getApi().set(enhance.getApi());
                    task.getVerbose().set(enhance.isVerbose());
                    task.getQuiet().set(enhance.isQuiet());
                    final File targetDirectory = enhance.getTargetDirectory();
                    task.getTargetDirectory().set(targetDirectory != null? targetDirectory :
                            mainSourceSet.getOutput().getClassesDirs().getFiles().iterator().next());
                    task.getFork().set(enhance.isFork());
                    task.getGeneratePK().set(enhance.isGeneratePK());
                    task.getPersistenceUnitName().set(enhance.getPersistenceUnitName());
                    task.getGenerateConstructor().set(enhance.isGenerateConstructor());
                    task.getDetachListener().set(enhance.isDetachListener());
                    task.getIgnoreMetaDataForMissingClasses()
                            .set(enhance.isIgnoreMetaDataForMissingClasses());
                });
    }
}
