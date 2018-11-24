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
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.util.ConfigureUtil;
import org.rm3l.datanucleus.gradle.extensions.enhance.EnhanceExtension;
import org.rm3l.datanucleus.gradle.tasks.enhance.EnhanceTask;

import java.io.File;
import java.util.Set;

/**
 * Extension for the 'datanucleus' DSL entrypoint
 */
@SuppressWarnings("unused")
public class DataNucleusExtension {

    private static final String ENHANCE_TASK_NAME = "enhance";
    private static final String TEST_ENHANCE_TASK_NAME = "testEnhance";

    private final Project project;

    private Boolean skip = false;

    private final EnhanceExtension enhance;

    private final EnhanceExtension testEnhance;

    public DataNucleusExtension(Project project) {
        this.project = project;
        this.enhance = new EnhanceExtension(project, SourceSet.MAIN_SOURCE_SET_NAME);
        this.testEnhance = new EnhanceExtension(project, SourceSet.TEST_SOURCE_SET_NAME);
    }

    private DataNucleusExtension skip(Boolean skip) {
        this.skip = skip;
        this.enhance.skip(skip);
        this.testEnhance.skip(skip);
        return this;
    }

    //Auto-bind the DSL to a Gradle task
    private void enhance(Closure closure) {
        configureExtensionAndTask(this.enhance,
                closure,
                ENHANCE_TASK_NAME,
                new String[] {"classes"});
    }

    private void testEnhance(Closure closure) {
        configureExtensionAndTask(this.testEnhance,
                closure,
                TEST_ENHANCE_TASK_NAME,
                new String[] {"testClasses"});
    }

    private void configureExtensionAndTask(final EnhanceExtension enhanceExtension,
                                           final Closure closure,
                                           final String taskName,
                                           final String[] dependentTasks) {
        ConfigureUtil.configure(closure, enhanceExtension);

        final TaskContainer projectTasks = project.getTasks();
        projectTasks.create(taskName, EnhanceTask.class,
                task -> {
                    configureTask(enhanceExtension, task);
                    task.getCheckOnly().set(false);
                });


        for (final String dependentTask : dependentTasks) {
            projectTasks.getByName(dependentTask).dependsOn(taskName);
        }

        projectTasks.create(taskName + "Check", EnhanceTask.class,
                task -> {
                    configureTask(enhanceExtension, task);
                    task.getCheckOnly().set(true);
                });
    }

    private void configureTask(EnhanceExtension enhanceExtension, EnhanceTask task) {
        final Boolean enhanceExtensionSkip = enhanceExtension.getSkip();
        final Property<Boolean> taskSkip = task.getSkip();
        taskSkip.set(this.skip);
        taskSkip.set(enhanceExtensionSkip);
        task.getPersistenceUnitName().set(enhanceExtension.getPersistenceUnitName());
        task.getLog4jConfiguration().set(enhanceExtension.getLog4jConfiguration());
        task.getJdkLogConfiguration().set(enhanceExtension.getJdkLogConfiguration());
        task.getApi().set(enhanceExtension.getApi());
        task.getVerbose().set(enhanceExtension.isVerbose());
        task.getQuiet().set(enhanceExtension.isQuiet());
        final File targetDirectory = enhanceExtension.getTargetDirectory();
        final Property<File> taskTargetDirectory = task.getTargetDirectory();
        if (targetDirectory != null) {
            taskTargetDirectory.set(targetDirectory);
        } else {
            final SourceSet sourceSet = enhanceExtension.getSourceSet();
            final Set<File> files = sourceSet.getOutput().getClassesDirs().getFiles();
            if (!files.isEmpty()) {
                taskTargetDirectory.set(files.iterator().next());
            }
        }
        task.getFork().set(enhanceExtension.isFork());
        task.getGeneratePK().set(enhanceExtension.isGeneratePK());
        task.getPersistenceUnitName().set(enhanceExtension.getPersistenceUnitName());
        task.getGenerateConstructor().set(enhanceExtension.isGenerateConstructor());
        task.getDetachListener().set(enhanceExtension.isDetachListener());
        task.getIgnoreMetaDataForMissingClasses()
                .set(enhanceExtension.isIgnoreMetaDataForMissingClasses());
    }

}
