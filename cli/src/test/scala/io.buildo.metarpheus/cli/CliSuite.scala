package io.buildo.metarpheus
package cli
package test

import org.scalatest._

class CliSuite extends FunSuite {
  test("run main") {
    Cli.main("--config cli/src/test/resources/fixtures/config.scala core/src/test/resources/fixtures".split(" "))
  }

  test("run main with wiro flag") {
    Cli.main("--wiro --config cli/src/test/resources/fixtures/config.scala core/src/test/resources/fixtures".split(" "))
  }

}
