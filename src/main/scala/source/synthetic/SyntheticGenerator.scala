package source.synthetic

import utils.Template

trait SyntheticGenerator {
  def generate(tmp: Template): SyntheticData
}

