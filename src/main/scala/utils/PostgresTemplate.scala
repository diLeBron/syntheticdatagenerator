package utils

import source.TableFields

class PostgresTemplateImpl extends Template {
  override def createSchema(schema: String): String = s"CREATE SCHEMA IF NOT EXISTS $schema"

  override def dropTable(schema: String, table: String): String =
    s"DROP TABLE IF EXISTS $schema.$table CASCADE"

  override def makeFields(fields: TableFields): String = {
    s"${fields.name} ${fields.dataType}"
  }

  override def createTable(
      schema: String,
      table: String,
      fields: List[TableFields]
  ): String =
    s"CREATE TABLE IF NOT EXISTS $schema.$table(${fields.map(makeFields).mkString(", ")})"

  override def insertIntoTable(schema: String, table: String, values: List[Any]): String =
    s"INSERT INTO $schema.$table VALUES (${values.mkString(",")})"
}
