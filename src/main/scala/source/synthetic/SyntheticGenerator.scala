package source.synthetic

import argument.PropertiesParsableImpl
import config.PropsConfig
import service.SparkSessionCreatableImpl
import source.TableFields
import source.synthetic.SampleGenerator.generator
import ujson.Value
import utils.{HiveTemplateImpl, PostgresTemplateImpl, Template}

import java.sql.{Connection, DriverManager, Statement}
import java.time.LocalDateTime

trait SyntheticGenerator {
  def generate(tmp: Template): SyntheticData

  def generate(props: PropsConfig, dbType: String): Unit
}

class SyntheticGeneratorImpl extends SyntheticGenerator {
  override def generate(tmp: Template): SyntheticData = ???

  override def generate(props: PropsConfig, dbType: String): Unit = {
//    System.setProperty("hadoop.home.dir", "C:\\hadoop")

    var con: Connection = null

    try {
      val propPars = new PropertiesParsableImpl()

      val rules = propPars.rules(props.rulesPath)

      Class.forName(props.dbConfig.driver)
      con = DriverManager.getConnection(
        props.dbConfig.connectionUrl,
        props.dbConfig.user,
        props.dbConfig.password
      )
      val stmt: Statement = con.createStatement()

      val dbTemplate = if (dbType == "Hive") new HiveTemplateImpl() else new PostgresTemplateImpl()

      for (schemaSettings <- rules) {
        val schema = schemaSettings("schema").toString().replaceAll("\"", "")
        stmt.execute(dbTemplate.createSchema(schema))

        for (tableSettings <- schemaSettings("tables").arr.toList) {
          val rowCount = tableSettings("row_count").num.toInt
          if (rowCount > 0) {
            val table = tableSettings("name").toString().replaceAll("\"", "")
            if (tableSettings("recreate").bool)
              stmt.execute(dbTemplate.dropTable(schema, table))

            val cols: List[Value] = tableSettings("columns").arr.toList

            stmt.execute(
              dbTemplate.createTable(
                schema,
                table,
                cols.map(x =>
                  TableFields(
                    x("name").toString().replaceAll("\"", ""),
                    x("type").toString().replaceAll("\"", "")
                  )
                )
              )
            )

            val rule =
              cols.map(x =>
                (
                  x("type").toString().replaceAll("\"", ""),
                  x("data")("action").toString().replaceAll("\"", ""),
                  x("data")("rules").obj.toMap
                )
              )

            val a = generator(rule, rowCount)

            generator(rule, rowCount).transpose
              .map(x => stmt.execute(dbTemplate.insertIntoTable(schema, table, x)))
          }
        }
      }
    } catch {
      case ex: Exception => ex.printStackTrace()
    } finally {
      try {
        if (con != null)
          con.close()
      } catch {
        case ex: Exception => ex.printStackTrace()
      }
    }
  }
}
