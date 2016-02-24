import metarpheus.annotation.{publishRoute, alias}

object Test {
  @alias def a = 2

  @publishRoute(authenticated = true) val route = 42
}
