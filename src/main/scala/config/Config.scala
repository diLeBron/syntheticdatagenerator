package config

trait Config

trait ArgumentsConfig extends Config

trait PropertiesConfig extends Config

trait Rules extends Config

case class DatabaseConfig(connectionUrl: String, driver: String, user: String, password: String) extends PropertiesConfig

case class PropsConfig(dbConfig: DatabaseConfig, rulesPath: String) extends PropertiesConfig

case class SyntheticRules(
    mapRowCount: Map[String, Int],
    fieldRules: Map[String, Map[String, String]],
    tableSettingsMap: Map[String, List[Map[String, String]]]
)
