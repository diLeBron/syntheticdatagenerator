package argument
import config.{ArgumentsConfig, Postgres}
import scopt.OParser

class ArgumentsParsableArgs extends ArgumentsParsable {

  override def config(l: Array[String]): ArgumentsConfig = {
    val builder = OParser.builder[Postgres]
    val parserPostres = {
      import builder._
      OParser.sequence(
        programName("scopt"),
        head("scopt", "4.x"),
        opt[String]('u', "url")
          .required()
          .validate(x => {
            val regexUrl = """jdbc:postgresql:\/\/\S+?:\d+\/\S+""".r
            if (regexUrl.pattern.matcher(x).matches()) success
            else failure("Url is not valid")
          })
          .action((x, c) => c.copy(url = Some(x)))
          .text("url is an string property"),
        head("scopt", "4.x"),
        opt[String]('l', "user")
          .required()
          .action((x, c) => {
            c.props match {
              case Some(properties) =>
                properties.setProperty("username", x)
                c.copy(props = Some(properties))
            }
          })
          .text("user is an string property"),
        opt[String]('p', "pass")
          .required()
          .action((x, c) => {
            c.props match {
              case Some(properties) =>
                properties.setProperty("password", x)
                c.copy(props = Some(properties))
            }
          })
          .text("password is an string property"),
      )
    }
    OParser.parse(parserPostres, l, Postgres()) match {
      case Some(config) =>
        config
      case _ =>
        Postgres()
    }
  }
}
