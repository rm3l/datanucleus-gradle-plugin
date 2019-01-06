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

package org.rm3l.datanucleus.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.TaskContainer;
import org.rm3l.datanucleus.gradle.extensions.DataNucleusExtension;
import org.rm3l.datanucleus.gradle.tasks.AbstractDataNucleusTask;
import org.rm3l.datanucleus.gradle.tasks.enhance.EnhanceCheckTask;
import org.rm3l.datanucleus.gradle.tasks.enhance.EnhanceTask;
import org.rm3l.datanucleus.gradle.tasks.enhance.TestEnhanceCheckTask;
import org.rm3l.datanucleus.gradle.tasks.enhance.TestEnhanceTask;
import org.rm3l.datanucleus.gradle.tasks.schematool.*;

import java.util.Arrays;

import static org.rm3l.datanucleus.gradle.extensions.DataNucleusExtension.*;
import static org.rm3l.datanucleus.gradle.extensions.schematool.SchemaToolExtension.*;

@SuppressWarnings("unused")
public class DataNucleusPlugin implements Plugin<Project> {

    @Override
    public void apply(@SuppressWarnings("NullableProblems") Project project) {
        project.getPlugins().apply(JavaPlugin.class);

        final DataNucleusExtension dataNucleusExtension = new DataNucleusExtension(project);

        //Register tasks
        addTask(project, ENHANCE_TASK_NAME, EnhanceTask.class, new String[]{"compileJava"}, new String[]{"classes"});
        addTask(project, ENHANCE_CHECK_TASK_NAME, EnhanceCheckTask.class, null, null);
        addTask(project, TEST_ENHANCE_TASK_NAME, TestEnhanceTask.class, new String[]{"compileTestJava"},
                new String[]{"testClasses"});
        addTask(project, TEST_ENHANCE_CHECK_TASK_NAME, TestEnhanceCheckTask.class, null, null);

        addTask(project, CREATE_DATABASE, CreateDatabaseTask.class, new String[]{"classes"}, null);
        addTask(project, DELETE_DATABASE, DeleteDatabaseTask.class, new String[]{"classes"}, null);
        addTask(project, CREATE_DATABASE_TABLES, CreateDatabaseTablesTask.class, new String[]{"classes"}, null);
        addTask(project, DELETE_DATABASE_TABLES, DeleteDatabaseTablesTask.class, new String[]{"classes"}, null);
        addTask(project, DELETE_THEN_CREATE_DATABASE_TABLES, DeleteThenCreateDatabaseTablesTask.class, new String[]{"classes"}, null);
        addTask(project, VALIDATE_DATABASE_TABLES, ValidateDatabaseTablesTask.class, new String[]{"classes"}, null);
        addTask(project, DBINFO, DBInfoTask.class, new String[]{"classes"}, null);
        addTask(project, SCHEMAINFO, SchemaInfoTask.class, new String[]{"classes"}, null);

        final Logger projectLogger = project.getLogger();
        if (projectLogger.isDebugEnabled()) {
            projectLogger.debug("Adding DataNucleus extensions to the build [{}]", project.getName());
        }

        project.getExtensions().add("datanucleus", dataNucleusExtension);
    }

    private <T extends AbstractDataNucleusTask> void addTask(final Project project, final String taskName, final Class<T> taskType,
                                                             final String[] dependencies, final String[] dependentTasks) {
        final TaskContainer projectTasks = project.getTasks();

        final T task = projectTasks.create(taskName, taskType);

        if (dependencies != null) {
            Arrays.stream(dependencies).forEach(task::dependsOn);
        }
        if (dependentTasks != null) {
            for (final String dependentTask : dependentTasks) {
                if (dependentTask != null) {
                    projectTasks.getByName(dependentTask).dependsOn(taskName);
                }
            }
        }
    }
}
