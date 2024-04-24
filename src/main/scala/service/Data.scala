package service

import cats.effect.IO
import config.SourceConfig
import org.apache.spark.sql.DataFrame

trait Data {
  def load(sc: SourceConfig, query: String): IO[Either[GeneratorError, DataFrame]]
  def save(df: DataFrame): IO[Either[GeneratorError, Unit]]
}
