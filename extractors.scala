package morpheus

import scala.meta._
import scala.meta.dialects.Scala211

package object extractors {

  def parse(file: java.io.File): scala.meta.Source = file.parse[Source]

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

}
