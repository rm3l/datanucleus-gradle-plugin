package org.rm3l.datanucleus.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.rm3l.datanucleus.gradle.extensions.DataNucleusExtension;
import org.rm3l.datanucleus.gradle.extensions.EnhanceExtension;
import org.rm3l.datanucleus.gradle.tasks.EnhanceTask;

public class DataNucleusPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);

        final DataNucleusExtension dataNucleusExtension = new DataNucleusExtension(project);

        project.getLogger().debug( "Adding DataNucleus extensions to the build [{}]", project.getName() );
        project.getExtensions().add( "datanucleus", dataNucleusExtension);

        project.getTasks().create("enhance", EnhanceTask.class,
                task -> {
                    final EnhanceExtension enhanceExtension = dataNucleusExtension.getEnhance();
                    task.getPersistenceUnitName().set(enhanceExtension.getPersistenceUnitName());
                    task.getLog4jConfiguration().set(enhanceExtension.getLog4jConfiguration());
                    task.getJdkLogConfiguration().set(enhanceExtension.getJdkLogConfiguration());
                    task.getApi().set(enhanceExtension.getApi());
                    task.getVerbose().set(enhanceExtension.isVerbose());
                    task.getQuiet().set(enhanceExtension.isQuiet());
                    task.getTargetDirectory().set(enhanceExtension.getTargetDirectory());
                    task.getFork().set(enhanceExtension.isFork());
                    task.getGeneratePK().set(enhanceExtension.isGeneratePK());
                    task.getPersistenceUnitName().set(enhanceExtension.getPersistenceUnitName());
                    task.getGenerateConstructor().set(enhanceExtension.isGenerateConstructor());
                    task.getIgnoreMetaDataForMissingClasses()
                            .set(enhanceExtension.isIgnoreMetaDataForMissingClasses());
                });

        //TODO
    }
}
