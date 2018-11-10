package org.rm3l.datanucleus.gradle.tasks;

import org.datanucleus.enhancer.DataNucleusEnhancer;
import org.gradle.api.DefaultTask;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.rm3l.datanucleus.gradle.DataNucleusApi;

import java.io.File;

public class EnhanceTask extends DefaultTask {

    private final Property<String> persistenceUnitName;
    private final Property<File> log4jConfiguration;
    private final Property<File> jdkLogConfiguration;
    private final Property<DataNucleusApi> api;
    private final Property<Boolean> verbose;
    private final Property<Boolean> quiet;
    private final Property<File> targetDirectory;
    private final Property<Boolean> fork;
    private final Property<Boolean> generatePK;
    private final Property<Boolean> generateConstructor;
    private final Property<Boolean> detachListener;
    private final Property<Boolean> ignoreMetaDataForMissingClasses;

    public EnhanceTask() {
        final ObjectFactory objects = getProject().getObjects();
        persistenceUnitName = objects.property(String.class);
        log4jConfiguration = objects.property(File.class);
        jdkLogConfiguration = objects.property(File.class);
        api = objects.property(DataNucleusApi.class);
        verbose = objects.property(Boolean.class);
        quiet = objects.property(Boolean.class);
        targetDirectory = objects.property(File.class);
        fork = objects.property(Boolean.class);
        generatePK = objects.property(Boolean.class);
        generateConstructor = objects.property(Boolean.class);
        detachListener = objects.property(Boolean.class);
        ignoreMetaDataForMissingClasses = objects.property(Boolean.class);
    }

    @Input
    public Property<String> getPersistenceUnitName() {
        return persistenceUnitName;
    }

    @Input
    @Optional
    public Property<File> getLog4jConfiguration() {
        return log4jConfiguration;
    }

    @Input
    @Optional
    public Property<File> getJdkLogConfiguration() {
        return jdkLogConfiguration;
    }

    @Input
    public Property<DataNucleusApi> getApi() {
        return api;
    }

    @Input
    @Optional
    public Property<Boolean> getVerbose() {
        return verbose;
    }

    @Input
    @Optional
    public Property<Boolean> getQuiet() {
        return quiet;
    }

    @Input
    @Optional
    public Property<File> getTargetDirectory() {
        return targetDirectory;
    }

    @Input
    public Property<Boolean> getFork() {
        return fork;
    }

    @Input
    @Optional
    public Property<Boolean> getGeneratePK() {
        return generatePK;
    }

    @Input
    @Optional
    public Property<Boolean> getGenerateConstructor() {
        return generateConstructor;
    }

    @Input
    public Property<Boolean> getDetachListener() {
        return detachListener;
    }

    @Input
    @Optional
    public Property<Boolean> getIgnoreMetaDataForMissingClasses() {
        return ignoreMetaDataForMissingClasses;
    }

    @SuppressWarnings("unused")
    @TaskAction
    public void performEnhancement() {
        final DataNucleusEnhancer enhancer = new DataNucleusEnhancer(api.get().name(), null)
                .setVerbose(verbose.get())
                .addPersistenceUnit(persistenceUnitName.get()) //TODO Support class list
                .setDetachListener(detachListener.get())
                .setGenerateConstructor(generateConstructor.get())
                .setGeneratePK(generatePK.get())
                .setSystemOut(!quiet.get())
                .setOutputDirectory(targetDirectory.get().getAbsolutePath());
        final int result = enhancer.enhance();
        getProject().getLogger().info("Enhanced {} class using DataNucleus Enhancer", result);
    }
}
