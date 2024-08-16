package ru.digitalleague.syntheticdatagenerator.utils

import ru.digitalleague.syntheticdatagenerator.source.TableFields

trait HiveTemplate extends Template {
  def createExternalTable(
      database: String,
      table: String,
      fields: List[TableFields]
  ): String
}

class HiveTemplateImpl extends HiveTemplate {
  override def createSchema(schema: String): String = s"CREATE DATABASE IF NOT EXISTS $schema"

  override def dropTable(schema: String, table: String): String =
    s"DROP TABLE IF EXISTS $schema.$table"

  override def makeFields(fields: TableFields): String = {
    s"${fields.name} ${fields.dataType}"
  }

  override def createTable(
                            schema: String,
                            table: String,
                            fields: List[TableFields]
  ): String =
    s"CREATE TABLE IF NOT EXISTS $schema.$table(${fields.map(makeFields).mkString(", ")}) STORED AS PARQUET"

  override def createExternalTable(
      database: String,
      table: String,
      fields: List[TableFields]
  ): String =
    s"CREATE EXTERNAL TABLE IF NOT EXISTS $database.$table(${fields.map(makeFields).mkString(", ")}) STORED AS PARQUET LOCATION '/user/data/core/test'"

  override def insertIntoTable(schema: String, table: String, values: List[Any]): String =
    s"INSERT INTO TABLE $schema.$table VALUES (${values.mkString(",")})"
}
