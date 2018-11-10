package org.rm3l.datanucleus.gradle.extensions;

import org.rm3l.datanucleus.gradle.DataNucleusApi;

import java.io.File;

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

    public boolean isDetachListener() {
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
