import sbt._
import Keys._
import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._

object VivowidgetsBuild extends Build {
  val Organization = "duke"
  val Name = "VivoWidgets"
  val Version = "0.3.0"
  val ScalaVersion = "2.10.1"
  val ScalatraVersion = "2.2.1"

  lazy val project = Project (
    "vivowidgets",
    file("."),
    settings = Defaults.defaultSettings ++ Seq(ScalatraPlugin.scalatraWithJRebel:_*) ++ Seq(scalateSettings:_*) ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      ivyXML :=
      <dependency org="log4j" name="log4j" rev="1.2.15">
        <exclude org="com.sun.jdmk"/>
        <exclude org="com.sun.jmx"/>
        <exclude org="javax.jms"/>
      </dependency>,
      resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
      resolvers += "Restlet" at "http://maven.restlet.com/",
      libraryDependencies ++= Seq(
        "org.scalatra" %% "scalatra" % ScalatraVersion,
        "org.scalatra" %% "scalatra-scalate" % ScalatraVersion,
        "net.liftweb" %% "lift-json" % "3.0-SNAPSHOT",

        "log4j" % "log4j" % "1.2.15" exclude("javax.jms", "jms"),

        "org.fusesource.scalate"  % "scalate-core_2.10" % "1.6.1",
        "org.scalatra" %% "scalatra-auth" % "2.2.0",

        // jena libs
        // "com.hp.hpl.jena" % "jena" % "2.6.4" exclude("org.slf4j", "slf4j-api"),
        "com.hp.hpl.jena" % "jena" % "2.6.4" excludeAll(ExclusionRule(organization = "org.slf4j")),
        "com.hp.hpl.jena" % "arq"  % "2.8.7" excludeAll(ExclusionRule(organization = "org.slf4j")),
        "com.hp.hpl.jena" % "tdb"  % "0.8.8" excludeAll(ExclusionRule(organization = "org.slf4j")),
        "com.hp.hpl.jena" % "sdb"  % "1.3.3" excludeAll(ExclusionRule(organization = "org.slf4j")),

        // solr
        "org.apache.solr" % "solr-core" % "4.7.2",
        "org.apache.solr" % "solr-solrj" % "4.7.2",

        "org.specs2" %% "specs2" % "2.3.12" % "test->default",
        // "org.scalatest" %% "scalatest" % "1.9.1" % "test->default",


        "mysql" % "mysql-connector-java" % "5.1.14",
        "com.h2database" % "h2" % "1.3.154" % "test",
        "com.mchange" % "c3p0" % "0.9.2",
            
        "org.scalatra" %% "scalatra-specs2" % ScalatraVersion % "test",
        "org.eclipse.jetty" % "jetty-webapp" % "8.1.8.v20121106" % "container,runtime,compile,test",
        "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container;provided;test" artifacts (Artifact("javax.servlet", "jar", "jar"))
      ),
      scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
        Seq(
          TemplateConfig(
            base / "webapp" / "WEB-INF" / "templates",
            Seq.empty,  /* default imports should be added here */
            Seq.empty,  /* add extra bindings here */
            Some("templates")
          )
        )
      }
    )
  )
}
