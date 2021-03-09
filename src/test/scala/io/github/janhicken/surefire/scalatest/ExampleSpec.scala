package io.github.janhicken.surefire.scalatest

import org.scalatest.flatspec.AnyFlatSpec

class ExampleSpec extends AnyFlatSpec {
  "A sample" should "do something without exception" in {
    val x = 1 + 1
    assertResult(2)(x)
  }

  ignore should "ignore something" in ()

  it should "fail dividing by 0" in {
    4 / 0
  }
}
