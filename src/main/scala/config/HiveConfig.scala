package config

import org.apache.spark.sql.SparkSession

trait HiveConfig extends PropertiesConfig {
  val spark: SparkSession
}
