// This is the build configuration for the plugin.  For the Gradle
// stuff that gets applied to the plugin's consumers, see
// `project.gradle.kts`.
import java.io.StringReader
import java.util.Properties
import groovy.text.SimpleTemplateEngine

import org.gradle.kotlin.dsl.*

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven`
    `maven-publish`
}

group = "se.bynk.gradle"
version = "1.0"
description = "Gradle baseline for Bynk"

repositories {
    jcenter()
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        create("bynk-baseline") {
            id = "se.bynk.baseline"
            implementationClass = "se.bynk.gradle.plugin.BynkPlugin"
        }
    }
}

val versions = readProperties("src/main/resources/versions.properties")
val plugins = readProperties("src/main/resources/plugins.properties", vars = versions)

val gradleVersion = versions.getProperty("gradle")
val jvmVersion = versions.getProperty("jvm")

tasks {
    withType<Wrapper> {
        gradleVersion = gradleVersion
    }
}

dependencies {
    for ((_, v) in plugins) {
        // Give our plugin access to external plugin classes, so we
        // can configure them.
        add("implementation", v)
    }
    val kotlinVersion = "1.3.30"
    add("implementation", "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
}

fun readProperties(
        path: String,
        vars: Properties = Properties()
): Properties {
    fun read(path: String) =
            File(path).readText(Charsets.UTF_8)
    fun expand(text: String, vars: Properties) =
            SimpleTemplateEngine().createTemplate(text).make(vars).toString()
    fun parse(text: String): Properties =
            Properties().apply { load(StringReader(text)) }

    return parse(expand(read(path), vars))
}