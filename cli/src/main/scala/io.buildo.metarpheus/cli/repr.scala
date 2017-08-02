package io.buildo.metarpheus
package cli

package object repr {

  import org.json4s._
  import org.json4s.JsonDSL._
  import org.json4s.jackson.JsonMethods._
  import org.json4s.jackson.Serialization

  implicit val formats = Serialization.formats(NoTypeHints)

  def serializeAPI(api: core.intermediate.API): String = pretty(Extraction.decompose(api))

}
