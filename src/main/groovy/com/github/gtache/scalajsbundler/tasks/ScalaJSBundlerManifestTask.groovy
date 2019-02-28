package com.github.gtache.scalajsbundler.tasks

import com.github.gtache.scalajsbundler.NpmDependencies
import com.github.gtache.scalajsbundler.PluginMain
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import com.github.gtache.scalajsbundler.util.Collections
import scala.collection.Map

class ScalaJSBundlerManifestTask extends DefaultTask {

    @TaskAction
    def run() {
        def testC = project.configurations.test
        def compileC = project.configurations.compile
        NpmDependencies.writeManifest(
                new NpmDependencies(
                        Collections.mapToList(compileC.getProperties().get(PluginMain.NPM_DEPENDENCIES_P) as Map<String, String>),
                        Collections.mapToList(compileC.getProperties().get(PluginMain.NPM_DEV_DEPENDENCIES_P) as Map<String, String>),
                        Collections.mapToList(testC.getProperties().get(PluginMain.NPM_DEPENDENCIES_P) as Map<String, String>),
                        Collections.mapToList(testC.getProperties().get(PluginMain.NPM_DEV_DEPENDENCIES_P) as Map<String, String>)
                ),
                project.configurations.compile as File //TODO class directory
        )
    }
}
