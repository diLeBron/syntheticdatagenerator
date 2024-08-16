package ru.digitalleague.syntheticdatagenerator.config

case class ApplicationConfig(
    arguments: ArgumentsConfig,
    hiveConfig: Option[HiveConfig] = None,
    postgresConfig: Option[PostgresConfig] = None
)
