import argument.PropertiesParsable
import config.PropertiesConfig

import java.util.Properties
import scala.jdk.CollectionConverters.PropertiesHasAsScala

object Tester extends App {

  val javaProps = new Properties
  javaProps.put("first_name", "Adam")
  javaProps.put("last_name", "Scott")

  println(javaProps.asScala.toMap)
}
