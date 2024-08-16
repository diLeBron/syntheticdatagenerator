package ru.digitalleague.syntheticdatagenerator.config

trait PostgresConfig extends PropertiesConfig {
  def getConfig(): Postgres
}
