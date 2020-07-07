name := "kafka-example"

version := "0.1"

scalaVersion := "2.13.1"

lazy val akkaVersion = "2.6.3"
lazy val alpakkaVersion = "1.1.2"
libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "1.1.0",
  "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime,
  "com.google.code.gson" % "gson" % "2.2.4",
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-protobuf" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % "2.0.2",
  "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % alpakkaVersion,
  "com.lightbend.akka" %% "akka-stream-alpakka-elasticsearch" % alpakkaVersion,
)

mainClass in assembly := Some("me.skatz.kafka.KafkaProducer")