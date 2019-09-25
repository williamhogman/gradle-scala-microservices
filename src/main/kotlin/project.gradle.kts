//
// This is a Kotlin DSL script which gets compiled into the binary
// Gradle plugin `se.bynk.gradle.`.
//

package se.bynk.gradle.plugin

import com.google.cloud.tools.jib.gradle.JibExtension

// Use built-in plugins.  External plugins are specified in the
// resource file `plugins.properties`.
plugins {
    java
    maven
}

apply("org.gradle.scala")

// Configure all the POM/JAR repositories we like to use.
repositories {
    jcenter()
    mavenCentral()
    // Add the private repository if set
    optionsProperties.getProperty("privateRepositoryURL")?.takeUnless { it.isBlank() }?.let {
        maven(it)
    }
}


// Apply the plugins listed in `plugins.properties`.
for ((k, _) in pluginProperties) {
    apply(plugin = k as String)
}
val mainClasspath = mainSourceSet.runtimeClasspath

tasks {
    withType<JavaCompile> {
        doLast {
            sourceCompatibility = bynk.jvm_version
            targetCompatibility = bynk.jvm_version
            options.setIncremental(true)
            options.encoding = "UTF-8"
        }
    }

    withType<Wrapper> {
        gradleVersion = version("gradle")
    }

    withType<Jar> {
        doLast {
            manifest {
                attributes("Main-Class" to bynk.main_class)
            }
        }
    }
}