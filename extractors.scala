package morpheus

import scala.meta._
import scala.meta.dialects.Scala211

package object extractors {

  def parse(file: java.io.File): scala.meta.Source = file.parse[Source]

  /**
   * Extract all terms from a sequence of applications of an infix operator
   * (which translates to nested `ApplyInfix`es).
   * e.g. getAllInfix(t1 + t2 + t3 + t4, "+") results in List(t1, t2, t3, t4)
   */
  def getAllInfix(ainfix: internal.ast.Term, op: String): List[internal.ast.Term] = {
    import scala.meta.internal.ast._
    ainfix match {
      case Term.ApplyInfix(subinfix: Term.ApplyInfix, Term.Name(`op`), Nil, List(term : Term)) =>
        getAllInfix(subinfix, `op`) :+ term
      case Term.ApplyInfix(term1: Term, Term.Name(`op`), Nil, List(term2 : Term)) =>
        term1 :: term2 :: Nil
      case term: Term => term :: Nil
    }
  }

  /*
   * Convert a scala-meta representation of a type to a metarpheus
   * intermediate representation
   */
  def tpeToIntermediate(tpe: internal.ast.Type): intermediate.Type = tpe match {
    case name: scala.meta.internal.ast.Type.Name =>
      intermediate.Type.Name(name.value)
    case scala.meta.internal.ast.Type.Apply(name: scala.meta.internal.ast.Type.Name, args) =>
      intermediate.Type.Apply(name.value, args.map(tpeToIntermediate))
  }

}
