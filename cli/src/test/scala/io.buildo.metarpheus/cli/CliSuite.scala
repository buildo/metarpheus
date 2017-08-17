package io.buildo.metarpheus
package cli
package test

import org.scalatest._

class CliSuite extends FunSuite {
  val fixturesPath = new java.io.File("core/shared/src/test/resources/fixtures").getAbsolutePath

  test("run main") {
    Cli.main(s"--config cli/src/test/resources/fixtures/config.json $fixturesPath".split(" "))
  }

  test("run main with wiro flag") {
    Cli.main(
      s"--wiro --config cli/src/test/resources/fixtures/config.json $fixturesPath".split(" ")
    )
  }

}
