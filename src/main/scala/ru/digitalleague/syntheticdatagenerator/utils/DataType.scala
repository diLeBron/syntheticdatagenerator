package ru.digitalleague.syntheticdatagenerator.utils

trait DataType // mapping typeclasses
class HiveDataType() extends DataType
class PostgresDataType() extends DataType

case class DatabaseType(name: String) extends DataType

trait GenerationType
object Synthetic extends GenerationType
object SemiSynthetic extends GenerationType

case class GenerationTypes(name: String) extends GenerationType