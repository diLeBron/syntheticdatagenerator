package config

import org.apache.spark.sql.SparkSession

import java.util.Properties

trait PostgresConfig extends PropertiesConfig {
  def getConfig(host: String, port: Int, db: String): (String, Properties) = {
    val properties = new Properties()
    properties.put("username", "user")
    properties.put("password", "pass")

    (s"jdbc:postgresql://${host}:${port}/${db}", properties)
  }
}
