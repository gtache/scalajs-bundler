package com.github.gtache.scalajsbundler.util

import java.io.File

import com.github.gtache.scalajsbundler.BundlerFile


object JSBundler {

  def loaderScript(bundleName: String): String =
    s"""
       |var exports = window;
       |exports.require = window["$bundleName"].require;
    """.stripMargin

  def writeLoader(
                   loaderFile: BundlerFile.Loader,
                   bundleName: String
                 ): Unit =
    IO.write(loaderFile.file, loaderScript(bundleName))

  /**
    * Run webpack to bundle the application.
    *
    * @param targetDir Target directory (and working directory for Nodejs)
    * @return The generated bundles
    */
  def bundle(targetDir: File,
             entry: BundlerFile.Application,
             libraryFile: BundlerFile.Library,
             emitSourceMaps: Boolean = false,
             libraryBundleName: String): BundlerFile.ApplicationBundle = {
    val bundleFile = entry.asApplicationBundle
    val loaderFile = entry.asLoader
    writeLoader(loaderFile, libraryBundleName)
    if (emitSourceMaps) {
      Logger.info("Bundling dependencies with source maps")
      val concatContent =
        JS.let(
          JS.ref("require")(JS.str("concat-with-sourcemaps")),
          JS.ref("require")(JS.str("fs"))
        ) { (Concat, fs) =>
          JS.let(
            JS.`new`(Concat,
              JS.bool(true),
              JS.str(bundleFile.file.getName),
              JS.str(";\n"))) { concat =>
            JS.block(
              concat
                .dot("add")
                .apply(
                  JS.str(""),
                  fs.dot("readFileSync")
                    .apply(JS.str(libraryFile.file.getAbsolutePath),
                      JS.str("utf-8")),
                  fs.dot("readFileSync")
                    .apply(JS.str(libraryFile.file.getAbsolutePath ++ ".map"))
                ),
              concat
                .dot("add")
                .apply(JS.str(loaderFile.file.getName),
                  fs.dot("readFileSync")
                    .apply(JS.str(loaderFile.file.getAbsolutePath))),
              concat
                .dot("add")
                .apply(
                  JS.str(""),
                  fs.dot("readFileSync")
                    .apply(JS.str(entry.file.getAbsolutePath), JS.str("utf-8")),
                  fs.dot("readFileSync")
                    .apply(JS.str(entry.file.getAbsolutePath ++ ".map"),
                      JS.str("utf-8"))
                ),
              JS.let(JS.`new`(
                JS.ref("Buffer"),
                JS.str(
                  s"\n//# sourceMappingURL=${bundleFile.file.getName ++ ".map"}\n"))) {
                endBuffer =>
                  JS.let(
                    JS.ref("Buffer")
                      .dot("concat")
                      .apply(JS.arr(concat.dot("content"), endBuffer))) {
                    result =>
                      fs.dot("writeFileSync")
                        .apply(JS.str(bundleFile.file.getAbsolutePath), result)
                  }
              },
              fs.dot("writeFileSync")
                .apply(JS.str(bundleFile.file.getAbsolutePath ++ ".map"),
                  concat.dot("sourceMap"))
            )
          }
        }
      val concatFile = new File(targetDir, s"scalajsbundler-concat-${bundleFile.file.getName}.js")
      IO.write(concatFile, concatContent.show)
      Commands.run(Seq("node", concatFile.getAbsolutePath), targetDir)
    } else {
      Logger.info("Bundling dependencies without source maps")
      IO.withTemporaryFile("scalajs-bundler", entry.project) { tmpFile =>
        IO.append(tmpFile, IO.readBytes(libraryFile.file))
        IO.append(tmpFile, "\n")
        IO.append(tmpFile, IO.readBytes(loaderFile.file))
        IO.append(tmpFile, "\n")
        IO.append(tmpFile, IO.readBytes(entry.file))
        IO.move(tmpFile, bundleFile.file)
      }
    }
    bundleFile
  }
}
