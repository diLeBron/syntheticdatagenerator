package ru.digitalleague.syntheticdatagenerator.services

import org.apache.spark.sql.{DataFrame, SparkSession}

import java.util.Properties

object PostresService {
  def getTablePostres(spark: SparkSession ,url: String, tableName: String, properties: Properties): DataFrame = {
    spark.read.jdbc(url, tableName, properties)
  }
}
