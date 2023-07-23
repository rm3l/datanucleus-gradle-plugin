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

package org.rm3l.datanucleus.gradle.extensions.enhance;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.util.ConfigureUtil;
import org.rm3l.datanucleus.gradle.DataNucleusApi;
import org.rm3l.datanucleus.gradle.extensions.DataNucleusExtension;
import org.rm3l.datanucleus.gradle.tasks.enhance.AbstractEnhanceTask;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

/**
 * Extension for the 'enhance' DSL, part of the parent 'datanucleus' one
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class EnhanceExtension {

    private final Project project;
    private final DataNucleusExtension datanucleusExtension;
    private String persistenceUnitName;
    private File log4jConfiguration;
    private File jdkLogConfiguration;
    private DataNucleusApi api = DataNucleusApi.JDO;
    private boolean verbose = false;
    private boolean quiet = false;
    private File targetDirectory;
    private boolean generatePK = true;
    private boolean generateConstructor = true;
    private boolean detachListener = false;
    private boolean ignoreMetaDataForMissingClasses = false;

    /**
     * The source sets that hold persistent model.  Default is project.sourceSets.main
     */
    private SourceSet sourceSet;
    private Boolean skip = null;

    public EnhanceExtension(DataNucleusExtension datanucleusExtension, String defaultSourceSetName) {
        this(datanucleusExtension, false, defaultSourceSetName);
    }

    public EnhanceExtension(DataNucleusExtension datanucleusExtension, boolean skip, String defaultSourceSetName) {
        this.datanucleusExtension = datanucleusExtension;
        this.project = datanucleusExtension.getProject();
        this.skip(skip);
        final JavaPluginExtension javaExtension =
                project.getExtensions().getByType(JavaPluginExtension.class);
        this.sourceSet = javaExtension.getSourceSets().getByName(defaultSourceSetName);
    }

    public Boolean getSkip() {
        return skip;
    }

    public EnhanceExtension skip(Boolean skip) {
        this.skip = skip;
        return this;
    }

    public SourceSet getSourceSet() {
        return this.sourceSet;
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public EnhanceExtension persistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
        return this;
    }

    public File getLog4jConfiguration() {
        return log4jConfiguration;
    }

    public EnhanceExtension log4jConfiguration(String log4jConfiguration) {
        if (log4jConfiguration != null) {
            this.log4jConfiguration = new File(log4jConfiguration);
        } else {
            this.log4jConfiguration = null;
        }
        return this;
    }

    public File getJdkLogConfiguration() {
        return jdkLogConfiguration;
    }

    public EnhanceExtension jdkLogConfiguration(String jdkLogConfiguration) {
        if (jdkLogConfiguration != null) {
            this.jdkLogConfiguration = new File(jdkLogConfiguration);
        } else {
            this.jdkLogConfiguration = null;
        }
        return this;
    }

    public DataNucleusApi getApi() {
        return api;
    }

    public EnhanceExtension api(DataNucleusApi api) {
        this.api = api;
        return this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public EnhanceExtension verbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public EnhanceExtension quiet(boolean quiet) {
        this.quiet = quiet;
        return this;
    }

    public File getTargetDirectory() {
        return targetDirectory;
    }

    public EnhanceExtension targetDirectory(String targetDirectory) {
        if (targetDirectory != null) {
            this.targetDirectory = new File(targetDirectory);
        } else {
            this.targetDirectory = null;
        }
        return this;
    }

    public boolean isGeneratePK() {
        return generatePK;
    }

    public EnhanceExtension generatePK(boolean generatePK) {
        this.generatePK = generatePK;
        return this;
    }

    public boolean isGenerateConstructor() {
        return generateConstructor;
    }

    public EnhanceExtension generateConstructor(boolean generateConstructor) {
        this.generateConstructor = generateConstructor;
        return this;
    }

    public boolean isDetachListener() {
        return detachListener;
    }

    public EnhanceExtension detachListener(boolean detachListener) {
        this.detachListener = detachListener;
        return this;
    }

    public boolean isIgnoreMetaDataForMissingClasses() {
        return ignoreMetaDataForMissingClasses;
    }

    public EnhanceExtension ignoreMetaDataForMissingClasses(boolean ignoreMetaDataForMissingClasses) {
        this.ignoreMetaDataForMissingClasses = ignoreMetaDataForMissingClasses;
        return this;
    }

    //Dummy method, just for the EnhanceTask to be created from SchemaTool extension
    public EnhanceExtension catalogName(String catalogName) {
        return this;
    }

    public EnhanceExtension schemaName(String schemaName) {
        return this;
    }

    public EnhanceExtension completeDdl(boolean completeDdl) {
        return this;
    }

    public EnhanceExtension ddlFile(String ddlFile) {
        return this;
    }

    public <T extends AbstractEnhanceTask> void configureExtensionAndTask(
            final Closure closure,
            final String taskName,
            final Class<T> taskType,
            final String[] dependencies,
            final String[] dependentTasks) {

        ConfigureUtil.configure(closure, this);

        final TaskContainer projectTasks = project.getTasks();
        final Task tasksByName = projectTasks.findByName(taskName);
        if (tasksByName != null) {
            this.configureTask((T) tasksByName);
            return;
        }
        final T task = projectTasks.create(taskName, taskType, this::configureTask);

        if (dependencies != null) {
            Arrays.stream(dependencies).forEach(task::dependsOn);
        }
        if (dependentTasks != null) {
            for (final String dependentTask : dependentTasks) {
                projectTasks.getByName(dependentTask).dependsOn(taskName);
            }
        }
    }

    @SuppressWarnings("Duplicates")
    private void configureTask(AbstractEnhanceTask task) {
        final Boolean enhanceExtensionSkip = this.getSkip();
        boolean skip = false;
        if (this.datanucleusExtension.getSkip() != null) {
            skip = this.datanucleusExtension.getSkip();
        }
        if (enhanceExtensionSkip != null) {
            skip = enhanceExtensionSkip;
        }
        task.setSkip(skip);
        task.setPersistenceUnitName(this.getPersistenceUnitName());
        task.setLog4jConfiguration(this.getLog4jConfiguration());
        task.setJdkLogConfiguration(this.getJdkLogConfiguration());
        task.setApi(this.getApi());
        task.setVerbose(this.isVerbose());
        task.setQuiet(this.isQuiet());
        final File targetDirectory = this.getTargetDirectory();
        if (targetDirectory != null) {
            task.setTargetDirectory(targetDirectory);
        } else {
            final SourceSet sourceSet = this.getSourceSet();
            final Set<File> files = sourceSet.getOutput().getClassesDirs().getFiles();
            if (!files.isEmpty()) {
                task.setTargetDirectory(files.iterator().next());
            }
        }
        final File taskTargetDirectory = task.getTargetDirectory();
        if (!(taskTargetDirectory == null || taskTargetDirectory.exists() || taskTargetDirectory.isDirectory())) {
            if (!taskTargetDirectory.mkdirs()) {
                project.getLogger().warn("Failed to create target directory: " + taskTargetDirectory);
            }
        }
        task.setGeneratePK(this.isGeneratePK());
        task.setPersistenceUnitName(this.getPersistenceUnitName());
        task.setGenerateConstructor(this.isGenerateConstructor());
        task.setDetachListener(this.isDetachListener());
        task.setIgnoreMetaDataForMissingClasses(this.isIgnoreMetaDataForMissingClasses());
    }

}
