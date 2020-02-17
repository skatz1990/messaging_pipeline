name := "kafka-example"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "1.1.0",
  "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime,
  "org.scalaj" % "scalaj-http_2.11" % "2.3.0",
  "com.google.code.gson" % "gson" % "2.2.4"
)

mainClass in assembly := Some("me.skatz.kafka.Consumer")