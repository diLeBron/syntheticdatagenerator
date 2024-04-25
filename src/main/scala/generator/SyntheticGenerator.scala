package generator

import source.SyntheticData
import template.Template

trait SyntheticGenerator {
  def generate(tmp: Template): SyntheticData
}
