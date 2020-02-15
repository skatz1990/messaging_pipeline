name := "kafka-example"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "1.1.0",
  "com.typesafe" % "config" % "1.3.0"
)
