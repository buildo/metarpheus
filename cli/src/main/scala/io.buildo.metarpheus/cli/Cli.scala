package io.buildo.metarpheus
package cli

import java.io.File
import scala.io.Source

import org.rogach.scallop._
import io.circe.parser.decode
import io.circe.generic.extras._
import io.circe.generic.extras.auto._

class CommandLine(args: Array[String]) extends ScallopConf(args) {
  val configPath = opt[String]("config", descr = "config file path", required = false)
  val outputFile = opt[String]("output", descr = "output file path", required = false)
  val wiro = opt[Boolean]("wiro", descr = "parse wiro-style", required = false)
  val targets = trailArg[List[String]](required = true)
  verify()
}

object Cli {

  def main(argv: Array[String]): Unit = {
    val conf = new CommandLine(argv)

    val files = (conf.targets.get.get: List[String]).map { target =>
      val fileOrDir = new File(target)
      if (!fileOrDir.exists) {
        throw new Exception("The provided file or folder does not exist")
      }
      recursivelyListFiles(fileOrDir)
    }.flatten.filter(_.getName.endsWith(".scala")).toList
    
    val sources = files.map(Source.fromFile(_).mkString)

    implicit val parseConfig: Configuration = Configuration.default.withDefaults
    val config = (for {
      fileName <- conf.configPath.get
      json = Source.fromFile(fileName).mkString
      parsed <- decode[core.Config](json).toOption
    } yield parsed).getOrElse(core.Config.default)

    val wiro = conf.wiro.get.getOrElse(false)

    val api = core.Metarpheus.run(sources, config.copy(wiro = wiro))

    val serializedAPI = repr.serializeAPI(api)

    conf.outputFile.get.map { outputFilePath =>
      val f = new File(outputFilePath)
      val p = new java.io.PrintWriter(f)
      try {
        p.println(serializedAPI)
      } finally {
        p.close()
      }
    }

    println(serializedAPI)
  }

  private[this] def recursivelyListFiles(target: File): Array[File] = {
    if (target.isDirectory) {
      val these: Array[java.io.File] = Option(target.listFiles).getOrElse(Array())
      these ++ these.filter(_.isDirectory).flatMap(recursivelyListFiles)
    } else {
      Array(target)
    }
  }

}