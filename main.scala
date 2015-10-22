package morpheus

import java.io.File

object main {

  private def parse(file: java.io.File): scala.meta.Source = {
    import scala.meta._
    import scala.meta.dialects.Scala211
    file.parse[Source]
  }

  def main(argv: Array[String]) = {
    def recursivelyListFiles(f: File): Array[File] = {
      val these: Array[java.io.File] = Option(f.listFiles).getOrElse(Array())
      these ++ these.filter(_.isDirectory).flatMap(recursivelyListFiles)
    }

    val files = argv.map { dir =>
      val file = new File(dir)
      if (!file.exists) {
        throw new Exception("The provided file or folder does not exist")
      }
      recursivelyListFiles(file)
    }.flatten.filter(_.getName.endsWith(".scala")).toList

    val parsed: List[scala.meta.Source] = files.map(parse)

    val routeOverrides: Map[List[String], intermediate.Route] = Map()

    val api = extractors.extractFullAPI(parsed, routeOverrides).stripUnusedModels

    println(repr.serializeAPI(api))

  }

}
