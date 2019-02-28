package com.github.gtache.scalajsbundler.util

import com.github.gtache.scalajsbundler.util.Logger.Level.Level
import org.scalajs.core.tools.logging
import org.slf4j.{Logger, LoggerFactory}

import scala.sys.process.ProcessLogger

object Logger {
  val logger: Logger = LoggerFactory.getLogger("some-logger")

  object Level extends Enumeration {
    type Level = Value
    val ERROR, WARN, INFO, DEBUG = Value
  }

  def info(s: String): Unit = {
    logger.info(s)
  }

  def error(s: String): Unit = {
    logger.error(s)
  }

  def warn(s: String): Unit = {
    logger.warn(s)
  }

  def debug(s: String): Unit = {
    logger.debug(s)
  }

  def log(s: String, level: Level = Level.INFO): Unit = {
    level match {
      case Level.ERROR => error(s)
      case Level.WARN => warn(s)
      case Level.INFO => info(s)
      case Level.DEBUG => debug(s)
    }
  }

  def trace(t: Throwable): Unit = {
    logger.trace(t.getMessage)
  }

  def getProcessLogger: ProcessLogger = {
    ProcessLogger(out => Logger.info(out), err => Logger.error(err))
  }

  def getSJSLogger: org.scalajs.core.tools.logging.Logger = {
    new logging.Logger {
      override def log(level: org.scalajs.core.tools.logging.Level, message: => String): Unit = Logger.log(message, sjsLevelToGradleLevel(level))

      override def success(message: => String): Unit = Logger.log(message)

      override def trace(t: => Throwable): Unit = Logger.trace(t)
    }

    def sjsLevelToGradleLevel(level: org.scalajs.core.tools.logging.Level): Level.Value = level match {
      case org.scalajs.core.tools.logging.Level.Error => Level.ERROR
      case org.scalajs.core.tools.logging.Level.Warn => Level.WARN
      case org.scalajs.core.tools.logging.Level.Info => Level.INFO
      case org.scalajs.core.tools.logging.Level.Debug => Level.DEBUG
    }
  }
}
