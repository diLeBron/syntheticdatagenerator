package ru.digitalleague.syntheticdatagenerator.service

import org.apache.spark.sql.SparkSession

trait SparkSessionCreatable {
  def create(): SparkSession
}

class SparkSessionCreatableImpl extends SparkSessionCreatable {
  def create(): SparkSession = {
    SparkSession.builder.master("local[*]")
      .appName("SyntheticDataGenerator")
      .enableHiveSupport()
      .getOrCreate()
  }
}