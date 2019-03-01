package com.github.gtache.scalajsbundler

import java.io.File

import com.github.gtache.scalajsbundler.util.Commands
import org.gradle.api.logging.Logger

/**
  * Simple wrapper over webpack-dev-server
  */
private[scalajsbundler] class WebpackDevServer {

  private var worker: Option[Worker] = None

  /**
    * @param workDir    - path to working directory for webpack-dev-server
    * @param configPath - path to webpack config.
    * @param port       - port, on which the server will operate.
    * @param extraArgs  - additional arguments for webpack-dev-server.
    */
  def start(workDir: File,
            configPath: File,
            port: Int,
            extraArgs: Seq[String],
            logger: Logger): Unit = this.synchronized {
    stop()
    worker = Some(new Worker(
      workDir,
      configPath,
      port,
      extraArgs,
      logger))
  }

  def stop(): Unit = this.synchronized {
    worker.foreach { w => {
      w.stop()
      worker = None
    }
    }
  }

  private class Worker(workDir: File,
                       configPath: File,
                       port: Int,
                       extraArgs: Seq[String],
                       logger: Logger) {
    logger.info("Starting webpack-dev-server")

    val command: Seq[String] = Seq(
      "node",
      "node_modules/webpack-dev-server/bin/webpack-dev-server.js",
      "--config",
      configPath.getAbsolutePath,
      "--port",
      port.toString
    ) ++ extraArgs

    val process: scala.sys.process.Process = Commands.start(command, workDir)

    def stop(): Unit = {
      logger.info("Stopping webpack-dev-server")
      process.destroy()
    }
  }

  override def finalize(): Unit = stop()
}
