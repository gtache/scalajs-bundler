package com.github.gtache.scalajsbundler

import java.io.File

import com.github.gtache.scalajsbundler.util.{IO, JSON, Logger}
import org.gradle.api.artifacts.Configuration

object PackageJson {

  /**
    * Write a package.json file defining the NPM dependencies of the application, plus the ones
    * required to do the bundling.
    *
    * @param targetFile           File to write into
    * @param npmDependencies      NPM dependencies
    * @param npmDevDependencies   NPM devDependencies
    * @param npmResolutions       Resolutions to use in case of conflicting dependencies
    * @param additionalNpmConfig  Additional options to include in 'package.json'
    * @param fullClasspath        Classpath (used to look for dependencies of Scala.js libraries this project depends on)
    * @param currentConfiguration Current configuration
    * @return The created package.json file
    */
  def write(targetFile: File,
            npmDependencies: Seq[(String, String)],
            npmDevDependencies: Seq[(String, String)],
            npmResolutions: Map[String, String],
            additionalNpmConfig: Map[String, JSON],
            fullClasspath: Seq[File],
            currentConfiguration: Configuration,
            webpackVersion: String,
            webpackDevServerVersion: String,
            webpackCliVersion: String
           ): Unit = {
    val npmManifestDependencies = NpmDependencies.collectFromClasspath(fullClasspath)
    val dependencies =
      npmDependencies ++ (
        if (currentConfiguration.getName == "compile") npmManifestDependencies.compileDependencies
        else npmManifestDependencies.testDependencies
        )

    val sourceMapLoaderVersion =
      NpmPackage(webpackVersion).major match {
        case Some(1) | Some(2) => "0.1.5"
        case Some(3) => "0.2.1"
        case Some(4) => "0.2.3"
        case Some(x) => sys.error(s"Unsupported webpack major version $x")
        case None => sys.error("No webpack version defined")
      }

    val webpackPackages =
      NpmPackage(webpackVersion).major match {
        case Some(1) | Some(2) | Some(3) => Seq("webpack" -> webpackVersion)
        case Some(4) => Seq("webpack" -> webpackVersion, "webpack-cli" -> webpackCliVersion)
        case _ => Seq.empty
      }

    val devDependencies =
      npmDevDependencies ++ (
        if (currentConfiguration.getName == "compile") npmManifestDependencies.compileDevDependencies
        else npmManifestDependencies.testDevDependencies
        ) ++ webpackPackages ++ Seq(
        "webpack-dev-server" -> webpackDevServerVersion,
        "concat-with-sourcemaps" -> "1.0.7", // Used by the reload workflow
        "source-map-loader" -> sourceMapLoaderVersion // Used by webpack when emitSourceMaps is enabled
      )

    val packageJson =
      JSON.obj(additionalNpmConfig.toSeq :+
        "dependencies" -> JSON.objStr(resolveDependencies(dependencies, npmResolutions)) :+
        "devDependencies" -> JSON.objStr(resolveDependencies(devDependencies, npmResolutions)): _*
      )

    Logger.debug("Writing 'package.json'")
    IO.write(targetFile, packageJson.toJson)
    ()
  }

  /**
    * Resolves multiple occurrences of a dependency to a same package.
    *
    *  - If all the occurrences refer to the same version, pick this one ;
    *  - If they refer to different versions, pick the one defined in `resolutions` (or fail
    * if there is no such resolution).
    *
    * @return The resolved dependencies
    * @param dependencies The dependencies to resolve
    * @param resolutions  The resolutions to use in case of conflict (they will be ignored if there are no conflicts)
    */
  def resolveDependencies(dependencies: Seq[(String, String)],
                          resolutions: Map[String, String],
                         ): List[(String, String)] = {
    val resolvedDependencies =
      dependencies
        .groupBy { case (name, _) => name }
        .mapValues(_.map(_._2).distinct)
        .foldRight(List.empty[(String, String)]) { case ((name, versions), result) =>
          val resolvedDependency =
            versions match {
              case Seq(single) =>
                name -> single
              case _ =>
                val resolution = resolutions.get(name) match {
                  case Some(v) => v
                  case None => versions.mkString(" ")
                }
                name -> resolution
            }
          resolvedDependency :: result
        }

    // Add a warning in case a resolution was defined but not used because the corresponding
    // dependency was not in conflict.
    val unusedResolutions =
    resolutions.filter { case (name, resolution) =>
      resolvedDependencies.exists { case (n, v) => n == name && v != resolution }
    }
    if (unusedResolutions.nonEmpty) {
      Logger.warn(s"Unused resolutions: $unusedResolutions")
    }

    Logger.debug(s"Resolved the following dependencies: $resolvedDependencies")

    resolvedDependencies
  }

}
