package data
import cats.effect.IO
import config.SourceConfig
import generator.GeneratorError
import org.apache.spark.sql.{DataFrame, SparkSession}

class HiveDatabase extends Database {
  override def load(sc: SourceConfig, spark: SparkSession): IO[Either[GeneratorError, DataFrame]] = ???
  override def save() = ???
}
