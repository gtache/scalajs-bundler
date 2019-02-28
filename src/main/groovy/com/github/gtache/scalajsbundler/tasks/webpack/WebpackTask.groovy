package com.github.gtache.scalajsbundler.tasks.webpack

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class WebpackTask extends DefaultTask {
    public static final String WEBPACK_VERSION_P = "webpackVersion" //string
    public static final String WEBPACK_BUNDLING_MODE_P = "webpackBundlingMode" //BundlingMode
    public static final String WEBPACK_CONFIG_FILE_P = "webpackConfigFile" //File
    public static final String WEBPACK_RESOURCES_P = "webpackResources" //FileCollection
    public static final String WEBPACK_SOURCEMAP_P = "webpackEmitSourceMaps" //Boolean
    public static final String WEBPACK_MONITORED_DIR_P = "webpackMonitoredDirectories" //FileCollection
    public static final String WEBPACK_MONITORED_FILES_P = "webpackMonitoredFiles" //FileCollection
    public static final String WEBPACK_EXTRA_ARGS_P = "webpackExtraArgs" //Seq string
    public static final String WEBPACK_NODE_ARGS_P = "webpackNodeArgs" //Seq string
    public static final String WEBPACK_CLI_VERSION_P = "webpackCliVersion" //String
    @TaskAction
    def run() {

    }
}
