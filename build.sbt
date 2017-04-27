scalaVersion := "2.12.2"
scalacOptions := Seq("-unchecked", "-deprecation", "-feature")

version := "0.1.0-SNAPSHOT"
organization := "one.aleph"
organizationName := "Alexander Kreuzer"
name := "akkastream azure"

val akkaVersion = "2.5.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "com.microsoft.azure" % "azure-storage" % "5.0.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
  "com.typesafe" % "config" % "1.3.1"
)
