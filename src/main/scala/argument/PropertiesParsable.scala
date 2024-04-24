package argument

import config.PropertiesConfig

import java.io._
import java.util._

trait PropertiesParsable {
  val props: Properties = {
    val f = new File("application.properties")
    val props = new Properties
    val fis = new InputStreamReader(new FileInputStream(f), "UTF-8")
    props.load(fis)
    fis.close()
    props
  }

  def config(): PropertiesConfig = {
    new PropertiesConfig {
      override val url: Option[String] = Some(props.getProperty("url"))
      override val schema: Option[String] = Some(props.getProperty("schema"))
    }
  }
}