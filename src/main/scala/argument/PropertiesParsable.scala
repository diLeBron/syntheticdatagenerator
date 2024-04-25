package argument

import config.PropertiesConfig

import java.util.Properties
import scala.jdk.CollectionConverters.PropertiesHasAsScala

trait PropertiesParsable {
  def config(): PropertiesConfig

  def parse(properties: Properties): Map[String, String] = {
    properties.asScala.toMap
  }
}
