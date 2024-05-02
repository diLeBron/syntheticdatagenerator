package argument

import scopt.OParser
import config.ArgumentsConfig
import utils.{DataType, GenerationType}

trait ArgumentsParsable {
  def config(args: Array[String]): Option[ArgumentsConfig]
}

case class ApplicationParamsConfig(credsConfig: CredsConfig, dataConfig: DataConfig) extends ArgumentsConfig
case class CredsConfig(authorName: String = "NoName") extends ArgumentsConfig
case class DataConfig(srcDataType: DataType, genType: GenerationType) extends ArgumentsConfig

case class ArgsConfig(authorName: String = "NoName",
                      srcDataType: String = "Hive",
                      genType: String = "Synthetic") extends ArgumentsConfig

class ArgumentsParsableImpl extends ArgumentsParsable {
  def config(args: Array[String]): Option[ArgumentsConfig] = {
    val builder = OParser.builder[ArgsConfig]
    val parser = {
      import builder._
      OParser.sequence(
        programName("scopt"),
        head("scopt", "4.x"),
        opt[String]('a', "authorName")
          .action((x, c) => c.copy(authorName = x))
          .text("authorName is an string property"),
        opt[String]('s', "srcDataType")
          .required()
          .action((x, c) => c.copy(srcDataType = x))
          .text("srcDataType is an string property"),
        opt[String]('g', "genType")
          .action((x, c) => c.copy(authorName = x))
          .text("genType is an string property"),
      )
    }
    OParser.parse(parser, args, ArgsConfig())
  }
}
