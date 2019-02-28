package com.github.gtache.scalajsbundler.util

object Collections {

  def mapToList[A, B](map: Map[A, B]): List[(A, B)] = {
    map.toList
  }

}
