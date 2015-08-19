package morpheus

import java.io.File

object main {

  def main(argv: Array[String]) = {
    def recursivelyListFiles(f: File): Array[File] = {
      val these = f.listFiles
      these ++ these.filter(_.isDirectory).flatMap(recursivelyListFiles)
    }

    val files = argv.map { (dir) =>
      recursivelyListFiles(new File(dir))
    }.flatten.filter(_.getName.endsWith(".scala")).toList

    val parsed: List[scala.meta.Source] = files.map(extractors.parse)

    val routes: List[intermediate.Route] = 
      parsed.flatMap(extractors.route.extractAllRoutes _)

    val modelsInUse: List[intermediate.Type] = {
      import intermediate._
      routes.flatMap { route =>
        route.route.collect {
          case RouteSegment.Param(routeParam) => routeParam.tpe
        } ++
        route.params.map(_.tpe) ++
        List(route.returns) ++
        route.body.map(b => List(b.tpe)).getOrElse(Nil)
      }
    }

    val allConcreteTypesInUse: List[String] = {
      def recurse(t: intermediate.Type): List[intermediate.Type.Name] = t match {
        case name: intermediate.Type.Name => List(name)
        case intermediate.Type.Apply(_, args) => args.flatMap(recurse).toList
      }
      modelsInUse.flatMap(recurse)
    }.map(_.name)

    val models: List[intermediate.CaseClass] =
      parsed.flatMap(extractors.model.extractModel).filter(cc =>
        allConcreteTypesInUse.contains(cc.name))

    import org.json4s._
    import org.json4s.JsonDSL._
    import org.json4s.jackson.JsonMethods._
    import org.json4s.jackson.Serialization

    implicit val formats = Serialization.formats(NoTypeHints)

    val json =
      ("models" -> Extraction.decompose(models)) ~
      ("routes" -> Extraction.decompose(routes))

    println(pretty(json))
  }

}
