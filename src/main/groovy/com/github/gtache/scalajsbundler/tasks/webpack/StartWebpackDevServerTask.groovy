package com.github.gtache.scalajsbundler.tasks.webpack

import com.github.gtache.scalajsbundler.PluginMain
import com.github.gtache.scalajsbundler.Webpack
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import scala.collection.JavaConverters
import scala.collection.Seq

//Depends on npmUpdate and Webpack
class StartWebpackDevServerTask extends DefaultTask {
    public static final String WEBPACK_DEV_SERV_P = "webpackDevServerVersion" //string
    public static final String WEBPACK_DEV_SERVER_PORT_P = "webpackDevServerPort" //String
    public static final String WEBPACK_DEV_SERVER_EXTRA_ARGS_P = "webpackDevServerExtraArgs" //Seq string

    @TaskAction
    def run() {
        String port = project.property(WEBPACK_DEV_SERVER_PORT_P)
        Seq<String> extraArgs = JavaConverters.asScalaBuffer(project.property(WEBPACK_DEV_SERVER_EXTRA_ARGS_P) as List<String>)

        // This duplicates file layout logic from `Webpack`
        def targetDir = project.property(PluginMain.CROSS_TARGET_P) as File
        def customConfigOption = project.property(WebpackTask.WEBPACK_CONFIG_FILE_P)
        def generatedConfig = new File(targetDir, "scalajs.webpack.config.js")

        def config = customConfigOption
                .map(Webpack.copyCustomWebpackConfigFiles(targetDir, project.property(WebpackTask.WEBPACK_RESOURCES_P)))
        .getOrElse(generatedConfig.file)

        // To match `webpack` task behavior
        def workDir = targetDir

        // Server instance is project-level
        def server = PluginMain.server
        def logger = project.logger

        server.start(
                workDir,
                config,
                port,
                extraArgs,
                logger
        )
    }
}
