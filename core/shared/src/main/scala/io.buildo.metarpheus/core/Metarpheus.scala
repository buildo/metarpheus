package io.buildo.metarpheus
package core

import scala.meta._
import scala.meta.inputs.Input.File
import scala.meta.io.AbsolutePath
import scala.meta.internal.io.PlatformFileIO

object Metarpheus {

  def recursivelyListFiles(target: AbsolutePath): List[AbsolutePath] = {
    if (target.isDirectory) {
      val dirContent = PlatformFileIO.listFiles(target).iterator.toList
      dirContent ++ dirContent.filter(_.isDirectory).flatMap(recursivelyListFiles)
    } else {
      List(target)
    }
  }

  def run(paths: List[String], config: Config): intermediate.API = {
    val files = paths
      .flatMap(path => recursivelyListFiles(AbsolutePath(path)))
      .filter(_.toString.endsWith(".scala"))
    val parsed = files.map(File(_).parse[Source].get)

    if(config.verbose) {
      val filesString = files.mkString(" \n\t ")
      println(
        s"""paths : $paths
           |files : $filesString
          |
          |---------------------------------------
          |
        """.stripMargin)
    }



    extractors
      .extractFullAPI(
        parsed = parsed,
        authRouteTermNames = config.authRouteTermNames,
        wiro = config.wiro,
        verbose = config.verbose
      )
      .stripUnusedModels(config.modelsForciblyInUse)
  }

}
