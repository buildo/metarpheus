package morpheus

import scala.meta._
import scala.meta.dialects.Scala211

package object extractors {

  def parse(file: java.io.File): scala.meta.Source = file.parse[Source]

}
