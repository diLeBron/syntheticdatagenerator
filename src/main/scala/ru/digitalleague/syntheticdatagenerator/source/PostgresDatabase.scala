package ru.digitalleague.syntheticdatagenerator.source

//import cats.effect.IO
//import ru.digitalleague.syntheticdatagenerator.config.SourceConfig
//import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
//import ru.digitalleague.syntheticdatagenerator.service.GeneratorError
//
//class PostgresDatabase() extends Database {
//  override def load(spark: SparkSession, sc: SourceConfig, query: String): IO[Either[GeneratorError, DataFrame]] = {
//    val conf = sc.getConfig()
//    val res = for {
//      url <- conf.url
//      props <- conf.props
//      tableName <- conf.table
//    } yield (url, props, tableName)
//    res match {
//      case Some((url, props, tableName)) => IO(Right(spark.read.jdbc(url, tableName, props)))
//    }
//  }
//
//  override def save(df: DataFrame): IO[Either[GeneratorError, Unit]] = {
//    IO(Right(df.write.mode(SaveMode.Overwrite).saveAsTable("tableName")))
//  }
//}