package config

import argument.ArgumentsParsableArgs
import org.apache.spark.sql.SparkSession

import java.util.Properties

trait PostgresConfig extends PropertiesConfig {
  def getConfig(): Postgres
}
