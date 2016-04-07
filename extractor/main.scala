package morpheus

import java.io.File

import org.rogach.scallop._

class CommandLine(args: Array[String]) extends ScallopConf(args) {
  val configPath = opt[String]("config", descr = "config file path", required = false)
  val directories = trailArg[List[String]](required = true)
  verify()
}

object main {

  private def parse(file: java.io.File): scala.meta.Source = {
    import scala.meta._
    import scala.meta.dialects.Scala211
    file.parse[Source].get
  }

  def main(argv: Array[String]) = {
    val conf = new CommandLine(argv)

    def recursivelyListFiles(f: File): Array[File] = {
      val these: Array[java.io.File] = Option(f.listFiles).getOrElse(Array())
      these ++ these.filter(_.isDirectory).flatMap(recursivelyListFiles)
    }

    val files = (conf.directories.get.get: List[String]).map { dir =>
      val file = new File(dir)
      if (!file.exists) {
        throw new Exception("The provided file or folder does not exist")
      }
      recursivelyListFiles(file)
    }.flatten.filter(_.getName.endsWith(".scala")).toList

    val parsed: List[scala.meta.Source] = files.map(parse)

    val config = conf.configPath.get.map { fileName =>
      val eval = new com.twitter.util.Eval(None)
      eval.apply(new File(fileName)) : Config
    }.getOrElse(DefaultConfig)

    val api = extractors.extractFullAPI(parsed,
      config.routeOverrides,
      config.routeMatcherToIntermediate).stripUnusedModels

    println(repr.serializeAPI(api))

  }

}
