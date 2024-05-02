package argument

import config.ArgumentsConfig

import java.util.Properties

trait PropertiesParsable {
  def config(properties: Properties): ArgumentsConfig
}
