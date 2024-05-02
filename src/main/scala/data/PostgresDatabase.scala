package data
import cats.effect.IO
import config.{PostgresConfig, SourceConfig}
import generator.GeneratorError
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.util.Properties

class PostgresDatabase extends Database {
  override def load(sc: SourceConfig, spark: SparkSession): IO[Either[GeneratorError, DataFrame]] = {
    val conf = sc.getConfig()
    val res = for {
      url <- conf.url
      props <- conf.props
    } yield (url, props)
    res match {
      case Some((url, props)) =>
        val df = spark.read.jdbc(url, "tableName", props)
        IO(Right(df))
    }
  }
  override def save() = ???
}
