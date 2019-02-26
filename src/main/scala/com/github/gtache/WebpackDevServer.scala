package com.github.gtache

import java.io.File

import scalajsbundler.util

/**
  * Simple wrapper over webpack-dev-server
  */
private [scalajsbundler] class WebpackDevServer {

  private var worker: Option[Worker] = None

  /**
    * @param workDir - path to working directory for webpack-dev-server
    * @param configPath - path to webpack config.
    * @param port - port, on which the server will operate.
    * @param extraArgs - additional arguments for webpack-dev-server.
    * @param logger - a logger to use for output
    */
  def start(
    workDir: File,
    configPath: File,
    port: Int,
    extraArgs: Seq[String],
    logger: Logger
  ) = this.synchronized {
    stop()
    worker = Some(new Worker(
      workDir,
      configPath,
      port,
      extraArgs,
      logger
    ))
  }

  def stop() = this.synchronized {
    worker.foreach { w => {
      w.stop()
      worker = None
    }}
  }

  private class Worker(
    workDir: File,
    configPath: File,
    port: Int,
    extraArgs: Seq[String],
    logger: Logger
  ) {
    logger.info("Starting webpack-dev-server");

    val command = Seq(
      "node",
      "node_modules/webpack-dev-server/bin/webpack-dev-server.js",
      "--config",
      configPath.getAbsolutePath,
      "--port",
      port.toString
    ) ++ extraArgs

    val process = util.Commands.start(command, workDir, logger)

    def stop() = {
      logger.info("Stopping webpack-dev-server");
      process.destroy()
    }
  }

  override def finalize() = stop()
}
