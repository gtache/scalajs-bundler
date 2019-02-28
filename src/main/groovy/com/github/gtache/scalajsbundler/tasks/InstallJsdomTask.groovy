package com.github.gtache.scalajsbundler.tasks

import com.github.gtache.scalajsbundler.ExternalCommand
import com.github.gtache.scalajsbundler.PluginMain
import com.github.gtache.scalajsbundler.util.IO
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import scala.collection.JavaConverters
import scala.collection.Seq

class InstallJsdomTask extends DefaultTask {
    public static final String VERSION_P = "installJsdomVersion" //String

    @TaskAction
    def run() {
        File installDir = new File(project.projectDir, "scalajs-bundler-jsdom")
        File baseDir = project.rootDir
        File jsdomDir = new File(new File(installDir, "node_modules"), "jsdom")
        String jsdomVersion = project.property(VERSION_P)
        boolean useYarn = project.property(PluginMain.USE_YARN_P)
        Seq<String> npmExtraArgs = JavaConverters.asScalaBuffer(project.property(PluginMain.NPM_EXTRA_ARGS_P) as List<String>)
        Seq<String> yarnExtraArgs = JavaConverters.asScalaBuffer(project.property(PluginMain.YARN_EXTRA_ARGS_P) as List<String>)
        Seq<String> npmPackages = JavaConverters.asScalaBuffer(Arrays.asList("jsdom" + jsdomVersion))
        if (!jsdomDir.exists()) {
            project.logger.info("Installing jsdom in " + installDir.absolutePath)
            IO.createDirectory(installDir)
            ExternalCommand.addPackages(baseDir, installDir, useYarn, npmExtraArgs, yarnExtraArgs, npmPackages)
        }
        installDir
    }
}
