import argument.{ArgumentsParsableImpl, PropertiesParsableImpl}

object Main {
  def main(args: Array[String]): Unit = {
    val argsPars = new ArgumentsParsableImpl()
    val cfg = argsPars.config(args)

    cfg match {
      case Some(_) => println("Config was successfully parsed")
      case _ => println("Failed config parsing")
    }

    val propPars = new PropertiesParsableImpl()

    val props = propPars.config("application.properties")
  }
}
