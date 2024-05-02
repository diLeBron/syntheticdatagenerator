package argument

import config.ArgumentsConfig

trait ArgumentsParsable {
  def config(l: Array[String]): ArgumentsConfig
}
