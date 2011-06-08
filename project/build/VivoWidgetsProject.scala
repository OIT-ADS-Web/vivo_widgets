import sbt._
import org.fusesource.scalate.sbt.PrecompilerWebProject

class VivoWidgetsProject(info: ProjectInfo) extends DefaultWebProject(info) 
  with AkkaProject 
  with PrecompilerWebProject {
  
  // ## Jetty
  // set the jetty path some templates are reloaded
  override def jettyWebappPath  = webappPath
  override def jettyPort = 8081
  
  // don't scan if using jrebel
  override def scanDirectories = Nil
  
  // change the name of the WAR file that is generated
  override def artifactBaseName = "widgets"
  

  // ## scalatra
  val scalatraVersion = "2.0.0.M3" // 2.8.1
  // val scalatraVersion = "2.0.0-SNAPSHOT" // 2.9.0

  val scalatra = "org.scalatra" %% "scalatra" % scalatraVersion
  val scalate = "org.scalatra" %% "scalatra-scalate" % scalatraVersion
  val auth = "org.scalatra" %% "scalatra-auth" % scalatraVersion
  val servletApi = "org.mortbay.jetty" % "servlet-api" % "2.5-20081211" % "provided"
   

  override def precompilerContextClass = Some("org.fusesource.scalate.DefaultRenderContext")
  // val scalateCore = "org.fusesource.scalate" % "scalate-core" % "1.5.0" 
  
  // Pick your favorite slf4j binding
  val slf4jBinding = "ch.qos.logback" % "logback-classic" % "0.9.28" % "runtime"
  
  // Alternatively, you could use scalatra-specs
  val scalatest = "org.scalatra" %% "scalatra-scalatest" % scalatraVersion % "test"
  

  // this restrict the executed classes names to end with either "Spec" or "Unit"
  override def includeTest(s: String) = { s.endsWith("Spec") || s.endsWith("Unit") }

  val scalaToolsSnapshots = ScalaToolsSnapshots

  override def compileOptions = super.compileOptions ++ Seq(Unchecked)

  val scalatoolsSnapshotRepo = "Scala Tools Snapshot" at
    "http://scala-tools.org/repo-snapshots/"

  val scalatoolsReleaseRepo = "Scala Tools Snapshot" at
    "http://scala-tools.org/repo-releases/"

  val akkaRemote = akkaModule("remote")
  val akkaHttp = akkaModule("http")

  override def libraryDependencies = Set(
    "com.hp.hpl.jena" % "jena" % "2.6.4",
    "com.hp.hpl.jena" % "arq" % "2.8.7",
    "com.hp.hpl.jena" % "tdb" % "0.8.8",
    "com.hp.hpl.jena" % "sdb" % "1.3.3",

    "mysql" % "mysql-connector-java" % "5.1.14",

    // "org.scalatest" % "scalatest" % "1.2" % "test->default",
    // "org.scala-tools.testing" % "specs" % "1.6.1-2.8.0.Beta1-RC6",

    // scala libraries
    "org.scala-tools.testing" %% "specs" % "1.6.8" % "test->default", 
    "net.liftweb" %% "lift-json-ext" % "2.4-SNAPSHOT", 

    "commons-logging" % "commons-logging" % "1.1.1",
    "joda-time" % "joda-time" % "1.6",

    // might want to switch to logback in the future - updated version of log4j
    "com.googlecode.sli4j" % "sli4j-slf4j" % "2.0",
    "com.googlecode.sli4j" % "sli4j-slf4j-logback" % "2.0",
    "org.slf4j" % "slf4j-log4j12" % "1.6.1",
    "org.slf4j" % "slf4j-api" % "1.6.1",
    // "org.slf4j" % "slf4j-jdk14" % "1.6.1",
    "log4j" % "log4j" % "1.2.16",
    "ch.qos.logback" % "logback-classic" % "0.9.28",

    // "org.slf4j" % "slf4j-api" % "1.5.11",

    "org.apache.solr" % "solr-core" % "3.2.0",
    "org.apache.solr" % "solr-solrj" % "3.2.0",
    
    "org.apache.activemq" % "activemq-camel" % "5.5.0",
    "org.apache.activemq" % "activemq-core" % "5.5.0",
    "com.h2database" % "h2" % "1.3.154" % "test->default"


  ) ++ super.libraryDependencies


  // temporary work around to add scala compiler to lib
  // should be fixed in scalate 1.5 (requires scala 2.9.0+)
  override def webappClasspath = 
    super.webappClasspath +++ 
    buildCompilerJar 
  
  
  // http://groups.google.com/group/simple-build-tool/msg/1f17b43807d06cda
  override def testClasspath = super.testClasspath +++ buildCompilerJar

  // val sonatypeNexusSnapshotsRepo = "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  // For Scalate
  // val fuseSourceSnapshotsRepo = "FuseSource Snapshot Repository" at "http://repo.fusesource.com/nexus/content/repositories/snapshots"

  // remove old versions
  override def ivyXML =
      <dependencies>
        <dependency org="org.apache.solr" name="solr-core" rev="3.2.0">
          <exclude module="slf4j-api"/>
          <exclude module="slf4j-jdk14"/>
        </dependency>
        <dependency org="org.apache.solr" name="solr-solrj" rev="3.2.0">
          <exclude module="slf4j-api"/>
        </dependency>
      </dependencies>


}
