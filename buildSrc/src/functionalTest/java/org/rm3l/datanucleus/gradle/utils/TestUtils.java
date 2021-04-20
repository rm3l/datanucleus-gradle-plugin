package org.rm3l.datanucleus.gradle.utils;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import java.nio.file.Path;
import java.util.Arrays;

public final class TestUtils {

    public static final String DN_JPA_RDBMS_VERSION = "5.2.8";
    public static final String JUNIT_VERSION = "4.12";
    public static final String H2_VERSION = "1.4.200";
    static final String DOMAIN_PACKAGE_NAME_IN_TEST_PROJECT = "org.rm3l.datanucleus.gradle.test.domain";

    private TestUtils() {
        throw new UnsupportedOperationException("Not instantiable");
    }

    /**
     * Helper method that runs a Gradle task in the testProjectDir
     *
     * @param arguments         the task arguments to execute
     * @param isSuccessExpected boolean representing whether or not the build is supposed to fail
     * @return the task's BuildResult
     */
    public static BuildResult gradle(Path tempDir,
                                     @SuppressWarnings("SameParameterValue") boolean isSuccessExpected,
                                     String... arguments) {
        final String[] args;
        if (arguments == null) {
            args = new String[]{"tasks", "--info", "--warning-mode", "all"};
        } else {
            args = Arrays.copyOf(arguments, arguments.length + 3);
            args[args.length - 3] = "--info";
            args[args.length - 2] = "--warning-mode";
            args[args.length - 1] = "all";
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
