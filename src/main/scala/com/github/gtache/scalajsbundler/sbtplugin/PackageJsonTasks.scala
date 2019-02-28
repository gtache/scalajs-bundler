package com.github.gtache.scalajsbundler.sbtplugin

import java.io.File

import com.github.gtache.scalajsbundler.util.IO.FileImprovements
import com.github.gtache.scalajsbundler.util.{Caching, JSON}
import com.github.gtache.scalajsbundler.{BundlerFile, PackageJson}
import org.gradle.api.artifacts.Configuration

object PackageJsonTasks {

  /**
    * Writes the package.json file that describes the project dependencies
    *
    * @param targetDir               Directory in which write the file
    * @param npmDependencies         NPM dependencies
    * @param npmDevDependencies      NPM devDependencies
    * @param npmResolutions          Resolutions to use in case of conflicts
    * @param additionalNpmConfig     Additional options to include in 'package.json'
    * @param fullClasspath           Classpath
    * @param configuration           Current configuration (Compile or Test)
    * @param webpackVersion          Webpack version
    * @param webpackDevServerVersion Webpack development server version
    * @return The written package.json file
    */
  def writePackageJson(targetDir: File,
                       npmDependencies: Seq[(String, String)],
                       npmDevDependencies: Seq[(String, String)],
                       npmResolutions: Map[String, String],
                       additionalNpmConfig: Map[String, JSON],
                       fullClasspath: Seq[File],
                       configuration: Configuration,
                       webpackVersion: String,
                       webpackDevServerVersion: String,
                       webpackCliVersion: String
                      ): BundlerFile.PackageJson = {

    val hash = Seq(
      configuration.getName,
      npmDependencies.toString,
      npmDevDependencies.toString,
      npmResolutions.toString,
      fullClasspath.map(_.getName).toString,
      webpackVersion,
      webpackDevServerVersion,
      webpackCliVersion
    ).mkString(",")

    val packageJsonFile = targetDir / "package.json"

    Caching.cached(
      packageJsonFile,
      hash,
      streams.cacheDirectory / s"scalajsbundler-package-json-${if (configuration.getName == "compile") "main" else "test"}"
    ) { () =>
      PackageJson.write(
        packageJsonFile,
        npmDependencies,
        npmDevDependencies,
        npmResolutions,
        additionalNpmConfig,
        fullClasspath,
        configuration,
        webpackVersion,
        webpackDevServerVersion,
        webpackCliVersion
      )
      ()
    }

    BundlerFile.PackageJson(packageJsonFile)
  }

}
