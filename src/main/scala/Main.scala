import argument.{ArgumentsParsableImpl, PropertiesParsableImpl}
import source.synthetic.SyntheticGeneratorImpl


object Main {
  def main(args: Array[String]): Unit = {
    val argsPars = new ArgumentsParsableImpl()
    val cfg = argsPars.config(args)

    val propPars = new PropertiesParsableImpl()

    val props = propPars.config("application.properties")

    val syntheticGenerator = new SyntheticGeneratorImpl()

    syntheticGenerator.generate(props, cfg.get.srcDataType.name)
  }
}
