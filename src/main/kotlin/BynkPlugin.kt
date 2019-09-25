package se.bynk.gradle.plugin

import com.google.cloud.tools.jib.gradle.JibExtension
import java.io.StringReader
import java.util.Properties
import groovy.text.SimpleTemplateEngine
import org.gradle.kotlin.dsl.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.jvm.tasks.Jar

val versionProperties =
        readProperties("versions.properties")
val pluginProperties =
        readProperties("plugins.properties", vars = versionProperties)

val optionsProperties =
        readProperties("options.properties")

fun version(x: String) = versionProperties.getProperty(x)

class BynkPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val mainClass = optionsProperties.getProperty("mainClass")
        val jvmVersion = version("jvm")
        pluginProperties.forEach { k, _ ->
            target.plugins.apply(k as String)
        }
        // Deps :)
        target.dependencies {
            "implementation"("org.scala-lang:scala-library:${version("scalaLibrary")}")
        }
        target.tasks.withType(JavaCompile::class.java) {
            sourceCompatibility = jvmVersion
            targetCompatibility = jvmVersion
            options.setIncremental(true)
            options.encoding = "UTF-8"
        }

        target.tasks.withType<Wrapper> {
            gradleVersion = version("gradle")
        }

        target.tasks.withType<Jar> {
            manifest {
                attributes("Main-Class" to mainClass)
            }
        }

        target.configure<JibExtension> {
            // TODO: Set this image here!!
            from {
                image = "gradle:5.3.1-jdk11"
            }
        }
    }
}

// We only use this to get access to the resources via class loader.
private class Foo {}

private fun readProperties(
        path: String,
        vars: Properties = Properties()
): Properties {
    fun parse(text: String) =
            Properties().apply { load(StringReader(text)) }
    fun read(path: String) =
            Foo::class.java.classLoader.getResource(path).readText()
    fun expand(text: String, vars: Properties) =
            SimpleTemplateEngine().createTemplate(text).make(vars).toString()

    return parse(expand(read(path), vars))
}
