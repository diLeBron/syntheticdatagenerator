package config

import java.util.Properties

case class Postgres(
    url: Option[String] = None,
    props: Option[Properties] = Some(new Properties()),
    table: Option[String] = None
) extends ArgumentsConfig
