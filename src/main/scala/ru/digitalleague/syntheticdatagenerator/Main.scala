package ru.digitalleague.syntheticdatagenerator

import argument.{AppParamsConfig, ArgumentsParsableImpl, PropertiesParsableImpl}
import ch.qos.logback.classic.{Level, Logger}
import config.{DatabaseConfig, KafkaConfig, PropsConfig}
import org.slf4j.LoggerFactory
import source.synthetic.SyntheticGeneratorImpl
import utils.SimpleLogger

import scala.util.{Failure, Success, Try}

object Main {
  def main(args: Array[String]): Unit = {
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
        PropsConfig(DatabaseConfig("", "", "", ""), "", KafkaConfig(""))
      }
    }

    val syntheticGenerator = new SyntheticGeneratorImpl()

    syntheticGenerator.generate(props, cfg, logger)
  }
}
