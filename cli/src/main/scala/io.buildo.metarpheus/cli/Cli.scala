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
  val verbose = opt[Boolean]("verbose", descr = "verbose output", required = false)
  val targets = trailArg[List[String]](required = true)
  verify()
}

object Cli {



  def main(argv: Array[String]): Unit = {
    val conf = new CommandLine(argv)

    val paths = conf.targets.get.get

    implicit val parseConfig: Configuration = Configuration.default.withDefaults
    val config = (for {
      fileName <- conf.configPath.get
      json = Source.fromFile(fileName).mkString
      parsed <- decode[core.Config](json).toOption
    } yield parsed).getOrElse(core.Config.default)


    val wiro = conf.wiro.get.getOrElse(config.wiro)

    val verbose = conf.verbose.get.getOrElse(config.verbose)

    val api = core.Metarpheus.run(paths, config.copy(wiro = wiro, verbose = verbose))

    val serializedAPI = repr.serializeAPI(api)

    conf.outputFile.get match {
      case None => println(serializedAPI)
      case Some(outputFilePath) =>
        val f = new File(outputFilePath)
        val p = new java.io.PrintWriter(f)
        try {
          p.println(serializedAPI)
        } finally {
          p.close()
        }
    }

    for (v <- conf.verbose.get){
      println(
        s"""config: $config
           |api: $api
           |serializedAPI: $serializedAPI
         """.stripMargin)
    }

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
