package metarpheus.annotation

import scala.annotation.StaticAnnotation

class publishRoute(authenticated: Boolean = false) extends StaticAnnotation

class alias extends StaticAnnotation
