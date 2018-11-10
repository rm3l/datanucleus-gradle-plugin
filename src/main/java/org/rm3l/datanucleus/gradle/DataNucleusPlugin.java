package org.rm3l.datanucleus.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.rm3l.datanucleus.gradle.extensions.DataNucleusExtension;

public class DataNucleusPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaPlugin.class);

        final DataNucleusExtension dataNucleusExtension = new DataNucleusExtension(project);

        project.getLogger().debug( "Adding DataNucleus extensions to the build [{}]", project.getName() );
        project.getExtensions().add( "datanucleus", dataNucleusExtension);
    }
}
