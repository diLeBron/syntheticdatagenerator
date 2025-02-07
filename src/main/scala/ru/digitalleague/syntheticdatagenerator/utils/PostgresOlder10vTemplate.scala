package ru.digitalleague.syntheticdatagenerator.utils

import ru.digitalleague.syntheticdatagenerator.source.TableFields

class PostgresOlder10vTemplateImpl extends Template {
  override def dropSchema(schema: String): String = s"DROP SCHEMA IF EXISTS $schema CASCADE"

  override def createSchema(schema: String): String = s"CREATE SCHEMA $schema"

  override def dropTable(schema: String, table: String): String =
    s"DROP TABLE IF EXISTS $schema.$table CASCADE"

  override def makeFields(fields: TableFields): String = {
    if (fields.isPrimaryKey) s"${fields.name} ${fields.dataType} PRIMARY KEY"
    else {
      val madeFields = s"${fields.name} ${fields.dataType}"
      if (fields.unique) madeFields + " UNIQUE"
      else if (fields.unique & !fields.nullable) madeFields + " UNIQUE" + " NOT NULL"
      else if (!fields.nullable) madeFields + " NOT NULL"
      else madeFields
    }
  }

  override def createTable(
      schema: String,
      table: String,
      fields: List[TableFields]
  ): String =
    s"CREATE TABLE $schema.$table(${fields.map(makeFields).mkString(", ")})"

  override def insertIntoTable(schema: String, table: String, values: List[Any]): String =
    s"INSERT INTO $schema.$table VALUES (${values.mkString(",")})"
}
