package argument

import config.{PropertiesConfig, PropsConfig}

import java.io._
import java.util._

trait PropertiesParsable {
  def properties(fileName: String): Properties

  def config(fileName: String): PropertiesConfig
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

  def config(fileName: String): PropertiesConfig = {
    val props = properties(fileName)
    PropsConfig(Some(props.getProperty("url")), Some(props.getProperty("schema")))
  }
}