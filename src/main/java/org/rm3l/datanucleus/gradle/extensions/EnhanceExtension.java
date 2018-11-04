package org.rm3l.datanucleus.gradle.extensions;

import org.rm3l.datanucleus.gradle.DataNucleusApi;

import java.io.File;

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

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public EnhanceExtension setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
        return this;
    }

    public File getLog4jConfiguration() {
        return log4jConfiguration;
    }

    public EnhanceExtension setLog4jConfiguration(File log4jConfiguration) {
        this.log4jConfiguration = log4jConfiguration;
        return this;
    }

    public File getJdkLogConfiguration() {
        return jdkLogConfiguration;
    }

    public EnhanceExtension setJdkLogConfiguration(File jdkLogConfiguration) {
        this.jdkLogConfiguration = jdkLogConfiguration;
        return this;
    }

    public DataNucleusApi getApi() {
        return api;
    }

    public EnhanceExtension setApi(DataNucleusApi api) {
        this.api = api;
        return this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public EnhanceExtension setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public EnhanceExtension setQuiet(boolean quiet) {
        this.quiet = quiet;
        return this;
    }

    public File getTargetDirectory() {
        return targetDirectory;
    }

    public EnhanceExtension setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
        return this;
    }

    public boolean isFork() {
        return fork;
    }

    public EnhanceExtension setFork(boolean fork) {
        this.fork = fork;
        return this;
    }

    public boolean isGeneratePK() {
        return generatePK;
    }

    public EnhanceExtension setGeneratePK(boolean generatePK) {
        this.generatePK = generatePK;
        return this;
    }

    public boolean isGenerateConstructor() {
        return generateConstructor;
    }

    public EnhanceExtension setGenerateConstructor(boolean generateConstructor) {
        this.generateConstructor = generateConstructor;
        return this;
    }

    public boolean isDetachListener() {
        return detachListener;
    }

    public EnhanceExtension setDetachListener(boolean detachListener) {
        this.detachListener = detachListener;
        return this;
    }

    public boolean isIgnoreMetaDataForMissingClasses() {
        return ignoreMetaDataForMissingClasses;
    }

    public EnhanceExtension setIgnoreMetaDataForMissingClasses(boolean ignoreMetaDataForMissingClasses) {
        this.ignoreMetaDataForMissingClasses = ignoreMetaDataForMissingClasses;
        return this;
    }
}
