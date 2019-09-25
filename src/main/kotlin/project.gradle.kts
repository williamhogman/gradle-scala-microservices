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
    idea
    scala
}

// Configure all the POM/JAR repositories we like to use.
repositories {
    jcenter()
    mavenCentral()
    // Add the private repository if set
    optionsProperties.getProperty("privateRepositoryURL")?.takeUnless { it.isBlank() }?.let {
        maven(it)
    }
}

// Make the plugin configurable via a ``bynk { ... }`` block.
val bynk = extensions.create<BynkProject>("bynk")

// Apply the plugins listed in `plugins.properties`.
for ((k, _) in pluginProperties) {
    apply(plugin = k as String)
}

// Add our standard dependencies that don't have configurable
// versions; the configurable ones come later.
dependencies {
    "implementation"("org.scala-lang:scala-library:${version("scalaLibrary")}")
}

val mainSourceSet = the<JavaPluginConvention>().sourceSets["main"]
val mainClasspath = mainSourceSet.runtimeClasspath

defaultTasks("run")

task<JavaExec>("run") {
    main = bynk.main_class
    classpath = mainClasspath
}

task<JavaExec>("debug") {
    main = bynk.main_class
    classpath = mainClasspath
    debug = true
    environment["DEBUG"] = true
}

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
    val sourcesJar by registering(Jar::class) {
        dependsOn("classes")
        classifier = "sources"
        from(mainSourceSet.allSource)
    }

    val javadoc by existing(Javadoc::class)
    val javadocJar by registering(Jar::class) {
        dependsOn(javadoc)
        from(javadoc.get().destinationDir)
    }

    artifacts {
        add("archives", sourcesJar)
        add("archives", javadocJar)
    }
}

configure<JibExtension> {
    // TODO: Set this image here!!
    from {
        image = "gradle:5.3.1-jdk11"
    }
}