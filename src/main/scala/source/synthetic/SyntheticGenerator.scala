package source.synthetic

import argument.PropertiesParsableImpl
import config.PropsConfig
import source.TableFields
import source.synthetic.SampleGenerator.generator
import ujson.Value
import utils.{HiveTemplateImpl, Logger, PostgresTemplateImpl, Template}

import java.sql.{Connection, DriverManager, Statement}
import scala.util.{Failure, Success, Try}

trait SyntheticGenerator {
  def generate(tmp: Template): SyntheticData

  def generate(props: PropsConfig, dbType: String, logger: Logger): Unit

  def generateTable(
      stmt: Statement,
      schema: String,
      table: String,
      tableSettings: Value,
      dbTemplate: Template,
      logger: Logger
  ): List[List[Any]]
}

class SyntheticGeneratorImpl extends SyntheticGenerator {
  override def generate(tmp: Template): SyntheticData = ???

  override def generateTable(
      stmt: Statement,
      schema: String,
      table: String,
      tableSettings: Value,
      dbTemplate: Template,
      logger: Logger
  ): List[List[Any]] = {
    val rowCount = Try(tableSettings("row_count").num.toInt) match {
      case Success(v) => v
      case Failure(e) => {
        logger.error("Failed in parsing of row count", e)
        0
      }
    }
    if (rowCount > 0) {
      if (tableSettings("recreate").bool)
        Try(stmt.execute(dbTemplate.dropTable(schema, table))) match {
          case Success(_) => logger.info(s"Successfully drop table if exists = ${schema}.${table}")
          case Failure(e) => logger.error(s"Failed in drop table = ${schema}.${table}", e)
        }

      val cols: List[Value] = Try(tableSettings("columns").arr.toList) match {
        case Success(v) => v
        case Failure(e) => {
          logger.error(s"Failed in parsing columns for table = ${schema}.${table}", e)
          List.empty
        }
      }

      Try {
        stmt.execute(
          dbTemplate.createTable(
            schema,
            table,
            cols.map(x =>
              TableFields(
                x("name").toString().replaceAll("\"", ""),
                x("type").toString().replaceAll("\"", ""),
                Try(x("isPrimaryKey").bool) match {
                  case Success(v) => v
                  case Failure(_) => false
                },
                Try(x("nullable").bool) match {
                  case Success(v) => v
                  case Failure(_) => true
                },
                Try(x("unique").bool) match {
                  case Success(v) => v
                  case Failure(_) => false
                }
              )
            )
          )
        )
      } match {
        case Success(_) => logger.info(s"Successfully create table = ${schema}.${table}")
        case Failure(e) =>
          logger.error(s"Failed in create table = ${schema}.${table}", e)
      }

      val rule = Try(
        cols.map(x =>
          (
            x("type").toString().replaceAll("\"", ""),
            x("data")("action").toString().replaceAll("\"", ""),
            x("data")("rules").obj.toMap
          )
        )
      ) match {
        case Success(v) => v
        case Failure(e) => {
          logger.error(s"Failed in parsing generation data rules for table = ${schema}.${table}", e)
          List.empty
        }
      }

      Try(generator(rule, rowCount, logger)) match {
        case Success(v) =>
          Try(v.transpose) match {
            case Success(v) => v
            case Failure(e) =>
              {
                logger.error(
                  s"Incorrect data was generated or in rules specified incompatible parameters row_count = ${rowCount} and " +
                    s"action = insert data with length != ${rowCount} for table = ${schema}.${table}",
                  e
                )
              }
              List.empty
          }
        case Failure(e) => {
          logger.error(s"Failed in generation data for table = ${schema}.${table}", e)
          List.empty
        }
      }
    } else List.empty
  }

  override def generate(props: PropsConfig, dbType: String, logger: Logger): Unit = {
//    System.setProperty("hadoop.home.dir", "C:\\hadoop")

    var con: Connection = null

    val propPars = new PropertiesParsableImpl()

    val rules = Try(propPars.rules(props.rulesPath)) match {
      case Success(v) => v
      case Failure(e) => {
        logger.error("Failed in parsing rules", e)
        List.empty
      }
    }

    Class.forName(props.dbConfig.driver)
    con = Try(
      DriverManager.getConnection(
        props.dbConfig.connectionUrl,
        props.dbConfig.user,
        props.dbConfig.password
      )
    ) match {
      case Success(v) => {
        logger.info(
          s"Successfully created connection to database = ${props.dbConfig.connectionUrl}"
        )
        v
      }
      case Failure(e) => {
        logger.error(s"Failed in connection to database = ${props.dbConfig.connectionUrl}", e)
        con
      }
    }
    val stmt: Statement = Try(con.createStatement()) match {
      case Success(v) => v
      case Failure(e) => {
        logger.error(
          s"Failed create statement connection for database = ${props.dbConfig.connectionUrl}",
          e
        )
        null
      }
    }

    val dbTemplate = if (dbType == "Hive") new HiveTemplateImpl() else new PostgresTemplateImpl()

    for (schemaSettings <- rules) {
      val schema = Try(schemaSettings("schema").toString().replaceAll("\"", "")) match {
        case Success(v) => v
        case Failure(e) => {
          logger.error("Failed in parsing schema name", e)
          ""
        }
      }
      if (schema.isEmpty)
        logger.warning(s"Incorrect specified database schema name in rules")
      else {
        stmt.execute(dbTemplate.createSchema(schema))

        val tables = Try(schemaSettings("tables").arr.toList) match {
          case Success(v) => v
          case Failure(e) => {
            logger.error("Failed in parsing tables", e)
            List.empty
          }
        }

        for (tableSettings <- tables) {
          val table = Try(tableSettings("name").toString().replaceAll("\"", "")) match {
            case Success(v) => v
            case Failure(e) => {
              logger.error(s"Failed in parsing table name for schema = ${schema}", e)
              ""
            }
          }

          if (table.nonEmpty) {
            val insertQueries = Try(
              generateTable(stmt, schema, table, tableSettings, dbTemplate, logger)
                .map(x => dbTemplate.insertIntoTable(schema, table, x))
            ) match {
              case Success(v) => v
              case Failure(e) => {
                logger.error(
                  s"Failed in generation insert queries for table = ${schema}.${table}",
                  e
                )
                List.empty
              }
            }

            if (insertQueries.nonEmpty) {
              insertQueries.map(stmt.execute)
              logger.info(
                s"${insertQueries.length} row(s) inserted into table = ${schema}.${table}"
              )
            }
          } else logger.warning(s"Incorrect specified table name in rules for schema = ${schema}")
        }
      }
    }

    Try(con.close()) match {
      case Success(_) =>
        logger.info(
          s"Successfully connection closed for database = ${props.dbConfig.connectionUrl}"
        )
      case Failure(e) =>
        logger.error(s"Failed closing connection for database = ${props.dbConfig.connectionUrl}", e)
    }
  }
}
