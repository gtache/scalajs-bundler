package com.github.gtache.scalajsbundler.util

import java.io._
import java.nio.charset.Charset
import java.nio.file.Files

import scala.collection.mutable

object IO {

  private val utf8 = Charset.forName("UTF-8")

  def write(file: File, content: String, charset: Charset = utf8, append: Boolean = false): Unit = {
    val writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), utf8))
    try {
      writer.write(content)
      writer.flush()
    } finally {
      writer.close()
    }
  }

  def write(file: File, content: Array[Byte], append: Boolean = false): Unit = {
    val writer = new BufferedOutputStream(new FileOutputStream(file, append))
    try {
      writer.write(content)
      writer.flush()
    } finally {
      writer.close()
    }
  }

  def copy(src: File, dest: File): Unit = {
    import java.nio.file.StandardCopyOption
    Files.copy(src.toPath, dest.toPath, StandardCopyOption.REPLACE_EXISTING)
  }

  def copyFile(src: File, dest: File): Unit = copy(src, dest)

  def write(path: String, content: String, charset: Charset = utf8, append: Boolean = false): Unit = {
    write(new File(path), content, charset, append)
  }

  def withTemporaryFile[T](prefix: String, postfix: String)(action: File => T): T = {
    val file = File.createTempFile(prefix, postfix)
    try {
      action(file)
    } finally {
      file.delete()
    }
  }

  def readBytes(file: File): Array[Byte] = {
    if (file.length > Int.MaxValue) {
      throw new IllegalArgumentException("File " + file.getAbsolutePath + " is too big")
    }
    val reader = new BufferedInputStream(new FileInputStream(file))
    try {
      val bytes = new Array[Byte](file.length.toInt)
      reader.read(bytes)
      bytes
    } finally {
      reader.close()
    }
  }

  def append(file: File, bytes: Array[Byte]): Unit = {
    write(file, bytes, append = true)
  }

  def append(file: File, bytes: String): Unit = {
    append(file, bytes.getBytes)
  }

  def move(src: File, dest: File): Unit = {
    Files.move(src.toPath, dest.toPath)
  }

  def read(file: File, charset: Charset = utf8): String = {
    readStream(new FileInputStream(file), charset)
  }

  def readStream(stream: InputStream, charset: Charset = utf8): String = {
    val reader = new BufferedReader(new InputStreamReader(stream, charset))
    try {
      val strBuilder = new StringBuilder()
      while (reader.ready()) {
        strBuilder.append(reader.readLine())
      }
      strBuilder.toString
    } finally {
      reader.close()
    }
  }

  def selectSubfiles(file: File): Set[File] = {
    val ret = mutable.Set[File]()
    if (file.isFile) {
      ret.add(file)
    } else {
      file.listFiles.foreach(d => selectSubfiles(d).foreach(f => ret.add(f)))
    }
    ret.toSet
  }

  implicit class FileImprovements(f: File) {
    def /(child: String): File = if (child != ".") new File(f, child) else f
  }

}
