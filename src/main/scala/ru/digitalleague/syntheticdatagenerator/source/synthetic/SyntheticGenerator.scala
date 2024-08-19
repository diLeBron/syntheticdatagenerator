package ru.digitalleague.syntheticdatagenerator.source.synthetic

import ru.digitalleague.syntheticdatagenerator.argument.{AppParamsConfig, PropertiesParsableImpl}
import ru.digitalleague.syntheticdatagenerator.config.PropsConfig
import ru.digitalleague.syntheticdatagenerator.service.GeneratedData
import ru.digitalleague.syntheticdatagenerator.source.TableFields
import ru.digitalleague.syntheticdatagenerator.source.synthetic.SampleGenerator.generator
import ru.digitalleague.syntheticdatagenerator.streams.flink.FlinkSinkToKafkaAppRun
import ru.digitalleague.syntheticdatagenerator.streams.kafka.Client.createTopic
import ujson.Value
import ru.digitalleague.syntheticdatagenerator.utils.Json.convertToJson
import ru.digitalleague.syntheticdatagenerator.utils.{HiveTemplateImpl, PostgresTemplateImpl, SimpleLogger, Template}

import java.sql.{Connection, DriverManager, Statement}
import scala.sys.exit
import scala.util.{Failure, Success, Try}

trait SyntheticGenerator {
//  def generate(tmp: Template): SyntheticData

  def generate(props: PropsConfig, config: Option[AppParamsConfig], logger: SimpleLogger): Unit

  def generateTable(
                     stmt: Statement,
                     schema: String,
                     table: String,
                     tableSettings: Value,
                     dbTemplate: Template,
                     logger: SimpleLogger,
                     mode: String
  ): GeneratedData
}

class SyntheticGeneratorImpl extends SyntheticGenerator {
//  override def generate(tmp: Template): SyntheticData = ???

  override def generateTable(
                              stmt: Statement,
                              schema: String,
                              table: String,
                              tableSettings: Value,
                              dbTemplate: Template,
                              logger: SimpleLogger,
                              mode: String
  ): GeneratedData = {
    val rowCount = Try(tableSettings("row_count").num.toInt) match {
      case Success(v) => v
      case Failure(e) => {
        logger.error("Failed in parsing of row count", e)
        0
      }
    }
    if (rowCount > 0) {
      if (mode == "batch" & tableSettings("recreate").bool) {
        val dropQuery = dbTemplate.dropTable(schema, table)
        Try(stmt.execute(dropQuery)) match {
          case Success(_) => logger.info(s"Successfully drop table if exists = ${schema}.${table}")
          case Failure(e) => logger.error(s"Failed in drop table = ${schema}.${table}. Query: `${dropQuery}``", e)
        }
      }

      val cols: List[Value] = Try(tableSettings("columns").arr.toList) match {
        case Success(v) => v
        case Failure(e) => {
          logger.error(s"Failed in parsing columns for table = ${schema}.${table}", e)
          List.empty
        }
      }

      if (mode == "batch") {
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
            case Success(v) =>
              GeneratedData(
                v,
                v.map(x => cols.map(x => x("name").toString().replaceAll("\"", "")).zip(x).toMap)
              )
            case Failure(e) =>
              {
                logger.error(
                  s"Incorrect data was generated or in rules specified incompatible parameters row_count = ${rowCount} and " +
                    s"action = insert data with length != ${rowCount} for table = ${schema}.${table}",
                  e
                )
              }
              GeneratedData(List.empty, List.empty)
          }
        case Failure(e) => {
          logger.error(s"Failed in generation data for table = ${schema}.${table}", e)
          GeneratedData(List.empty, List.empty)
        }
      }
    } else GeneratedData(List.empty, List.empty)
  }

  override def generate(props: PropsConfig, config: Option[AppParamsConfig], logger: SimpleLogger): Unit = {
//    System.setProperty("hadoop.home.dir", "C:\\hadoop")

    val mode = Try(config.get.mode) match {
      case Success(v) => v match {
        case "NoDef" => {
          logger.warning("Set default app mode = batch because not specified in arguments. " +
            "For another mode pLease specify throw args -m or --mode. Supported: [batch, stream]")
          "batch"
        }
        case _ => {
          logger.info(s"App mode = ${v.toLowerCase} was specified")
          v.toLowerCase
        }
      }
      case Failure(e) => {
        logger.error("Failed in parsing app mode", e)
        ""
      }
    }

    var con: Connection = null

    val propPars = new PropertiesParsableImpl()

    val rules = Try(propPars.rules(props.rulesPath)) match {
      case Success(v) => v
      case Failure(e) => {
        logger.error("Failed in parsing rules", e)
        List.empty
      }
    }

    if (mode == "batch") {
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
    }

    val stmt: Statement = if (mode == "batch") Try(con.createStatement()) match {
      case Success(v) => v
      case Failure(e) => {
        logger.error(
          s"Failed create statement connection for database = ${props.dbConfig.connectionUrl}",
          e
        )
        null
      }
    } else null

    val dbTemplate = config.get.targetDatabaseType.name.toLowerCase match {
      case "hive" => new HiveTemplateImpl()
      case "postgres" => new PostgresTemplateImpl()
      case dbType => {
        if (mode == "batch") {
          logger.warning(s"Unsupported target database type specified: `${dbType}`. Supported: [hive, postgres]")
          exit(1)
        } else new PostgresTemplateImpl()
      }
    }

    for (schemaSettings <- rules) {
      val schema = Try(schemaSettings("schema").toString().replaceAll("\"", "")) match {
        case Success(v) => v
        case Failure(e) => {
          logger.error("Failed in parsing schema name", e)
          ""
        }
      }

      if (mode == "batch") {
        if (schema.isEmpty) logger.warning(s"Incorrect specified database schema name in rules")
        else stmt.execute(dbTemplate.createSchema(schema))
      }

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
            if (mode == "batch") {
              val generatedData = Try(generateTable(stmt, schema, table, tableSettings, dbTemplate, logger, mode)) match {
                case Success(v) => v
                case Failure(e) => {
                  logger.error(
                    s"Failed in generation data for table = ${schema}.${table}",
                    e
                  )
                  GeneratedData(List.empty, List.empty)
                }
              }

              val insertQueries = Try(generatedData.data.map(x => dbTemplate.insertIntoTable(schema, table, x))) match {
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
            }

            if (mode == "stream") {
              val topicName = Try(tableSettings("kafka_topic").toString().replaceAll("\"", "")) match {
                case Success(v) => v
                case Failure(e) => {
                  logger.error(s"Failed in parsing kafka topic for table = ${schema}.${table}. " +
                    s"Set kafka topic = ${schema}.${table}", e)
                  s"${schema}.${table}"
                }
              }

              Try(createTopic(props.kafkaConfig.brokers, topicName)) match {
                case Success(_) => logger.info(s"Successfully created kafka topic = ${topicName}")
                case Failure(e) => logger.error(s"Failed in creating kafka topic = ${topicName}", e)
              }

              val data = (Try(generateTable(stmt, schema, table, tableSettings, dbTemplate, logger, mode)) match {
                case Success(v) => v
                case Failure(e) => {
                  logger.error(
                    s"Failed in generation data for table = ${schema}.${table}",
                    e
                  )
                  GeneratedData(List.empty, List.empty)
                }
              }).json.map(convertToJson)

              logger.info(s"Start stream with sink to kafka for topic = ${topicName}")
              FlinkSinkToKafkaAppRun(props.kafkaConfig.brokers, data, topicName)
              logger.info(s"${data.length} message(s) write to kafka topic = ${topicName}" )
              logger.info(s"Terminated stream with sink to kafka for topic = ${topicName}")
            }
          } else logger.warning(s"Incorrect specified table name in rules for schema = ${schema}")
        }
    }

    if (mode == "batch") {
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
}
