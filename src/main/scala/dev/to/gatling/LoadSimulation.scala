package dev.to.gatling

import com.typesafe.scalalogging.StrictLogging
import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class LoadSimulation extends Simulation with StrictLogging {

  GatlingRunner.conf match {
    case Some(conf) => {
      val duration: FiniteDuration = Try(Duration(conf.testDuration().replace('_', ' '))) match {
        case Success(duration) => duration.asInstanceOf[FiniteDuration]
        case Failure(exception) => throw exception
      }
      val usersPerSecond = conf.usersPerSecond().toDouble
      val postsScenario =
        scenario("Posts")
          .exec(http("Get 100 posts").get("http://jsonplaceholder.typicode.com/todos"))

      setUp(
        postsScenario.inject(
          constantUsersPerSec(usersPerSecond) during duration
        ).protocols(http)
      )
    }
    case None => throw new IllegalStateException
  }
}
