package config

class SourceConfigImpl(conf: PostgresConfig) extends SourceConfig {

  override def getConfig(): Postgres = {
    conf.getConfig()
  }
}
