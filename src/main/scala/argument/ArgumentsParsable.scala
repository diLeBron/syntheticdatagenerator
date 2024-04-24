package argument

import config.ArgumentsConfig

trait ArgumentsParsable {
  def config(l: List[String]): ArgumentsConfig = {
    new ArgumentsConfig {
      override val pathToSyntheticRules: Option[String] = Some(l.head)
      override val databaseType: Option[String] = Some(l.last)
    }
  }
}
