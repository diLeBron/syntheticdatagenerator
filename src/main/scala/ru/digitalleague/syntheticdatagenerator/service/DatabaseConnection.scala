package ru.digitalleague.syntheticdatagenerator.service

import ru.digitalleague.syntheticdatagenerator.config.DatabaseConfig

import java.sql.{Connection, DriverManager}
import scala.util.{Failure, Success, Try}

case class DatabaseConfigMap(dbType: String, config: DatabaseConfig)
//case class DatabaseConnectionMap(dbType: String, connection: Connection)

trait DatabaseConnection {
  def connection(config: DatabaseConfig): Connection
}

trait DatabaseConnectionPool extends DatabaseConnection {
  def connectionPool(configs: List[DatabaseConfigMap]): Map[String, Connection]
}

class DatabaseConnectionPoolImpl extends DatabaseConnectionPool {
  override def connectionPool(configs: List[DatabaseConfigMap]): Map[String, Connection] = {
    configs.map(x => x.dbType -> connection(x.config)).toMap
  }

  override def connection(config: DatabaseConfig): Connection = {
    Class.forName(config.driver)
    DriverManager.getConnection(
      config.connectionUrl,
      config.user,
      config.password
    )
  }
}
