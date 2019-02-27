package com.github.gtache.scalajsbundler

import java.io.{BufferedWriter, FileWriter}

import com.github.gtache.scalajsbundler.util.{IO, Logger}
import scalajsbundler.util.JS

object WebpackEntryPoint {

  /**
    * @return The written loader file (faking a `require` implementation)
    * @param entryPoint File to write the loader to
    */
  def writeEntryPoint(imports: Seq[String],
                      entryPoint: BundlerFile.EntryPoint): Unit = {
    Logger.info(s"Writing module entry point for ${entryPoint.file.getName}")
    val depsFileContent =
      JS.ref("module")
        .dot("exports")
        .assign(
          JS.obj(
            Seq(
              "require" -> JS.fun(name =>
                JS.obj(imports.map { moduleName =>
                  moduleName -> JS.ref("require").apply(JS.str(moduleName))
                }: _*)
                  .bracket(name))): _*)
        )
    IO.write(entryPoint.file, depsFileContent.show)
  }
}
