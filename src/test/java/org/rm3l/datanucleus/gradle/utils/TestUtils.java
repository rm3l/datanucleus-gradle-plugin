package org.rm3l.datanucleus.gradle.utils;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import java.nio.file.Path;
import java.util.Arrays;

public final class TestUtils {

    static final String DOMAIN_PACKAGE_NAME_IN_TEST_PROJECT = "org.rm3l.datanucleus.gradle.test.domain";
    public static final String DN_JPA_RDBMS_VERSION = "5.1.11";
    public static final String JUNIT_VERSION = "4.12";

    private TestUtils() {
        throw new UnsupportedOperationException("Not instantiable");
    }

    /**
     * Helper method that runs a Gradle task in the testProjectDir
     * @param arguments the task arguments to execute
     * @param isSuccessExpected boolean representing whether or not the build is supposed to fail
     * @return the task's BuildResult
     */
    public static BuildResult gradle(Path tempDir,
                                     @SuppressWarnings("SameParameterValue") boolean isSuccessExpected,
                                     String... arguments) {
        final String[] args;
        if (arguments == null) {
            args = new String[] {"tasks", "--stacktrace"};
        } else {
            args = Arrays.copyOf(arguments, arguments.length + 1);
            args[args.length - 1] = "--stacktrace";
        }

        final GradleRunner runner = GradleRunner.create()
                .withArguments(args)
                .withProjectDir(tempDir.toFile())
                .withPluginClasspath()
                .forwardOutput()
                .withDebug(true);
        return isSuccessExpected ? runner.build() : runner.buildAndFail();
    }

    public static BuildResult gradle(Path tempDir, String... arguments) {
        return gradle(tempDir, true, arguments);
    }

}
