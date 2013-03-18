import AssemblyKeys._ 

port in container.Configuration := 8080

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
    case x => prev(x)
  }
}

mainClass in assembly := Some("edu.duke.oit.vw.JettyLauncher")

publishArtifact in Test := false
