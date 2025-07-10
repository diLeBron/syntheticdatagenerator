package ru.digitalleague.syntheticdatagenerator

import argument.{AppParamsConfig, ArgumentsParsableImpl, PropertiesParsableImpl}
import ch.qos.logback.classic.{Level, Logger}
import config.{DatabaseConfig, KafkaConfig, PropsConfig}
import org.slf4j.LoggerFactory
import ru.digitalleague.syntheticdatagenerator.streams.flink.FlinkKafkaSinkToKafkaAppRun
import source.synthetic.SyntheticGeneratorImpl
import utils.SimpleLogger

import scala.sys.exit
import scala.util.{Failure, Success, Try}

object Main {
  def main(args: Array[String]): Unit = {
    val supportedDb = List("hive", "postgres", "greenplum")
    val kafka = "kafka"
    val libs = List("org.apache", "akka.event")

    libs.foreach { lib =>
      val root = LoggerFactory.getLogger(lib).asInstanceOf[Logger]
      root.setLevel(Level.OFF)
    }

    val logger = new SimpleLogger()

    val argsPars = new ArgumentsParsableImpl()
    val cfg = Try(argsPars.config(args)) match {
      case Success(v) => v
      case Failure(e) => {
        logger.error("Failed in parsing arguments", e)
        Some(AppParamsConfig())
      }
    }

    val propPars = new PropertiesParsableImpl()

    val props = Try(propPars.config("application.properties")) match {
      case Success(v) => v
      case Failure(e) => {
        logger.error("Failed in parsing config file application.properties", e)
        PropsConfig(
          DatabaseConfig("", "", "", "", 0),
          DatabaseConfig("", "", "", "", 0),
          "",
          KafkaConfig(""),
          KafkaConfig("")
        )
      }
    }

    val mode = Try(cfg.get.mode) match {
      case Success(v) =>
        v match {
          case "NoDef" => {
            logger.warning(
              "Set default app mode = batch because not specified in arguments. " +
                "For another mode pLease specify throw args -m or --mode. Supported: [batch, stream]"
            )
            "batch"
          }
          case _ => {
            logger.info(s"App mode = ${v.toLowerCase} was specified")
            v.toLowerCase
          }
        }
      case Failure(e) => {
        logger.error("Failed in parsing app mode", e)
        "NoDef"
      }
    }

    val genType = Try(cfg.get.genType.name) match {
      case Success(v) =>
        v match {
          case "NoDef" => {
            logger.warning(
              "Set default app genType = synthetic because not specified in arguments. " +
                "For another genType please specify throw args -g or --genType. Supported: [synthetic, semi-synthetic]"
            )
            "synthetic"
          }
          case _ => {
            logger.info(s"Generation Type = ${v.toLowerCase} was specified")
            v.toLowerCase
          }
        }
      case Failure(e) => {
        logger.error("Failed in parsing genType", e)
        "NoDef"
      }
    }

    val srcType = Try(cfg.get.source.name) match {
      case Success(v) =>
        v match {
          case "NoDef" => {
            logger.warning(
              "Not specified Data Source Type. Please specify one of following supported sources: " +
                "[Hive, Postgres, Greenplum, Kafka]"
            )
            v
          }
          case _ => {
            logger.info(s"Data Source Type = ${v.toLowerCase} was specified")
            v.toLowerCase
          }
        }
      case Failure(e) => {
        logger.error("Failed in parsing Data Source Type", e)
        "NoDef"
      }
    }

    val targetType = Try(cfg.get.target.name) match {
      case Success(v) =>
        v match {
          case "NoDef" => {
            logger.warning(
              "Not specified Data Target Type. Please specify one of following supported targets: " +
                "[Hive, Postgres, Greenplum, Kafka]"
            )
            v
          }
          case _ => {
            logger.info(s"Data Target Type = ${v.toLowerCase} was specified")
            v.toLowerCase
          }
        }
      case Failure(e) => {
        logger.error("Failed in parsing Data Target Type", e)
        "NoDef"
      }
    }

    if (genType == "semi-synthetic" & List(mode, srcType, targetType).contains("NoDef"))
      throw new Exception(
        "Required params was not specified or raised error in parsing for [genType, mode, srcType, targetType]"
      )

    val rules = Try(propPars.rules(props.rulesPath)) match {
      case Success(v) => v
      case Failure(e) => {
        logger.error("Failed in parsing rules", e)
        List.empty
      }
    }

    if (mode == "batch" & List("synthetic", "semi-synthetic").contains(genType)) {
      val syntheticGenerator = new SyntheticGeneratorImpl()
      syntheticGenerator.generate(
        props,
        cfg,
        logger,
        genType,
        srcType,
        targetType,
        rules,
        supportedDb,
        kafka
      )
    } else if (mode == "stream" & srcType == targetType & srcType == "kafka") {
      val streamConfig = Try(rules.head("stream").obj.toMap) match {
        case Success(v) => v
        case Failure(e) => {
          logger.error("Failed in parsing stream rules", e)
          exit(1)
        }
      }

      val srcKafkaTopics = Try(
        streamConfig("kafka.src.topics").arr.toList.map(x => x.toString().replaceAll("\"", ""))
      ) match {
        case Success(v) => v
        case Failure(e) => {
          logger.error(
            s"Failed in parsing stream source kafka topics",
            e
          )
          exit(1)
        }
      }
      val targetKafkaTopic = Try(
        streamConfig("kafka.target.topic").toString().replaceAll("\"", "")
      ) match {
        case Success(v) => v
        case Failure(e) => {
          logger.error(
            s"Failed in parsing stream target kafka topic",
            e
          )
          exit(1)
        }
      }

      logger.info(
        s"Start stream with source topics = ${srcKafkaTopics} and sink to target kafka topic = ${targetKafkaTopic}"
      )
      FlinkKafkaSinkToKafkaAppRun(
        props.kafkaConfigSrc.brokers,
        props.kafkaConfigTarget.brokers,
        srcKafkaTopics,
        targetKafkaTopic
      )
      logger.info(
        s"Terminated stream with source topics = ${srcKafkaTopics} and sink to target kafka topic = ${targetKafkaTopic}"
      )
    } else {
      logger.warning(
        "No specified app mode. Please specify throw args -m or --mode. Supported: [batch, stream]"
      )
      exit(1)
    }

  }
}
