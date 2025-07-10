package ru.digitalleague.syntheticdatagenerator.source.synthetic

import ru.digitalleague.syntheticdatagenerator.argument.{AppParamsConfig, PropertiesParsableImpl}
import ru.digitalleague.syntheticdatagenerator.config.PropsConfig
import ru.digitalleague.syntheticdatagenerator.service.{
  DatabaseConfigMap,
  DatabaseConnectionPoolImpl,
  GeneratedData
}
import ru.digitalleague.syntheticdatagenerator.source.TableFields
import ru.digitalleague.syntheticdatagenerator.source.synthetic.SampleGenerator.generator
import ru.digitalleague.syntheticdatagenerator.streams.flink.FlinkSinkToKafkaAppRun
import ru.digitalleague.syntheticdatagenerator.streams.kafka.Client.createTopic
import ujson.Value
import ru.digitalleague.syntheticdatagenerator.utils.Json.convertToJson
import ru.digitalleague.syntheticdatagenerator.utils.{
  GenerationType,
  HiveTemplateImpl,
  PostgresOlder10vTemplateImpl,
  PostgresTemplateImpl,
  SimpleLogger,
  Template
}

import java.sql.{Connection, DriverManager, ResultSet, Statement}
import scala.sys.exit
import scala.util.{Failure, Success, Try}

trait SyntheticGenerator {
//  def generate(tmp: Template): SyntheticData

  def generate(
      props: PropsConfig,
      config: Option[AppParamsConfig],
      logger: SimpleLogger,
      genType: String,
      srcType: String,
      targetType: String,
      rules: List[Value],
      supportedDb: List[String],
      kafka: String
  ): Unit

  def generateTable(
      stmtSrc: Statement,
      stmtTarget: Statement,
      schema: String,
      table: String,
      tableSettings: Value,
      dbTemplate: Template,
      logger: SimpleLogger,
      genType: String,
      isDatabase: Boolean
  ): GeneratedData
}

class SyntheticGeneratorImpl extends SyntheticGenerator {
//  override def generate(tmp: Template): SyntheticData = ???

  override def generateTable(
      stmtSrc: Statement,
      stmtTarget: Statement,
      schema: String,
      table: String,
      tableSettings: Value,
      dbTemplate: Template,
      logger: SimpleLogger,
      genType: String,
      isDatabase: Boolean
  ): GeneratedData = {
    val rowCount = Try(tableSettings("row_count").num.toInt) match {
      case Success(v) => v
      case Failure(e) => {
        logger.error("Failed in parsing of row count", e)
        0
      }
    }
    if (rowCount > 0) {
      if (isDatabase & tableSettings("recreate").bool) {
        val dropQuery = dbTemplate.dropTable(schema, table)
        Try(stmtTarget.execute(dropQuery)) match {
          case Success(_) => logger.info(s"Successfully drop table if exists = ${schema}.${table}")
          case Failure(e) =>
            logger.error(s"Failed in drop table = ${schema}.${table}. Query: `${dropQuery}``", e)
        }
      }

      val cols: List[Value] = Try(tableSettings("columns").arr.toList) match {
        case Success(v) => v
        case Failure(e) => {
          logger.error(s"Failed in parsing columns for table = ${schema}.${table}", e)
          List.empty
        }
      }

      if (isDatabase) {
        Try {
          stmtTarget.execute(
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

      val query: String =
        if (genType == "semi-synthetic") Try(tableSettings("source_db_query").toString()) match {
          case Success(v) => v.replaceAll("\"", "")
          case Failure(_) => {
            logger.warning(
              s"For source_db_query set `SELECT 1 WHERE false` since it was specified for table = ${schema}.${table}"
            )
            "SELECT 1 WHERE false"
          }
        }
        else "SELECT 1 WHERE false"

      val replication: Boolean =
        if (genType == "semi-synthetic") Try(tableSettings("replication").bool) match {
          case Success(v) => v
          case Failure(_) => {
            logger.warning(
              s"For replication set TRUE since it was specified for table = ${schema}.${table}"
            )
            true
          }
        }
        else false

      val src: ResultSet = if (genType == "semi-synthetic") stmtSrc.executeQuery(query) else null
      val srcRows = if (genType == "semi-synthetic") {
        src.last()
        src.getRow
      } else 0

      if (srcRows == 0 & genType == "semi-synthetic") {
        logger.warning(s"No data was got from source for query: ${query}")
        GeneratedData(List.empty, List.empty)
      } else {
        Try(generator(rule, rowCount, logger, src, replication)) match {
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
      }
    } else GeneratedData(List.empty, List.empty)
  }

  override def generate(
      props: PropsConfig,
      config: Option[AppParamsConfig],
      logger: SimpleLogger,
      genType: String,
      srcType: String,
      targetType: String,
      rules: List[Value],
      supportedDb: List[String],
      kafka: String
  ): Unit = {
//    System.setProperty("hadoop.home.dir", "C:\\hadoop")
    val srcIsDb = supportedDb.contains(srcType)
    val targetIsDB = supportedDb.contains(targetType)
    val targetIsKafka = kafka == targetType

    val dbOnly = srcIsDb & targetIsDB
    val dbExist = srcIsDb | targetIsDB
    val dbOneSrcTarget = srcType == targetType & dbExist

    val dbConn = new DatabaseConnectionPoolImpl()
    val srcDbConf: DatabaseConfigMap = DatabaseConfigMap("src", props.dbConfigSrc)
    val dbConnUrlStr = s"[${props.dbConfigSrc.connectionUrl}, ${props.dbConfigTarget.connectionUrl}]"
    val dbConnectionPool: Map[String, Connection] = Try(
      if (dbOneSrcTarget) {
        dbConn.connectionPool(List(srcDbConf))
      } else
        dbConn.connectionPool(List(srcDbConf, DatabaseConfigMap("target", props.dbConfigTarget)))
    ) match {
      case Success(v) => {
        logger.info(s"Successfully created connection to database(s): ${dbConnUrlStr}")
        v
      }
      case Failure(e) => {
        logger.error(s"Failed in connection to databases =  ${dbConnUrlStr}", e)
        Map.empty
      }
    }

    if (dbExist & dbConnectionPool.isEmpty) throw new Exception("No connections to databases")

    val stmtSrc: Statement =
      if (srcIsDb) {
        Try(
          dbConnectionPool("src")
            .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
        ) match {
          case Success(v) => v
          case Failure(e) => {
            logger.error(
              s"Failed create statement connection for database = ${props.dbConfigSrc.connectionUrl}",
              e
            )
            null
          }
        }
      } else null

    val stmtTarget: Statement = {
      if (dbOnly) stmtSrc
      else if (targetIsDB) {
        Try(
          dbConnectionPool("target")
            .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
        ) match {
          case Success(v) => v
          case Failure(e) => {
            logger.error(
              s"Failed create statement connection for database = ${props.dbConfigSrc.connectionUrl}",
              e
            )
            null
          }
        }
      }
      else null
    }

    val dbName = config.get.target.name.toLowerCase
    val dbVersion = props.dbConfigSrc.dbVersion
    val dbTemplate = dbName match {
      case "hive" => new HiveTemplateImpl()
      case "postgres" =>
        if (dbVersion != 0 & dbVersion < 10) new PostgresOlder10vTemplateImpl()
        else new PostgresTemplateImpl()
      case "greenplum" =>
        if (dbVersion != 0 & dbVersion < 10) new PostgresOlder10vTemplateImpl()
        else new PostgresTemplateImpl()
      case dbType => {
        if (targetIsDB) {
          logger.warning(
            s"Unsupported target database type specified: `${dbType}`. Supported: [hive, postgres]"
          )
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

      if (targetIsDB) {
        if (schema.isEmpty) logger.warning(s"Incorrect specified database schema name in rules")
        else if (List("postgres", "greenplum").contains(dbName) & dbVersion != 0 & dbVersion < 10) {
          stmtTarget.execute(dbTemplate.dropSchema(schema))
          stmtTarget.execute(dbTemplate.createSchema(schema))
        } else stmtTarget.execute(dbTemplate.createSchema(schema))
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
          if (targetIsDB) {
            val generatedData = Try(
              generateTable(
                stmtSrc,
                stmtTarget,
                schema,
                table,
                tableSettings,
                dbTemplate,
                logger,
                genType,
                true
              )
            ) match {
              case Success(v) => v
              case Failure(e) => {
                logger.error(
                  s"Failed in generation data for table = ${schema}.${table}",
                  e
                )
                GeneratedData(List.empty, List.empty)
              }
            }

            val insertQueries = Try(
              generatedData.data.map(x => dbTemplate.insertIntoTable(schema, table, x))
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
              insertQueries.map(stmtTarget.execute)
              logger.info(
                s"${insertQueries.length} row(s) inserted into table = ${schema}.${table}"
              )
            }
          }

          if (targetIsKafka) {
            val topicName = Try(
              tableSettings("kafka_topic").toString().replaceAll("\"", "")
            ) match {
              case Success(v) => v
              case Failure(e) => {
                logger.error(
                  s"Failed in parsing kafka topic for table = ${schema}.${table}. " +
                    s"Set kafka topic = ${schema}.${table}",
                  e
                )
                s"${schema}.${table}"
              }
            }

            Try(createTopic(props.kafkaConfigSrc.brokers, topicName)) match {
              case Success(_) => logger.info(s"Successfully created kafka topic = ${topicName}")
              case Failure(_) => logger.warning(s"Failed in creating kafka topic = ${topicName}")
            }

            val data = (Try(
              generateTable(
                stmtSrc,
                stmtTarget,
                schema,
                table,
                tableSettings,
                dbTemplate,
                logger,
                genType,
                false
              )
            ) match {
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
            FlinkSinkToKafkaAppRun(props.kafkaConfigTarget.brokers, data, topicName)
            logger.info(s"${data.length} message(s) write to kafka topic = ${topicName}")
            logger.info(s"Terminated stream with sink to kafka for topic = ${topicName}")
          }
        } else logger.warning(s"Incorrect specified table name in rules for schema = ${schema}")
      }
    }

    if (dbExist) {
      Try(dbConnectionPool.foreach(x => x._2.close())) match {
        case Success(_) =>
          logger.info(
            s"Successfully connection closed for database(s) = ${dbConnUrlStr}"
          )
        case Failure(e) =>
          logger.error(
            s"Failed closing connection for database(s) = ${dbConnUrlStr}",
            e
          )
      }
    }
  }
}
