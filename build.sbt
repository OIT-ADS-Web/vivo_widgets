import AssemblyKeys._ 

artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
   "widgets." + artifact.extension
}

assemblySettings

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (prev) =>
  {
    case PathList("org", "slf4j", xs @ _*) => MergeStrategy.first
    case PathList("javax", "xml", xs @ _*) => MergeStrategy.first
    case PathList("org","apache","commons","collections", xs @ _*) => MergeStrategy.first
    case "about.html" => MergeStrategy.discard
    case "deploy.properties" => MergeStrategy.discard
    case "logback.xml" => MergeStrategy.discard
    case x => prev(x)
  }
}

mainClass in assembly := Some("edu.duke.oit.vw.JettyLauncher")

publishArtifact in Test := false

test in assembly := {}

jarName in assembly := "vivo-widgets.jar"

libraryDependencies ++= Seq(
  "com.h2database" % "h2" % "[1.3,)"
  )

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic"  % "1.0.13",
  "ch.qos.logback" % "logback-core"  % "1.0.13",
  "org.slf4j" % "slf4j-api" % "1.7.2"
)


// by default >container:start will be on port 8080; use the following to override
//jetty(port = 9090)

