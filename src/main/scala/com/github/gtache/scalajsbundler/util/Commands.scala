package com.github.gtache.scalajsbundler.util

import java.io.{File, InputStream}

import com.github.gtache.scalajsbundler.util.Logger

import scala.sys.process.{BasicIO, Process}

object Commands {

  def run[A](cmd: Seq[String], cwd: File, outputProcess: InputStream => A): Either[String, Option[A]] = {
    val toErrorLog = (is: InputStream) => {
      scala.io.Source.fromInputStream(is).getLines.foreach(msg => Logger.error(msg))
      is.close()
    }

    // Unfortunately a var is the only way to capture the result
    var result: Option[A] = None

    def outputCapture(o: InputStream): Unit = {
      result = Some(outputProcess(o))
      o.close()
      ()
    }

    Logger.debug(s"Command: ${cmd.mkString(" ")}")
    val process = Process(cmd, cwd)
    val processIO = BasicIO.standard(false).withOutput(outputCapture).withError(toErrorLog)
    val code: Int = process.run(processIO).exitValue()
    if (code != 0) {
      Left(s"Non-zero exit code: $code")
    } else {
      Right(result)
    }
  }

  def run(cmd: Seq[String], cwd: File): Unit = {
    val toInfoLog = (is: InputStream) => scala.io.Source.fromInputStream(is).getLines.foreach(msg => Logger.info(msg))
    run(cmd, cwd, toInfoLog).fold(sys.error, _ => ())
  }

  def start(cmd: Seq[String], cwd: File): Process =
    Process(cmd, cwd).run(Logger.getProcessLogger)

}
