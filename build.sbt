ThisBuild / version := "0.1.0-Synthetic"
ThisBuild / scalaVersion := "2.12.15"

val main_class: String = "ru.digitalleague.syntheticdatagenerator.Main"

lazy val root = (project in file("."))
  .settings(
    assembly / mainClass := Some(main_class),
    name := "syntheticdatagenerator"
  )

lazy val sparkVersion = "2.4.0"
lazy val uVersion = "3.3.0"

//lazy val integration = (project in file("integration"))
//  .dependsOn(root)
//  .settings(
//    publish / skip := true,
//    libraryDependencies ++= Seq(
//      "org.scalatest" %% "scalatest" % "3.2.18" % Test,
//      "com.dimafeng" % "testcontainers-scala_2.12" % "0.41.3",
//      "com.dimafeng" %% "testcontainers-scala-scalatest" % "0.41.3" % "it",
//      "com.dimafeng" %% "testcontainers-scala-kafka" % "0.41.3" % "it"
//    )
//  )

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.10.0",
  "org.typelevel" %% "cats-effect" % "3.6-0142603",
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-hive" % sparkVersion,
  "org.apache.hive" % "hive-jdbc" % "3.1.2",
  "org.postgresql" % "postgresql" % "42.7.2",
  "com.github.scopt" %% "scopt" % "4.1.0",
  "org.apache.flink" %% "flink-scala" % "1.14.4",
  "org.apache.flink" %% "flink-streaming-scala" % "1.14.4",
  "org.apache.flink" %% "flink-connector-kafka" % "1.14.4",
  "org.apache.flink" % "flink-connector-base" % "1.14.4",
  "org.apache.flink" %% "flink-clients" % "1.14.4",
  "org.apache.kafka" %% "kafka" % "3.7.0",
  "com.lihaoyi" %% "ujson" % uVersion,
  "com.lihaoyi" %% "upickle" % uVersion,
  "org.scalatest" %% "scalatest" % "3.2.18" % Test,
  "org.testcontainers" % "testcontainers" % "1.19.7" % Test,
  "com.dimafeng" %% "testcontainers-scala-scalatest" % "0.41.3" % Test,
  "com.dimafeng" %% "testcontainers-scala-kafka" % "0.41.3" % Test,
  "org.scalatest" %% "scalatest-flatspec" % "3.2.18" % Test
)

dependencyOverrides += "org.scala-lang.modules" %% "scala-xml" % "2.1.0"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}

mainClass in assembly := Some(main_class)
mainClass in Compile := Some(main_class)