package morpheus

import org.scalatest._

class MainSuite extends FunSuite {
  test("run main") {
    import morpheus.intermediate._

    main.main("--config fixture/config.scala fixture/sources".split(" "))
  }

}
