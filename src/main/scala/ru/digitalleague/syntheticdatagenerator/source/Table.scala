package ru.digitalleague.syntheticdatagenerator.source

trait Table { // TODO? Naming

}

case class TableFields(
    name: String,
    dataType: String,
    isPrimaryKey: Boolean = false,
    nullable: Boolean = true,
    unique: Boolean = false
) extends Table
