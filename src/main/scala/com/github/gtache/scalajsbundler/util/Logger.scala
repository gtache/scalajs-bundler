package com.github.gtache.scalajsbundler.util

import org.slf4j.{Logger, LoggerFactory}

import scala.sys.process.ProcessLogger

object Logger {
  val logger: Logger = LoggerFactory.getLogger("some-logger")

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

  def log(s: String): Unit = {
    logger.info(s)
  }

  def getProcessLogger: ProcessLogger = {
    ProcessLogger(out => Logger.info(out), err => Logger.error(err))
  }
}
