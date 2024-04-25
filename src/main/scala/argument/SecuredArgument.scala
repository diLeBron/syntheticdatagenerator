package argument

trait SecuredArgument extends Argument {
  override def print(): String = shadedPrint()
  private[argument] def shadedPrint(): String
}
