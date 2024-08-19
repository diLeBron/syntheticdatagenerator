package ru.digitalleague.syntheticdatagenerator.source

//import cats.effect.IO
//import ru.digitalleague.syntheticdatagenerator.config.SourceConfig
//import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
//import ru.digitalleague.syntheticdatagenerator.service.GeneratorError
//
//class HiveDatabase() extends Database {
//  override def load(spark: SparkSession, sc: SourceConfig, query: String): IO[Either[GeneratorError, DataFrame]] = {
//    IO(Right(spark.sql(query)))
//  }
//
//  override def save(df: DataFrame): IO[Either[GeneratorError, Unit]] = {
//    IO(Right(df.write.mode(SaveMode.Overwrite).saveAsTable("emp.employee")))
//  }
//}