package ru.digitalleague.syntheticdatagenerator.utils

import ru.digitalleague.syntheticdatagenerator.source.TableFields

trait Template {
  def dropSchema(schema: String): String

  def createSchema(schema: String): String

  def dropTable(schema: String, table: String): String

  def makeFields(fields: TableFields): String

  def createTable(schema: String, table: String, fields: List[TableFields]): String

  def insertIntoTable(schema: String, table: String, values: List[Any]): String
}

trait JsonTemplate extends Template {}