//MIT License
//
//Copyright (c) 2018 Armel Soro
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in all
//copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//SOFTWARE.

package org.rm3l.datanucleus.gradle.extensions;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.util.ConfigureUtil;
import org.rm3l.datanucleus.gradle.tasks.EnhanceTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Extension for the 'datanucleus' DSL entrypoint
 */
@SuppressWarnings("unused")
public class DataNucleusExtension {

    private static final String ENHANCE_TASK_NAME = "enhance";

    private final Project project;

    /**
     * The source sets that hold persistent model.  Default is project.sourceSets.main
     */
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<SourceSet> sourceSets;

    private final SourceSet mainSourceSet;

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

    //Auto-bind the DSL to a Gradle task
    private void enhance(Closure closure) {
        ConfigureUtil.configure(closure, enhance);

        final TaskContainer projectTasks = project.getTasks();
        projectTasks.create(ENHANCE_TASK_NAME, EnhanceTask.class,
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
        try {
            projectTasks.getByName("classes").dependsOn(ENHANCE_TASK_NAME);
        } catch (final UnknownTaskException ute) {
            //No worries
            project.getLogger().warn(ute.getMessage(), ute);
        }
    }
}
