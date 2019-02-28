package com.github.gtache.scalajsbundler

import org.gradle.api.Plugin
import org.gradle.api.Project

class PluginMain implements Plugin<Project> {
    public static final String WEBPACK_VERSION_P = "webpackVersion" //string
    public static final String JSDOM_VERSION_P = "jsdomVersion" //string
    public static final String WEBPACK_DEV_SERV_P = "webpackDevServerVersion" //string
    public static final String CROSS_TARGET_P = "crossTarget" //file
    public static final String NPM_DEPENDENCIES_P = "npmDependencies" //name -> version
    public static final String NPM_DEV_DEPENDENCIES_P = "npmDevDependencies" //name -> version
    public static final String NPM_RESOLUTIONS_P = "npmResolutions" //name -> version
    public static final String ADD_NPM_CONFIG_P = "additionalNpmConfig" //name -> json
    public static final String NPM_EXTRA_ARGS_P = "npmExtraArgs" //Seq string
    public static final String WEBPACK_BUNDLING_MODE_P = "webpackBundlingMode" //BundlingMode
    public static final String WEBPACK_CONFIG_FILE_P = "webpackConfigFile" //File
    public static final String WEBPACK_RESOURCES_P = "webpackResources" //FileCollection
    public static final String WEBPACK_SOURCEMAP_P = "webpackEmitSourceMaps" //Boolean
    public static final String WEBPACK_MONITORED_DIR_P = "webpackMonitoredDirectories" //FileCollection
    public static final String WEBPACK_MONITORED_FILES_P ="webpackMonitoredFiles" //FileCollection
    public static final String WEBPACK_EXTRA_ARGS_P = "webpackExtraArgs" //Seq string
    public static final String WEBPACK_NODE_ARGS_P ="webpackNodeArgs" //Seq string
    public static final String USE_YARN_P ="useYarn" //Boolean
    public static final String WEBPACK_DEV_SERVER_PORT_P = "webpackDevServerPort" //String
    public static final String YARN_EXTRA_ARGS_P = "yarnExtraArgs" //Seq String
    public static final String WEBPACK_DEV_SERVER_EXTRA_ARGS_P = "webpackDevServerExtraArgs" //Seq string
    public static final String WEBPACK_CLI_VERSION_P = "webpackCliVersion" //String
    public static final String REQUIRE_JSDOM_P = "requireJsdomEnv" //Boolean
    public static final String JS_SOURCE_DIRECTORIES_P = "jsSourceDirectories" //FileCollection
    @Override
    void apply(Project project) {
        public static final String webpackVersion = project.hasProperty(WEBPACK_VERSION_P) ? project.property(WEBPACK_VERSION_P) : "3.5.5"
        public static final String jsdomVersion = project.hasProperty(JSDOM_VERSION_P) ? project.property(JSDOM_VERSION_P) : "3.5.5"
        public static final String webpackDevSVersion = project.hasProperty(WEBPACK_DEV_SERV_P) ? project.property(WEBPACK_DEV_SERV_P) : "2.11.1"
        public static final String crossTarget = project.hasProperty(CROSS_TARGET_P) ? project.property(CROSS_TARGET_P) : "" //TODO

    }
}
