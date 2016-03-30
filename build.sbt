name := """play-tepking-mongo-example"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(
    "com.github.jeroenr" %% "tepkin" % "0.7",
    specs2 % Test
)

