package ru.digitalleague.syntheticdatagenerator.argument

import ru.digitalleague.syntheticdatagenerator.config.{DatabaseConfig, PropertiesConfig, PropsConfig, SyntheticRules, KafkaConfig}
import ru.digitalleague.syntheticdatagenerator.source.TableFields
import ujson.Value

import java.io._
import java.util._
import scala.collection.immutable.{List, Map}

trait PropertiesParsable {
  def properties(fileName: String): Properties

  def config(fileName: String): PropertiesConfig

  def rules(pathToFile: String): List[Value]
}

class PropertiesParsableImpl extends PropertiesParsable {
  def properties(fileName: String): Properties = {
    val f = new File(fileName)
    val props = new Properties
    val fis = new InputStreamReader(new FileInputStream(f), "UTF-8")
    props.load(fis)
    fis.close()
    props
  }

  def config(fileName: String): PropsConfig = {
    val props = properties(fileName)
    PropsConfig(
      DatabaseConfig(
        props.getProperty("connection.url"),
        props.getProperty("driver.class.name"),
        props.getProperty("user"),
        props.getProperty("password"),
        props.getProperty("database.version", "0").toInt
      ),
      props.getProperty("synthetic.rules.path"),
      KafkaConfig(props.getProperty("kafka.brokers"))
    )
  }

  override def rules(pathToFile: String): List[Value] = {
    val source = scala.io.Source.fromFile(pathToFile)
    val lines =
      try source.mkString
      finally source.close()

    ujson.read(lines).arr.toList
  }
}
