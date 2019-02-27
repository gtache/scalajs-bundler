package com.github.gtache.scalajsbundler

import java.io.{File, FileReader}

import org.scalajs.core.tools
import org.scalajs.core.tools.json.{JSON, JSONDeserializer, JSONObjExtractor}

import scala.util.Try

case class NpmPackage(version: String) {
  def major: Option[Int] = {
    val r = """^(\d+)(\..*|)$""".r
    version match {
      case r(v, _) => Try(v.toInt).toOption
      case _ => None
    }
  }
}

object NpmPackage {

  implicit object NpmPackageDeserializer extends JSONDeserializer[NpmPackage] {
    def deserialize(x: JSON): NpmPackage = {
      val obj = new JSONObjExtractor(x)
      NpmPackage(
        obj.fld[String]("version")
      )
    }
  }

  def getForModule(targetDir: File, module: String): Option[NpmPackage] = {
    val webpackPackageJsonFilePath = targetDir + "/node_modules/module/package.json"

    Try(tools.json.fromJSON[NpmPackage](tools.json.readJSON(new FileReader(webpackPackageJsonFilePath)))).toOption
  }
}