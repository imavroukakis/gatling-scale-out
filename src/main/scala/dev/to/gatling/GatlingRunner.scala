package dev.to.gatling

import java.text.SimpleDateFormat
import java.util.Calendar

import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder
import org.rogach.scallop.{ScallopConf, ScallopOption}

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  val usersPerSecond: ScallopOption[Int] = opt[Int](default = Some(5))
  val reportOnly: ScallopOption[String] = opt[String]()
  val testDuration: ScallopOption[String] = opt[String](default = Some("60_seconds"))
  verify()
}

object GatlingRunner {

  var conf: Option[Conf] = None

  def main(args: Array[String]) {
    conf = Some(new Conf(args))
    conf match {
      case Some(conf) => {
        val simClass = classOf[LoadSimulation].getName
        val props = new GatlingPropertiesBuilder
        props.simulationClass(simClass)
        props.runDescription("Gatling Load Test")
        if (conf.reportOnly.isDefined) {
          props.reportsOnly(conf.reportOnly())
        } else {
          val now = Calendar.getInstance().getTime
          val dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss")
          props.resultsDirectory(s"results/${dateFormat.format(now)}")
        }
        Gatling.fromMap(props.build)
      }
      case None => throw new IllegalArgumentException
    }
  }
}
