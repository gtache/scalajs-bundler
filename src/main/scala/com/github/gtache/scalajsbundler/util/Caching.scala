package com.github.gtache.scalajsbundler.util

import java.io.File

object Caching {

  def cached(
    fileToWrite: File,
    hash: String,
    cache: File
  )(
    write: () => Unit
  ): Unit = {
    if (!fileToWrite.exists() || (cache.exists() && IO.read(cache) != hash)) {
      write()
      IO.write(cache, hash)
    }
  }

}
