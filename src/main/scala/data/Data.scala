package data

import cats.effect.IO
import config.SourceConfig
import generator.GeneratorError
import org.apache.spark.sql.{DataFrame, SparkSession}

trait Data {
  def load(sc: SourceConfig, spark: SparkSession): IO[util.Either[GeneratorError, DataFrame]]
  def save(): IO[util.Either[GeneratorError, Unit]]
}
