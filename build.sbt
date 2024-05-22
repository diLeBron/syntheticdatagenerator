ThisBuild / version := "0.1.0-Synthetic"
ThisBuild / scalaVersion := "2.12.15"

lazy val root = (project in file("."))
  .settings(
    name := "syntheticdatagenerator"
  )

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.10.0",
  "org.typelevel" %% "cats-effect" % "3.6-0142603",
  "org.apache.spark" %% "spark-core" % "3.5.0",
  "org.apache.spark" %% "spark-sql" % "3.5.0" % "provided",
  "org.postgresql" % "postgresql" % "42.7.2",
  "com.github.scopt" %% "scopt" % "4.1.0",
  "org.apache.flink" %% "flink-scala" % "1.14.4",
  "org.apache.flink" %% "flink-streaming-scala" % "1.14.4",
  "org.apache.flink" %% "flink-connector-kafka" % "1.14.4",
  "org.apache.flink" % "flink-connector-base" % "1.14.4"
)

dependencyOverrides += "org.scala-lang.modules" %% "scala-xml" % "2.1.0"