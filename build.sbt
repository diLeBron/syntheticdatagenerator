name := "SyntheticDataGenerator"

version := "0.1"

scalaVersion := "2.13.12"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.10.0",
  "org.scala-exercises" %% "exercise-compiler" % "0.7.1",
  "org.scala-exercises" %% "definitions" % "0.7.1",
  "com.chuusai" %% "shapeless" % "2.3.10",
  "org.scalatest" %% "scalatest" % "3.2.10",
  "org.scalacheck" %% "scalacheck" % "1.17.0",
  "org.scalatestplus" %% "scalacheck-1-14" % "3.2.2.0"
)
