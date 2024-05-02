package config

trait PropertiesConfig extends Config {
  def getConfig(): Postgres
}
