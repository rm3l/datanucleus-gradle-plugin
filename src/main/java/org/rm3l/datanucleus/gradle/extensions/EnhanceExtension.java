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

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.rm3l.datanucleus.gradle.DataNucleusApi;

import java.io.File;

/**
 * Extension for the 'enhance' DSL, part of the parent 'datanucleus' one
 */
@SuppressWarnings("unused")
public class EnhanceExtension {

    private String persistenceUnitName;
    private File log4jConfiguration;
    private File jdkLogConfiguration;
    private DataNucleusApi api = DataNucleusApi.JDO;
    private boolean verbose = false;
    private boolean quiet = false;
    private File targetDirectory;
    private boolean fork = true;
    private boolean generatePK = true;
    private boolean generateConstructor = true;
    private boolean detachListener = false;
    private boolean ignoreMetaDataForMissingClasses = false;

    /**
     * The source sets that hold persistent model.  Default is project.sourceSets.main
     */
    private SourceSet sourceSet;

    EnhanceExtension(Project project, String defaultSourceSetName) {
        final JavaPluginConvention javaConvention =
                project.getConvention().getPlugin(JavaPluginConvention.class);
        this.sourceSet = javaConvention.getSourceSets().getByName(defaultSourceSetName);
    }

    public EnhanceExtension sourceSet(SourceSet sourceSet) {
        this.sourceSet = sourceSet;
        return this;
    }

    SourceSet getSourceSet() {
        return this.sourceSet;
    }

    String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public EnhanceExtension persistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
        return this;
    }

    File getLog4jConfiguration() {
        return log4jConfiguration;
    }

    public EnhanceExtension log4jConfiguration(String log4jConfiguration) {
        this.log4jConfiguration = new File(log4jConfiguration);
        return this;
    }

    File getJdkLogConfiguration() {
        return jdkLogConfiguration;
    }

    public EnhanceExtension jdkLogConfiguration(String jdkLogConfiguration) {
        this.jdkLogConfiguration = new File(jdkLogConfiguration);
        return this;
    }

    DataNucleusApi getApi() {
        return api;
    }

    public EnhanceExtension api(DataNucleusApi api) {
        this.api = api;
        return this;
    }

    boolean isVerbose() {
        return verbose;
    }

    public EnhanceExtension verbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    boolean isQuiet() {
        return quiet;
    }

    public EnhanceExtension quiet(boolean quiet) {
        this.quiet = quiet;
        return this;
    }

    File getTargetDirectory() {
        return targetDirectory;
    }

    public EnhanceExtension targetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
        return this;
    }

    boolean isFork() {
        return fork;
    }

    public EnhanceExtension fork(boolean fork) {
        this.fork = fork;
        return this;
    }

    boolean isGeneratePK() {
        return generatePK;
    }

    public EnhanceExtension generatePK(boolean generatePK) {
        this.generatePK = generatePK;
        return this;
    }

    boolean isGenerateConstructor() {
        return generateConstructor;
    }

    public EnhanceExtension generateConstructor(boolean generateConstructor) {
        this.generateConstructor = generateConstructor;
        return this;
    }

    boolean isDetachListener() {
        return detachListener;
    }

    public EnhanceExtension detachListener(boolean detachListener) {
        this.detachListener = detachListener;
        return this;
    }

    boolean isIgnoreMetaDataForMissingClasses() {
        return ignoreMetaDataForMissingClasses;
    }

    public EnhanceExtension ignoreMetaDataForMissingClasses(boolean ignoreMetaDataForMissingClasses) {
        this.ignoreMetaDataForMissingClasses = ignoreMetaDataForMissingClasses;
        return this;
    }
}
