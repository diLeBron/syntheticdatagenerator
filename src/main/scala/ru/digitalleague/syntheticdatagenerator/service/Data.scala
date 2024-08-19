package ru.digitalleague.syntheticdatagenerator.service

//import cats.effect.IO
import ru.digitalleague.syntheticdatagenerator.config.SourceConfig
//import org.apache.spark.sql.{DataFrame, SparkSession}
//
//trait Data {
//  def load(spark: SparkSession, sc: SourceConfig, query: String): IO[Either[GeneratorError, DataFrame]]
//  def save(df: DataFrame): IO[Either[GeneratorError, Unit]]
//}

case class GeneratedData(data: List[List[Any]], json: List[Map[String, Any]])
