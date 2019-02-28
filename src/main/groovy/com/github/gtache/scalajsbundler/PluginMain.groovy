package com.github.gtache.scalajsbundler

import com.github.gtache.scalajsbundler.tasks.InstallJsdomTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class PluginMain implements Plugin<Project> {
    public static final String JSDOM_VERSION_P = "jsdomVersion" //string
    public static final String CROSS_TARGET_P = "crossTarget" //file
    public static final String NPM_DEPENDENCIES_P = "npmDependencies" //name -> version
    public static final String NPM_DEV_DEPENDENCIES_P = "npmDevDependencies" //name -> version
    public static final String NPM_RESOLUTIONS_P = "npmResolutions" //name -> version
    public static final String ADD_NPM_CONFIG_P = "additionalNpmConfig" //name -> json
    public static final String NPM_EXTRA_ARGS_P = "npmExtraArgs" //Seq string

    public static final String USE_YARN_P = "useYarn" //Boolean
    public static final String YARN_EXTRA_ARGS_P = "yarnExtraArgs" //Seq String
    public static final String REQUIRE_JSDOM_P = "requireJsdomEnv" //Boolean
    public static final String JS_SOURCE_DIRECTORIES_P = "jsSourceDirectories" //FileCollection

    static final WebpackDevServer server = new WebpackDevServer()
    @Override
    void apply(Project project) {
        project.tasks.create("installJsdom", InstallJsdomTask.class)
    }
}
