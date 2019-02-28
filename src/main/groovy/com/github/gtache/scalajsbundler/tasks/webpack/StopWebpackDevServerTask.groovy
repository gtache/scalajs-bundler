package com.github.gtache.scalajsbundler.tasks.webpack

import com.github.gtache.scalajsbundler.PluginMain
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class StopWebpackDevServerTask extends DefaultTask {

    @TaskAction
    def run() {
        PluginMain.server.stop()
    }
}
