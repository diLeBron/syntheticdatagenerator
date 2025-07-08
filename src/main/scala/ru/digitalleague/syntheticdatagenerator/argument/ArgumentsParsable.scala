package ru.digitalleague.syntheticdatagenerator.argument

import scopt.{OParser, Read}
import ru.digitalleague.syntheticdatagenerator.config.ArgumentsConfig
import ru.digitalleague.syntheticdatagenerator.utils.{DatabaseType, GenerationTypes}

trait ArgumentsParsable {
  def config(args: Array[String]): Option[AppParamsConfig]
}

case class ApplicationParamsConfig(
    credsConfig: CredsConfig = CredsConfig("NoName"),
    dataConfig: DataConfig = DataConfig()
) extends ArgumentsConfig
case class AppParamsConfig(
                            credsConfig: CredsConfig = CredsConfig("NoName"),
                            targetDatabaseType: DatabaseType = DatabaseType("NoDef"),
                            genType: GenerationTypes = GenerationTypes("NoDef"),
                            mode: String = "NoDef"
) extends ArgumentsConfig
case class CredsConfig(authorName: String) extends ArgumentsConfig
case class DataConfig(
                       srcDataType: DatabaseType = DatabaseType("NoDef"),
                       genType: GenerationTypes = GenerationTypes("NoDef")
) extends ArgumentsConfig

class ArgumentsParsableImpl extends ArgumentsParsable {
  implicit val readCredsConfig: Read[CredsConfig] = Read.reads(a => CredsConfig(a))
  implicit val readDatabaseType: Read[DatabaseType] = Read.reads(DatabaseType)
  implicit val readGenerationTypes: Read[GenerationTypes] = Read.reads(GenerationTypes)
  implicit val readDataConfig: Read[DataConfig] = Read.reads(s =>
    DataConfig(
      DatabaseType(s.split("&").head),
      GenerationTypes(s.split("&").last)
    )
  )

  def config(args: Array[String]): Option[AppParamsConfig] = {
    val builder = OParser.builder[AppParamsConfig]
    val parser = {
      import builder._
      OParser.sequence(
        programName("SyntheticDataGenerator"),
        head("SyntheticDataGenerator", "0.1.0"),
        opt[CredsConfig]('a', "authorName")
          .action((x, c) => c.copy(credsConfig = x))
          .text("authorName is CredsConfig property"),
        opt[DatabaseType]('s', "srcDataType")
          .action((x, c) => c.copy(targetDatabaseType = x))
          .validate(x =>
            if (List("hive", "postgres", "greenplum", "kafka").contains(x.name.toLowerCase())) success
            else failure("Supported sources: [Hive, Postgres, Greenplum, Kafka]"))
          .text("srcDataType is DataTypes property"),
        opt[DatabaseType]('t', "targetDataType")
          .action((x, c) => c.copy(targetDatabaseType = x))
          .validate(x =>
            if (List("hive", "postgres", "greenplum", "kafka").contains(x.name.toLowerCase())) success
            else failure("Supported targets: [Hive, Postgres, Greenplum, Kafka]"))
          .text("targetDataType is DataTypes property"),
        opt[GenerationTypes]('g', "genType")
          .action((x, c) => c.copy(genType = x))
          .validate(x =>
            if (List("synthetic", "semi-synthetic").contains(x.name.toLowerCase())) success
            else failure("Supported generation data types: [synthetic, semi-synthetic]"))
          .text("genType is GenerationTypes property"),
        opt[String]('m', "mode")
          .action((x, c) => c.copy(mode = x))
          .validate(x =>
            if (List("batch", "stream").contains(x.toLowerCase())) success
            else failure("Supported app modes: [batch, stream]"))
      )
    }
    OParser.parse(parser, args, AppParamsConfig())
  }
}
