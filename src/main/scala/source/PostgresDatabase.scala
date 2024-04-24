package source

import cats.effect.IO
import config.SourceConfig
import org.apache.spark.sql.DataFrame
import service.GeneratorError

class PostgresDatabase() extends Database {
  override def load(sc: SourceConfig, query: String): IO[Either[GeneratorError, DataFrame]] = ???

  override def save(df: DataFrame): IO[Either[GeneratorError, Unit]] = ???
}