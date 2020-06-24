enablePlugins(PackPlugin)

organization := "dev.to"
name := "gatling-scale-out"

version := "1.0"

scalaVersion := "2.12.10"

val gatlingVersion = "3.3.1"

resolvers += Resolver.sonatypeRepo("releases")
resolvers += Resolver.jcenterRepo

libraryDependencies ++= Seq(
  "io.gatling" % "gatling-app" % gatlingVersion,
  "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion exclude("io.gatling", "gatling-recorder"),
  "org.rogach" %% "scallop" % "3.4.0",
)

packMain := Map("load-test" -> "dev.to.gatling.GatlingRunner")
packJvmOpts := Map("load-test" -> Seq("-Xms3G -Xmx3G -XX:+HeapDumpOnOutOfMemoryError -XX:+UseG1GC -XX:MaxGCPauseMillis=30 -XX:G1HeapRegionSize=16m -XX:InitiatingHeapOccupancyPercent=75 -XX:+ParallelRefProcEnabled -XX:+PerfDisableSharedMem -XX:+OptimizeStringConcat -Djava.net.preferIPv4Stack=true -Djava.net.preferIPv6Addresses=false"))



Compile / run / fork := true
javaOptions ++= {
  val props = sys.props.toList
  props.map {
    case (key, value) => s"-D$key=$value"
  }
}
