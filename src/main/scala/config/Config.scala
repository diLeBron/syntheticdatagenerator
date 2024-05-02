package config

trait Config

trait ArgumentsConfig extends Config

trait PropertiesConfig extends Config

case class PropsConfig(url: Option[String], schema: Option[String]) extends PropertiesConfig
