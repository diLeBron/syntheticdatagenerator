package config

import argument.ArgumentsParsableArgs

class PostgresConfigArgs(args: Array[String], ap: ArgumentsParsableArgs) extends PostgresConfig {
  override def getConfig(): Postgres = {
    ap.config(args) match {
      case pc: Postgres => pc
      case _ => Postgres()
    }
  }
}
