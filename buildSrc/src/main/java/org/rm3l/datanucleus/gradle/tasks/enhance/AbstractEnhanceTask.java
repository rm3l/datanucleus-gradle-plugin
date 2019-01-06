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
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.options.Option;
import org.gradle.api.tasks.options.OptionValues;
import org.rm3l.datanucleus.gradle.DataNucleusApi;
import org.rm3l.datanucleus.gradle.tasks.AbstractDataNucleusTask;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Actual enhancer task
 */
public abstract class AbstractEnhanceTask extends AbstractDataNucleusTask {

    private final boolean checkOnly;
    private final boolean testClasses;
    private Boolean skip;
    private String persistenceUnitName;
    private File log4jConfiguration;
    private File jdkLogConfiguration;
    private DataNucleusApi api = DataNucleusApi.JDO;
    private Boolean verbose;
    private Boolean quiet;
    private File targetDirectory;
    private Boolean generatePK;
    private Boolean generateConstructor;
    private Boolean detachListener;
    private Boolean ignoreMetaDataForMissingClasses;

    AbstractEnhanceTask(final boolean testClasses, final boolean checkOnly) {
        super.setGroup("DataNucleus Enhancement");
        this.testClasses = testClasses;
        this.checkOnly = checkOnly;
    }

    @Option(option = "skip", description = "Whether to skip execution")
    public void setSkip(Boolean skip) {
        this.skip = skip;
    }

    @Input
    @Optional
    public Boolean getSkip() {
        return skip;
    }

    @Option(option = "persistence-unit-name", description = "Name of the persistence-unit to enhance. Mandatory")
    public void setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    @Input
    @Optional
    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    @InputFile
    @Optional
    public File getLog4jConfiguration() {
        return log4jConfiguration;
    }

    @InputFile
    @Optional
    public File getJdkLogConfiguration() {
        return jdkLogConfiguration;
    }

    @Input
    @Optional
    public DataNucleusApi getApi() {
        return api;
    }

    @Input
    @Optional
    public Boolean getVerbose() {
        return verbose;
    }

    @Input
    @Optional
    public Boolean getQuiet() {
        return quiet;
    }

    @OutputDirectory
    @Optional
    public File getTargetDirectory() {
        return targetDirectory;
    }

    @Input
    @Optional
    public Boolean getGeneratePK() {
        return generatePK;
    }

    @Input
    @Optional
    public Boolean getGenerateConstructor() {
        return generateConstructor;
    }

    @Input
    @Optional
    public Boolean getDetachListener() {
        return detachListener;
    }

    @Input
    @Optional
    public Boolean getIgnoreMetaDataForMissingClasses() {
        return ignoreMetaDataForMissingClasses;
    }

    @Option(option = "log4j-conf", description = "Config file location for Log4J (if using it)")
    public void setLog4jConfiguration(String log4jConfiguration) {
        this.setLog4jConfiguration(
                java.util.Optional.ofNullable(log4jConfiguration).map(File::new).orElse(null));
    }

    public void setLog4jConfiguration(File log4jConfiguration) {
        this.log4jConfiguration = log4jConfiguration;
    }

    @Option(option = "jdk-log-conf", description = "Config file location for JDK logging (if using it)")
    public void setJdkLogConfiguration(String jdkLogConfiguration) {
        this.setJdkLogConfiguration(
                java.util.Optional.ofNullable(jdkLogConfiguration).map(File::new).orElse(null));
    }

    public void setJdkLogConfiguration(File jdkLogConfiguration) {
        this.jdkLogConfiguration = jdkLogConfiguration;
    }

    @Option(option = "api", description = "API to enhance to (JDO or JPA). Mandatory. Default is JDO.")
    public void setApi(DataNucleusApi api) {
        this.api = api;
    }

    @OptionValues("api")
    public List<DataNucleusApi> getApiTypes() {
        return new ArrayList<>(EnumSet.allOf(DataNucleusApi.class));
    }

    @Option(option = "verbose", description = "Whether to be verbose or not")
    public void setVerbose(Boolean verbose) {
        this.verbose = verbose;
    }

    @Option(option = "quiet", description = "Whether to be quiet or not")
    public void setQuiet(Boolean quiet) {
        this.quiet = quiet;
    }

    @Option(option = "target-directory", description = "Where the enhanced classes are written (default is to overwrite them)")
    public void setTargetDirectory(String targetDirectory) {
        this.setTargetDirectory(
                java.util.Optional.ofNullable(targetDirectory).map(File::new).orElse(null));
    }

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    @Option(option = "generate-pk",
            description = "Whether to generate a PK class (of name {MyClass}_PK) for cases where there are multiple PK " +
                    "fields yet no IdClass is defined.")
    public void setGeneratePK(Boolean generatePK) {
        this.generatePK = generatePK;
    }

    @Option(option = "generate-constructor",
            description = "Whether to generate a default constructor if not defined for the class being enhanced.")
    public void setGenerateConstructor(Boolean generateConstructor) {
        this.generateConstructor = generateConstructor;
    }

    @Option(option = "detach-listener", description = "Whether to enhance classes to make use of a detach listener " +
            "for attempts to access an un-detached field.")
    public void setDetachListener(Boolean detachListener) {
        this.detachListener = detachListener;
    }

    @Option(option = "ignore-metadata-for-missing-classes",
            description = "Whether to ignore when we have metadata specified for classes that are not found (e.g in orm.xml)")
    public void setIgnoreMetaDataForMissingClasses(Boolean ignoreMetaDataForMissingClasses) {
        this.ignoreMetaDataForMissingClasses = ignoreMetaDataForMissingClasses;
    }

    @SuppressWarnings("unused")
    @TaskAction
    public final void performEnhancement() throws MalformedURLException {

        final Project project = getProject();
        final Logger projectLogger = project.getLogger();

        final Boolean shouldSkip = skip;
        if (shouldSkip != null && shouldSkip) {
            if (projectLogger.isDebugEnabled()) {
                projectLogger.debug("Enhancement Task Execution skipped as requested");
            }
        } else {

            if (persistenceUnitName == null || persistenceUnitName.trim().isEmpty()) {
                projectLogger.info("Missing or blank 'persistenceUnitName'");
                return;
            }

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

            final List<String> sourcePathList =
                    Stream.concat(mainStream, testStream)
                    .map(File::getAbsolutePath)
                    .collect(Collectors.toList());
            final URL[] classloaderUrls = new URL[sourcePathList.size()];
            int index = 0;
            for (final String sourcePath : sourcePathList) {
                classloaderUrls[index++] = new File(sourcePath + "/").toURI().toURL();
            }

            final DataNucleusEnhancer enhancer = new DataNucleusEnhancer(api.name(), null)
                    .setVerbose(verbose != null && verbose)
                    .setClassLoader(new URLClassLoader(classloaderUrls, Thread.currentThread().getContextClassLoader()))
                    .addPersistenceUnit(persistenceUnitName)
                    .setDetachListener(detachListener != null && detachListener)
                    .setGenerateConstructor(generateConstructor != null && generateConstructor)
                    .setGeneratePK(generatePK != null && generatePK)
                    .setSystemOut(quiet == null || !quiet);
            if (targetDirectory != null) {
                    enhancer.setOutputDirectory(targetDirectory.getAbsolutePath());
            }
            final int result;
            if (this.checkOnly) {
                result = enhancer.validate();
                getProject().getLogger().info("Enhancement validation succeeded for {} class(es)", result);
            } else {
                result = enhancer.enhance();
                projectLogger.info("Enhanced {} class using DataNucleus Enhancer", result);
            }
        }
    }
}
