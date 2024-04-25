package argument

import config.ArgumentsConfig

trait ArgumentsParsable {
  def config(l: List[String]): ArgumentsConfig

  def parse(args: Array[String]): Map[String, String] = {
    (for {
      str <- args
      splitValues = str.split("=")
      if splitValues.length == 2
    } yield splitValues(0) -> splitValues(1)).toMap
  }
}
