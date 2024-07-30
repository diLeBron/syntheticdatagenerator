import argument.{AppParamsConfig, ArgumentsParsableImpl, PropertiesParsableImpl}
import config.{DatabaseConfig, PropsConfig}
import utils.Logger
import source.synthetic.SyntheticGeneratorImpl

import scala.util.{Failure, Success, Try}


object Main {
  def main(args: Array[String]): Unit = {
    val logger = new Logger()

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
        PropsConfig(DatabaseConfig("", "", "", ""), "")
      }
    }

    val syntheticGenerator = new SyntheticGeneratorImpl()

    syntheticGenerator.generate(props, cfg.get.srcDataType.name, logger)
  }
}
