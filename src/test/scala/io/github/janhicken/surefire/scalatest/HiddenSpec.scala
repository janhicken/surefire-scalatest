package io.github.janhicken.surefire.scalatest

import org.scalatest.DoNotDiscover
import org.scalatest.flatspec.AnyFlatSpec

@DoNotDiscover
class HiddenSpec extends AnyFlatSpec {
  it should "not be discovered" in ()
}
