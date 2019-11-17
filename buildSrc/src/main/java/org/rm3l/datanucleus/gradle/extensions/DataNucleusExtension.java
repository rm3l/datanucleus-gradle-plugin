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
import org.gradle.api.Task;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.rm3l.datanucleus.gradle.extensions.enhance.EnhanceExtension;
import org.rm3l.datanucleus.gradle.extensions.schematool.SchemaToolExtension;
import org.rm3l.datanucleus.gradle.tasks.enhance.AbstractEnhanceTask;
import org.rm3l.datanucleus.gradle.tasks.enhance.EnhanceCheckTask;
import org.rm3l.datanucleus.gradle.tasks.enhance.EnhanceTask;
import org.rm3l.datanucleus.gradle.tasks.enhance.TestEnhanceCheckTask;
import org.rm3l.datanucleus.gradle.tasks.enhance.TestEnhanceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension for the 'datanucleus' DSL entrypoint
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class DataNucleusExtension {

    public static final String ENHANCE_TASK_NAME = "enhance";
    public static final String ENHANCE_CHECK_TASK_NAME = "enhanceCheck";
    public static final String TEST_ENHANCE_TASK_NAME = "testEnhance";
    public static final String TEST_ENHANCE_CHECK_TASK_NAME = "testEnhanceCheck";

    private final Project project;
    private final EnhanceExtension enhance;
    private final EnhanceExtension testEnhance;
    private final SchemaToolExtension schemaTool;
    private Boolean skip = false;

    private final Logger logger = LoggerFactory.getLogger(DataNucleusExtension.class);

    public DataNucleusExtension(Project project) {
        this.project = project;
        this.enhance = new EnhanceExtension(this, SourceSet.MAIN_SOURCE_SET_NAME);
        this.testEnhance = new EnhanceExtension(this, SourceSet.TEST_SOURCE_SET_NAME);
        this.schemaTool = new SchemaToolExtension(this);
    }

    public Project getProject() {
        return project;
    }

    public Boolean getSkip() {
        return skip;
    }

    private DataNucleusExtension skip(Boolean skip) {
        this.skip = skip;
        this.enhance.skip(skip);
        this.testEnhance.skip(skip);
        return this;
    }

    //Auto-bind the DSL to a Gradle task
    public void enhance(Closure closure) {
        this.enhance.configureExtensionAndTask(closure, ENHANCE_TASK_NAME, EnhanceTask.class,
                new String[]{"compileJava"}, new String[]{"classes"});
        this.enhance.configureExtensionAndTask(closure, ENHANCE_CHECK_TASK_NAME, EnhanceCheckTask.class,
                null, null);
    }

    public void testEnhance(Closure closure) {
        this.testEnhance.configureExtensionAndTask(
                closure, TEST_ENHANCE_TASK_NAME, TestEnhanceTask.class,
                new String[]{"compileTestJava"},
                new String[]{"testClasses"});
        this.testEnhance.configureExtensionAndTask(
                closure, TEST_ENHANCE_CHECK_TASK_NAME, TestEnhanceCheckTask.class, null, null);
    }

    public void schemaTool(Closure closure) {
        this.schemaTool.configureExtensionAndTasks(closure);
//        final TaskContainer tasks = this.getProject().getTasks();
//        final Task enhanceTask = tasks.findByName("enhance");
//        if (enhanceTask == null) {
//            return;
//        }
        this.enhance.api(this.schemaTool.getApi())
                .catalogName(this.schemaTool.getCatalogName())
                .completeDdl(this.schemaTool.isCompleteDdl())
                .ddlFile(this.schemaTool.getDdlFile() != null ? this.schemaTool.getDdlFile().getAbsolutePath() : null)
                .log4jConfiguration(this.schemaTool.getLog4jConfiguration() != null ?
                        this.schemaTool.getLog4jConfiguration().getAbsolutePath() : null)
                .jdkLogConfiguration(this.schemaTool.getJdkLogConfiguration() != null ?
                        this.schemaTool.getJdkLogConfiguration().getAbsolutePath() : null)
                .persistenceUnitName(this.schemaTool.getPersistenceUnitName())
                .verbose(this.schemaTool.isVerbose())
                .schemaName(this.schemaTool.getSchemaName())
                .skip(this.schemaTool.getSkip());
        this.enhance(closure);

//        final Task existingTestEnhanceTask = tasks.findByName("testEnhance");
//        if (existingTestEnhanceTask != null) {
//            if (!(existingTestEnhanceTask instanceof AbstractEnhanceTask)) {
//                logger.warn(
//                    "Found an already existing 'testEnhance' task, which is going to be replaced: {}",
//                    existingTestEnhanceTask);
//            }
//            tasks.replace("testEnhance");
//        }

        this.testEnhance.api(this.schemaTool.getApi())
                .catalogName(this.schemaTool.getCatalogName())
                .completeDdl(this.schemaTool.isCompleteDdl())
                .ddlFile(this.schemaTool.getDdlFile() != null ? this.schemaTool.getDdlFile().getAbsolutePath() : null)
                .log4jConfiguration(this.schemaTool.getLog4jConfiguration() != null ?
                        this.schemaTool.getLog4jConfiguration().getAbsolutePath() : null)
                .jdkLogConfiguration(this.schemaTool.getJdkLogConfiguration() != null ?
                        this.schemaTool.getJdkLogConfiguration().getAbsolutePath() : null)
                .persistenceUnitName(this.schemaTool.getPersistenceUnitName())
                .verbose(this.schemaTool.isVerbose())
                .schemaName(this.schemaTool.getSchemaName())
                .skip(this.schemaTool.getSkip());
        this.testEnhance(closure);
    }
}
