name := """twitter-clone"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.3"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "com.typesafe.play" %% "anorm" % "2.5.3" //database access
libraryDependencies +=  "mysql" % "mysql-connector-java" % "5.1.36"
libraryDependencies ++= Seq(evolutions, jdbc)
