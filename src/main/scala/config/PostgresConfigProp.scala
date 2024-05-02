package config

import argument.ArgumentsParsableProp

import java.util.Properties

class PostgresConfigProp(prop: Properties, ap: ArgumentsParsableProp) extends PostgresConfig {

  override def getConfig(): Postgres = {
    ap.config(prop) match {
      case pc: Postgres => pc
      case _ => Postgres()
    }
  }
}
