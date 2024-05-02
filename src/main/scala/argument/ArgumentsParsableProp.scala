package argument
import config.{ArgumentsConfig, Postgres, PropertiesConfig}

import java.util.Properties

class ArgumentsParsableProp extends PropertiesParsable {

  override def config(properties: Properties): ArgumentsConfig = {
    val prop = new Properties()
    prop.setProperty("username", properties.getProperty("username"))
    prop.setProperty("password", properties.getProperty("password"))
    Postgres(
      url = Some(properties.getProperty("url")),
      props = Some(prop)
    )
  }
}
