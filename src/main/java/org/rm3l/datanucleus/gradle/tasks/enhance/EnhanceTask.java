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

package org.rm3l.datanucleus.gradle.tasks.enhance;

import org.datanucleus.enhancer.DataNucleusEnhancer;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.internal.impldep.org.apache.commons.lang.BooleanUtils;
import org.rm3l.datanucleus.gradle.DataNucleusApi;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Actual enhancer task
 */
public class EnhanceTask extends DefaultTask {

    private final Property<Boolean> skip;
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
    private final Property<Boolean> checkOnly;

    @SuppressWarnings("WeakerAccess")
    public EnhanceTask() {
        final ObjectFactory objects = getProject().getObjects();
        skip = objects.property(Boolean.class);
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
        checkOnly = objects.property(Boolean.class);
    }

    @Input
    @Optional
    public Property<Boolean> getSkip() {
        return skip;
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
    @Optional
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

    @Input
    @Optional
    public Property<Boolean> getCheckOnly() {
        return checkOnly;
    }

    @SuppressWarnings("unused")
    @TaskAction
    public void performEnhancement() throws MalformedURLException {

        final Project project = getProject();
        final Logger projectLogger = project.getLogger();

        final Boolean shouldSkip = skip.get();
        if (shouldSkip != null && shouldSkip) {
            if (projectLogger.isDebugEnabled()) {
                projectLogger.debug("Enhancement Task Execution skipped as requested");
            }
        } else {

            final JavaPluginConvention javaConvention =
                    project.getConvention().getPlugin(JavaPluginConvention.class);
            final SourceSet main = javaConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
            final SourceSetOutput mainOutput = main.getOutput();

            final SourceSet test = javaConvention.getSourceSets().getByName(SourceSet.TEST_SOURCE_SET_NAME);
            final SourceSetOutput testOutput = main.getOutput();

            final Stream<File> mainStream = Stream.concat(
                    Stream.concat(
                            mainOutput.getClassesDirs().getFiles().stream(),
                            Stream.of(mainOutput.getResourcesDir())
                    ),
                    main.getResources().getSrcDirs().stream()
            );
            final Stream<File> testStream = Stream.concat(
                    Stream.concat(
                            testOutput.getClassesDirs().getFiles().stream(),
                            Stream.of(testOutput.getResourcesDir())
                    ),
                    test.getResources().getSrcDirs().stream()
            );

            final List<String> sourcePathList = Stream.concat(mainStream, testStream)
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toList());
            final URL[] classloaderUrls = new URL[sourcePathList.size()];
            int index = 0;
            for (final String sourcePath : sourcePathList) {
                classloaderUrls[index++] = new File(sourcePath + "/").toURI().toURL();
            }

            final DataNucleusEnhancer enhancer = new DataNucleusEnhancer(api.get().name(), null)
                    .setVerbose(verbose.get())
                    .setClassLoader(new URLClassLoader(classloaderUrls, Thread.currentThread().getContextClassLoader()))
                    .addPersistenceUnit(persistenceUnitName.get())
                    .setDetachListener(detachListener.get())
                    .setGenerateConstructor(generateConstructor.get())
                    .setGeneratePK(generatePK.get())
                    .setSystemOut(!quiet.get())
                    .setOutputDirectory(targetDirectory.get().getAbsolutePath());
            final int result;
            //noinspection ConstantConditions
            if (checkOnly.get() != null && checkOnly.get()) {
                result = enhancer.validate();
                getProject().getLogger().info("Enhancement validation succeeded for {} class(es)", result);
            } else {
                result = enhancer.enhance();
                projectLogger.info("Enhanced {} class using DataNucleus Enhancer", result);
            }
        }
    }
}
