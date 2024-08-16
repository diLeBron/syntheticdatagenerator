package ru.digitalleague.syntheticdatagenerator.utils

object Json {
  def convertToJson(map: Map[String, Any]): String = {
    map
      .map { case (key, value) => s""""$key":$value""" }
      .mkString("{", ",", "}")
  }

}
