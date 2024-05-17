import argument.{AppParamsConfig, ArgumentsParsableImpl}

object ArgumentsParsableTest extends App {
  val ap = new ArgumentsParsableImpl
  val arr = Array("-a", "user1", "-s", "hive", "-g", "synthetic")
  ap.config(arr) match {
    case Some(value) => println(value)
    case _ => println("The error")
  }
}
