package se.bynk.gradle.plugin

import java.io.StringReader
import java.util.Properties
import groovy.text.SimpleTemplateEngine
import org.gradle.kotlin.dsl.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.scala.ScalaPlugin
import com.google.cloud.tools.jib.gradle.JibPlugin;

val versionProperties =
        readProperties("versions.properties")
val pluginProperties =
        readProperties("plugins.properties", vars = versionProperties)

val optionsProperties =
        readProperties("options.properties")

fun version(x: String) = versionProperties.getProperty(x)

open class BynkProject {
    var jvm_version: String = version("jvm")
    var main_class: String = "se.bynk.Main"
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
