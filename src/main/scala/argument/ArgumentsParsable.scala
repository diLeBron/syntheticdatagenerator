package argument

import scopt.{OParser, Read}
import config.ArgumentsConfig
import utils.{DataTypes, GenerationTypes}

trait ArgumentsParsable {
  def config(args: Array[String]): Option[ArgumentsConfig]
  def configTest(args: Array[String]): Option[ArgumentsConfig]
}

case class ApplicationParamsConfig(
    credsConfig: CredsConfig = CredsConfig("NoName"),
    dataConfig: DataConfig = DataConfig()
) extends ArgumentsConfig
case class AppParamsConfig(
    credsConfig: CredsConfig = CredsConfig("NoName"),
    srcDataType: DataTypes = DataTypes("NoDef"),
    genType: GenerationTypes = GenerationTypes("NoDef")
) extends ArgumentsConfig
case class CredsConfig(authorName: String) extends ArgumentsConfig
case class DataConfig(
    srcDataType: DataTypes = DataTypes("NoDef"),
    genType: GenerationTypes = GenerationTypes("NoDef")
) extends ArgumentsConfig

class ArgumentsParsableImpl extends ArgumentsParsable {
  implicit val readCredsConfig: Read[CredsConfig] = Read.reads(a => CredsConfig(a))
  implicit val readDataTypes: Read[DataTypes] = Read.reads(DataTypes)
  implicit val readGenerationTypes: Read[GenerationTypes] = Read.reads(GenerationTypes)
  implicit val readDataConfig: Read[DataConfig] = Read.reads(s =>
    DataConfig(
      DataTypes(s.split("&").head),
      GenerationTypes(s.split("&").last)
    )
  )

  def config(args: Array[String]): Option[ArgumentsConfig] = {
    val builder = OParser.builder[AppParamsConfig]
    val parser = {
      import builder._
      OParser.sequence(
        programName("SyntheticDataGenerator"),
        head("SyntheticDataGenerator", "0.1.0"),
        opt[CredsConfig]('a', "authorName")
          .action((x, c) => c.copy(credsConfig = x))
          .text("authorName is CredsConfig property"),
        opt[DataTypes]('s', "srcDataType")
          .action((x, c) => c.copy(srcDataType = x))
          .validate(x =>
            if (List("hive", "postgres").contains(x.name.toLowerCase())) success
            else failure("Supported source data types: [Hive, Postgres]"))
          .text("srcDataType is DataTypes property"),
        opt[GenerationTypes]('g', "genType")
          .action((x, c) => c.copy(genType = x))
          .validate(x =>
            if (List("synthetic", "semi-synthetic").contains(x.name.toLowerCase())) success
            else failure("Supported generation data types: [synthetic, semi-synthetic]"))
          .text("genType is GenerationTypes property"),
      )
    }
    OParser.parse(parser, args, AppParamsConfig())
  }

  override def configTest(args: Array[String]): Option[ArgumentsConfig] = {
    val builder = OParser.builder[ApplicationParamsConfig]
    val parser = {
      import builder._
      OParser.sequence(
        programName("scopt"),
        head("scopt", "4.x"),
        opt[CredsConfig]('a', "authorName")
          .action((x, c) => c.copy(credsConfig = x))
          .text("authorName is an CredsConfig property"),
        opt[DataConfig]('d', "data")
          .action((x, c) => c.copy(dataConfig = x))
          .text("data is DataConfig property")
      )
    }
    OParser.parse(parser, args, ApplicationParamsConfig())
  }
}
