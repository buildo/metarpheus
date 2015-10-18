package morpheus

package object repr {

  import org.json4s._
  import org.json4s.JsonDSL._
  import org.json4s.jackson.JsonMethods._
  import org.json4s.jackson.Serialization

  implicit val formats = Serialization.formats(NoTypeHints)

  def serializeAPI(api: intermediate.API): String = pretty(Extraction.decompose(api))

}
