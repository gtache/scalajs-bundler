package com.github.gtache.scalajsbundler

import java.io.{File, InputStream}

import com.github.gtache.scalajsbundler.Stats.WebpackStats
import com.github.gtache.scalajsbundler.util.{Commands, IO, JS, Logger}
import org.scalajs.core.tools.linker.StandardLinker.Config
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.util.{Failure, Success, Try}

object Webpack {

  // Represents webpack 4 modes
  sealed trait WebpackMode {
    def mode: String
  }

  case object DevelopmentMode extends WebpackMode {
    val mode = "development"
  }

  case object ProductionMode extends WebpackMode {
    val mode = "production"
  }

  object WebpackMode {
    def apply(sjsConfig: Config): WebpackMode = {
      if (sjsConfig.semantics.productionMode) {
        ProductionMode
      } else {
        DevelopmentMode
      }
    }
  }

  /**
    * Copies the custom webpack configuration file and the webpackResources to the target dir
    *
    * @param targetDir        target directory
    * @param webpackResources Resources to copy
    * @param customConfigFile User supplied config file
    * @return The copied config file.
    */
  def copyCustomWebpackConfigFiles(targetDir: File, webpackResources: Seq[File])(customConfigFile: File): File = {
    def copyToWorkingDir(targetDir: File)(file: File): File = {
      val copy = new File(targetDir + "/" + file.getName)
      IO.copyFile(file, copy)
      copy
    }

    webpackResources.foreach(copyToWorkingDir(targetDir))
    copyToWorkingDir(targetDir)(customConfigFile)
  }

  /**
    * Writes the webpack configuration file. The output file is designed to be minimal, and to be extended,
    * however, the `entry` and `output` keys must be preserved in order for the bundler to work as expected.
    *
    * @param emitSourceMaps    Whether source maps is enabled at all
    * @param entry             The input entrypoint file to process via webpack
    * @param webpackConfigFile webpack configuration file to write to
    * @param libraryBundleName If defined, generate a library bundle named `libraryBundleName`
    */
  def writeConfigFile(emitSourceMaps: Boolean,
                      entry: BundlerFile.WebpackInput,
                      webpackConfigFile: BundlerFile.WebpackConfig,
                      libraryBundleName: Option[String],
                      mode: WebpackMode): Unit = {
    Logger.info("Writing scalajs.webpack.config.js")
    // Build the output configuration, configured for library output
    // if a library bundle name is provided
    val output = libraryBundleName match {
      case Some(bundleName) =>
        JS.obj(
          "path" -> JS.str(webpackConfigFile.targetDir.toAbsolutePath.toString),
          "filename" -> JS.str(BundlerFile.Library.fileName("[name]")),
          "library" -> JS.str(bundleName),
          "libraryTarget" -> JS.str("var")
        )
      case None =>
        JS.obj(
          "path" -> JS.str(webpackConfigFile.targetDir.toAbsolutePath.toString),
          "filename" -> JS.str(BundlerFile.ApplicationBundle.fileName("[name]"))
        )
    }

    // Build the file itself
    val webpackConfigContent =
      JS.ref("module").dot("exports").assign(JS.obj(Seq(
        "entry" -> JS.obj(
          entry.project -> JS.arr(JS.str(entry.file.getAbsolutePath))
        ),
        "output" -> output
      ) ++ (
        if (emitSourceMaps) {
          val webpackNpmPackage = NpmPackage.getForModule(webpackConfigFile.targetDir.toFile, "webpack")
          webpackNpmPackage.flatMap(_.major) match {
            case Some(1) =>
              Seq(
                "devtool" -> JS.str("source-map"),
                "module" -> JS.obj(
                  "preLoaders" -> JS.arr(
                    JS.obj(
                      "test" -> JS.regex("\\.js$"),
                      "loader" -> JS.str("source-map-loader")
                    )
                  )
                )
              )
            case Some(2) =>
              Seq(
                "devtool" -> JS.str("source-map"),
                "module" -> JS.obj(
                  "rules" -> JS.arr(
                    JS.obj(
                      "test" -> JS.regex("\\.js$"),
                      "enforce" -> JS.str("pre"),
                      "loader" -> JS.str("source-map-loader")
                    )
                  )
                )
              )
            case Some(3) =>
              Seq(
                "devtool" -> JS.str("source-map"),
                "module" -> JS.obj(
                  "rules" -> JS.arr(
                    JS.obj(
                      "test" -> JS.regex("\\.js$"),
                      "enforce" -> JS.str("pre"),
                      "use" -> JS.arr(JS.str("source-map-loader"))
                    )
                  )
                )
              )
            case Some(4) =>
              Seq(
                "mode" -> JS.str(mode.mode),
                "devtool" -> JS.str("source-map"),
                "module" -> JS.obj(
                  "rules" -> JS.arr(
                    JS.obj(
                      "test" -> JS.regex("\\.js$"),
                      "enforce" -> JS.str("pre"),
                      "use" -> JS.arr(JS.str("source-map-loader"))
                    )
                  )
                )
              )
            case Some(x) => sys.error(s"Unsupported webpack major version $x")
            case None => sys.error("No webpack version defined")
          }
        } else Nil
        ): _*))
    IO.write(webpackConfigFile.file, webpackConfigContent.show)
  }

  /**
    * Run webpack to bundle the application.
    *
    * @param emitSourceMaps             Whether or not source maps are enabled
    * @param generatedWebpackConfigFile Webpack config file generated by scalajs-bundler
    * @param customWebpackConfigFile    User supplied config file
    * @param webpackResources           Additional resources to be copied to the working folder
    * @param entry                      Scala.js application to bundle
    * @param targetDir                  Target directory (and working directory for Nodejs)
    * @param extraArgs                  Extra arguments passed to webpack
    * @param mode                       Mode for webpack 4
    * @return The generated bundles
    */
  def bundle(emitSourceMaps: Boolean,
             generatedWebpackConfigFile: BundlerFile.WebpackConfig,
             customWebpackConfigFile: Option[File],
             webpackResources: Seq[File],
             entry: BundlerFile.Application,
             targetDir: File,
             extraArgs: Seq[String],
             nodeArgs: Seq[String],
             mode: WebpackMode,
            ): BundlerFile.ApplicationBundle = {
    writeConfigFile(emitSourceMaps, entry, generatedWebpackConfigFile, None, mode)

    val configFile = customWebpackConfigFile
      .map(Webpack.copyCustomWebpackConfigFiles(targetDir, webpackResources))
      .getOrElse(generatedWebpackConfigFile.file)

    Logger.info("Bundling the application with its NPM dependencies")
    val args = extraArgs ++: Seq("--config", configFile.getAbsolutePath)
    val stats = Webpack.run(nodeArgs: _*)(args: _*)(targetDir)
    stats.foreach(print)
    // Attempt to discover the actual name produced by webpack indexing by chunk name and discarding maps
    val bundle = generatedWebpackConfigFile.asApplicationBundle(stats)
    assert(bundle.file.exists(), "Webpack failed to create application bundle")
    assert(bundle.assets.forall(_.exists()), "Webpack failed to create application assets")
    bundle
  }

  /**
    * Run webpack to bundle the application.
    *
    * @param emitSourceMaps             Are source maps enabled?
    * @param generatedWebpackConfigFile Webpack config file generated by scalajs-bundler
    * @param customWebpackConfigFile    User supplied config file
    * @param webpackResources           Additional webpack resources to include in the working directory
    * @param entryPointFile             The entrypoint file to bundle dependencies for
    * @param libraryModuleName          The library module name to assign the webpack bundle to
    * @param extraArgs                  Extra arguments passed to webpack
    * @param mode                       Mode for webpack 4
    * @return The generated bundle
    */
  def bundleLibraries(emitSourceMaps: Boolean,
                      generatedWebpackConfigFile: BundlerFile.WebpackConfig,
                      customWebpackConfigFile: Option[File],
                      webpackResources: Seq[File],
                      entryPointFile: BundlerFile.EntryPoint,
                      libraryModuleName: String,
                      extraArgs: Seq[String],
                      nodeArgs: Seq[String],
                      mode: WebpackMode,
                     ): BundlerFile.Library = {
    writeConfigFile(emitSourceMaps,
      entryPointFile,
      generatedWebpackConfigFile,
      Some(libraryModuleName),
      mode)

    val configFile = customWebpackConfigFile
      .map(Webpack.copyCustomWebpackConfigFiles(generatedWebpackConfigFile.targetDir.toFile, webpackResources))
      .getOrElse(generatedWebpackConfigFile.file)

    val args = extraArgs ++: Seq("--config", configFile.getAbsolutePath)
    val stats = Webpack.run(nodeArgs: _*)(args: _*)(generatedWebpackConfigFile.targetDir.toFile)
    stats.foreach(print)
    val library = generatedWebpackConfigFile.asLibrary(stats)
    assert(library.file.exists, "Webpack failed to create library file")
    assert(library.assets.forall(_.exists), "Webpack failed to create library assets")
    library
  }

  private def jsonOutput(cmd: Seq[String])(in: InputStream): Option[WebpackStats] = {
    Try {
      val parsed = Json.parse(in)
      parsed.validate[WebpackStats] match {
        case JsError(e) =>
          Logger.error("Error parsing webpack stats output")
          // In case of error print the result and return None. it will be ignored upstream
          e.foreach {
            case (p, v) => Logger.error(s"$p: ${v.mkString(",")}")
          }
          None
        case JsSuccess(p, _) =>
          if (p.warnings.nonEmpty || p.errors.nonEmpty) {
            Logger.info("")
            // Filtering is a workaround for #111
            p.warnings.filterNot(_.contains("https://raw.githubusercontent.com")).foreach(x => Logger.warn(x))
            p.errors.foreach(x => Logger.error(x))
          }
          Some(p)
      }
    } match {
      case Success(x) =>
        x
      case Failure(e) =>
        // In same cases errors are not reported on the json output but comes on stdout
        // where they cannot be parsed as json. The best we can do here is to suggest
        // running the command manually
        Logger.error(s"Failure on parsing the output of webpack: ${e.getMessage}")
        Logger.error(s"You can try to manually execute the command")
        Logger.error(cmd.mkString(" "))
        Logger.error("\n")
        None
    }
  }

  /**
    * Runs the webpack command.
    *
    * @param nodeArgs   node.js cli flags
    * @param args       Arguments to pass to the webpack command
    * @param workingDir Working directory in which the Nodejs will be run (where there is the `node_modules` subdirectory)
    */
  def run(nodeArgs: String*)(args: String*)(workingDir: File): Option[WebpackStats] = {
    val webpackBin = new File(workingDir.getAbsolutePath + "/node_modules/webpack/bin/webpack")
    val params = nodeArgs ++ Seq(webpackBin.getAbsolutePath, "--bail", "--profile", "--json") ++ args
    val cmd = "node" +: params
    Commands.run(cmd, workingDir, jsonOutput(cmd)).fold(sys.error, _.flatten)
  }

}
