package source

import cats.effect.IO
import config.SourceConfig
import org.apache.spark.sql.{DataFrame, SaveMode}
import service.GeneratorError

class HiveDatabase() extends Database {
  override def load(sc: SourceConfig, query: String): IO[Either[GeneratorError, DataFrame]] = {
    IO(Right(sc.spark.sql(query)))
  }

  override def save(df: DataFrame): IO[Either[GeneratorError, Unit]] = {
    IO(Right(df.write.mode(SaveMode.Overwrite).saveAsTable("emp.employee")))
  }
}