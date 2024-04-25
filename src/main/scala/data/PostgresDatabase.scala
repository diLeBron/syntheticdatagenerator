package data
import config.SourceConfig
import org.apache.spark.sql.{DataFrame, SparkSession}
import config.PostgresConfig
import spark.SparkSessionCreatable

import java.util.Properties

class PostgresDatabase extends Database {
  override def load(sc: SourceConfig) = ???
  override def save() = ???

  def readTable(tableName: String, config: PostgresConfig, spark: SparkSession): DataFrame = {
    val getConfig = config.getConfig("host", 8443, "database")
    val url = getConfig._1
    val props = getConfig._2
    spark.read.jdbc(url, tableName, props)
  }
}
