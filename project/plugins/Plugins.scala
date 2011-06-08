import sbt._
 
class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val akkaRepo = "Akka Repo" at "http://akka.io/repository"
  val akkaPlugin = "se.scalablesolutions.akka" % "akka-sbt-plugin" % "1.0"
  lazy val scalatePlugin = "org.fusesource.scalate" % "sbt-scalate-plugin" % "1.4.1"
}
