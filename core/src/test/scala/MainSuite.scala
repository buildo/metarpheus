package morpheus

import org.scalatest._

class MainSuite extends FunSuite {
  test("run main") {
    import morpheus.intermediate._

    main.main("--config core/fixture/config.scala core/fixture/sources".split(" "))
  }

  test("run main with wiro flag") {
    import morpheus.intermediate._

    main.main("--wiro --config core/fixture/config.scala core/fixture/sources".split(" "))
  }

}
