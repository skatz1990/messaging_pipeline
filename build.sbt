name := "kafka-example"

version := "0.1"

scalaVersion := "2.13.1"

lazy val akkaVersion = "2.6.3"
lazy val alpakkaVersion = "1.1.2"
libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "1.1.0",
  "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime,
  "com.google.code.gson" % "gson" % "2.2.4",
  "com.sksamuel.avro4s" %% "avro4s-core" % "3.0.9",
  "com.sksamuel.avro4s" %% "avro4s-json" % "3.0.9",
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-protobuf" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % "2.0.2",
  "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % alpakkaVersion,
  "com.lightbend.akka" %% "akka-stream-alpakka-elasticsearch" % alpakkaVersion,
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case x => MergeStrategy.first
}

lazy val myProject = (project in file(".")).settings(assemblyJarName in assembly := "messaging_pipeline.jar")
