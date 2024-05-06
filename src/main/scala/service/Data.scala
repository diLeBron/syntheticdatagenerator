package service

import cats.effect.IO
import config.SourceConfig
import org.apache.spark.sql.{DataFrame, SparkSession}

trait Data {
  def load(spark: SparkSession, sc: SourceConfig, query: String): IO[Either[GeneratorError, DataFrame]]
  def save(df: DataFrame): IO[Either[GeneratorError, Unit]]
}
