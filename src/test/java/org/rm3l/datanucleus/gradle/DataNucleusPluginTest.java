package org.rm3l.datanucleus.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;

public class DataNucleusPluginTest {

    @Rule
    public final TemporaryFolder testProjectDir = new TemporaryFolder();

    private File buildGradle;

    @Before
    public void setUp() throws IOException {
        // Prepare build.gradle
        buildGradle = testProjectDir.newFile("build.gradle");
        Files.write(buildGradle.toPath(),
                "plugins { id 'org.rm3l.datanucleus-gradle-plugin' }\n".getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Helper method that runs a Gradle task in the testProjectDir
     * @param arguments the task arguments to execute
     * @param isSuccessExpected boolean representing whether or not the build is supposed to fail
     * @return the task's BuildResult
     */
    private BuildResult gradle(boolean isSuccessExpected, String... arguments) {
        final String[] args;
        if (arguments == null) {
            args = new String[] {"tasks", "--stacktrace"};
        } else {
            args = Arrays.copyOf(arguments, arguments.length + 1);
            args[args.length - 1] = "--stacktrace";
        }
        final GradleRunner runner = GradleRunner.create()
                .withArguments(args)
                .withProjectDir(testProjectDir.getRoot())
                .withPluginClasspath()
                .withDebug(true);
        return isSuccessExpected ? runner.build() : runner.buildAndFail();
    }

    private BuildResult gradle(String... arguments) {
        return gradle(true, arguments);
    }

    @Test
    public void test_standard() {
        final BuildResult result = gradle("helloWorld");
        assert result.task(":helloWorld").getOutcome() == SUCCESS;
        assert result.getOutput().contains("Hello, world!");
    }
}
