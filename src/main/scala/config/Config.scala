package config

trait Config

trait ArgumentsConfig extends Config {
  val pathToSyntheticRules: Option[String]
  val databaseType: Option[String]
}

trait PropertiesConfig extends Config {
  val url: Option[String]
  val schema: Option[String]
}
