package spark

import org.apache.spark.sql.SparkSession

trait SparkSessionCreatable {
  def create(): SparkSession
}
