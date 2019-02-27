package com.github.gtache.scalajsbundler.util

import java.io.{BufferedWriter, File, FileWriter}
import java.nio.charset.Charset

object IO {
  def write(file: File, content: String): Unit = {
    val writer = new BufferedWriter(new FileWriter(file))
    writer.write(content)
    writer.flush()
    writer.close()
  }

  def copy(file: File, newFile: File): Unit = {

  }

  def copyFile(file: File, newFile: File): Unit = copy(file, newFile)

  def write(path: String, content: String): Unit = {
    write(new File(path), content)
  }

  def withTemporaryFile[T](prefix: String, postfix: String)(action: File => T): T = {
    null
  }

  def readBytes(file: File): Array[Byte] = {
    Array.empty
  }

  def append(file: File, bytes: Array[Byte]): Unit = {

  }

  def move(file: File, newfile: File): Unit = {

  }

  def read(file: File, charset: Charset = Charset.forName("UTF-8")): String = {
    ""
  }
}
