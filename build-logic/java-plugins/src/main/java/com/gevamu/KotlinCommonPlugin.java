package com.gevamu;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.PluginManager;
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions;
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class KotlinCommonPlugin implements Plugin<Project> {
    private static final String KOTLIN_API_VERSION = "1.2.71";

    private static final Pattern VERSION_PATTERN = Pattern.compile("^(?<major>\\d+)\\.(?<minor>\\d+)");

    private static final String TARGET_JVM_VERSION = "1.8";

    private static final String[] PLUGIN_DEPENDENCIES = new String[] {
        "com.gevamu.java-common-conventions",
        "org.jetbrains.kotlin.jvm",
        "org.jmailen.kotlinter",
        "org.jetbrains.dokka"
    };

    private static final String[] KOTLIN_STANDARD_LIBRARIES = new String[] {
        "org.jetbrains.kotlin:kotlin-stdlib",
        "org.jetbrains.kotlin:kotlin-stdlib-common",
        "org.jetbrains.kotlin:kotlin-stdlib-jdk7",
        "org.jetbrains.kotlin:kotlin-stdlib-jdk8",
        "org.jetbrains.kotlin:kotlin-reflect"
    };

    private static final String[] DEFAULT_RESOLVABLE_CONFIGURATIONS = new String[] {
        JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME,
        JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME,
        JavaPlugin.TEST_COMPILE_CLASSPATH_CONFIGURATION_NAME,
        JavaPlugin.TEST_RUNTIME_CLASSPATH_CONFIGURATION_NAME,
        // TODO look for constants
        "testFixturesCompileClassPath",
        "testFixturesRuntimeClassPath"
    };

    @Override
    public void apply(final Project project) {
        applyDependencies(project.getPluginManager());

        project.getTasks().withType(KotlinCompile.class, (kotlinCompile) -> {
            final KotlinJvmOptions kotlinOptions = kotlinCompile.getKotlinOptions();
            kotlinOptions.setApiVersion(majorMinor(KOTLIN_API_VERSION));
            kotlinOptions.setJvmTarget(TARGET_JVM_VERSION);
            // Useful for reflection.
            kotlinOptions.setJavaParameters(true);
        });

        project.getDependencies().add(
            JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME,
            kotlinApi("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        );
        // logging
        // implementation("org.apache.logging.log4j:log4j-api-kotlin:1.2.0")

        configureSubstitutions(project.getConfigurations());

        // Disable javadoc tasks: dokka will process javadoc from java sources
        project.getTasks().named("javadoc", (it) -> it.setEnabled(false));
    }

    private static void applyDependencies(PluginManager pluginManager) {
        for (String pluginId : PLUGIN_DEPENDENCIES) {
            pluginManager.apply(pluginId);
        }
    }

    private static void configureSubstitutions(ConfigurationContainer configurations) {
        for (String configurationName : DEFAULT_RESOLVABLE_CONFIGURATIONS) {
            Configuration configuration = configurations.findByName(configurationName);
            if (configuration == null) {
                continue;
            }
            configuration.getResolutionStrategy().dependencySubstitution((substitutions) -> {
                for (String moduleName : KOTLIN_STANDARD_LIBRARIES) {
                    substitutions.substitute(substitutions.module(moduleName))
                        .using(substitutions.module(kotlinApi(moduleName)));
                }
            });
        }
    }
    private static String kotlinApi(String dependency) {
        return dependency + ':' + KOTLIN_API_VERSION;
    }

    private static String majorMinor(String fullVersion) {
        Matcher versionMatcher = VERSION_PATTERN.matcher(fullVersion);
        if (versionMatcher.find()) {
            return versionMatcher.group("major") + '.' + versionMatcher.group("minor");
        }
        throw new IllegalArgumentException("Invalid version format: " + fullVersion);
    }
}
