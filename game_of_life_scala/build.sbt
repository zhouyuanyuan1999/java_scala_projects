name := "GameOfLifeScala"

version := "0.1.0"

scalaVersion := "3.3.3"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-swing" % "3.0.0"
)

Compile / run / fork := true
