package com.github.gtache.scalajsbundler

import java.io.File

import com.github.gtache.scalajsbundler.util.Logger
import scalajsbundler.util.Commands

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
            extraArgs: Seq[String]
           ): Unit = this.synchronized {
    stop()
    worker = Some(new Worker(
      workDir,
      configPath,
      port,
      extraArgs))
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
                      ) {
    Logger.info("Starting webpack-dev-server")

    val command: Seq[String] = Seq(
      "node",
      "node_modules/webpack-dev-server/bin/webpack-dev-server.js",
      "--config",
      configPath.getAbsolutePath,
      "--port",
      port.toString
    ) ++ extraArgs

    val process: Process = Commands.start(command, workDir)

    def stop(): Unit = {
      Logger.info("Stopping webpack-dev-server")
      process.destroy()
    }
  }

  override def finalize(): Unit = stop()
}
