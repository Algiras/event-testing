name := "event-testing"
version := "0.0.1-SNAPSHOT"

scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.8.0",
  "io.estatico" %% "newtype" % "0.4.4",
  "eu.timepit" %% "refined" % "0.10.1",
  "eu.timepit" %% "refined-cats" % "0.10.1",
  "com.github.nscala-time" %% "nscala-time" % "2.32.0",
  "tf.tofu" %% "derevo-cats" % "0.13.0",
  "tf.tofu" %% "derevo-scalacheck" % "0.13.0" % Test,
  "eu.timepit" %% "refined-scalacheck" % "0.10.1" % Test,
  "com.47deg" %% "scalacheck-toolbox-datetime" % "0.6.0" % Test,
  "com.disneystreaming" %% "weaver-cats" % "0.7.15" % Test,
  "com.disneystreaming" %% "weaver-specs2" % "0.7.15" % Test,
  "com.disneystreaming" %% "weaver-scalacheck" % "0.7.15" % Test,
)

testFrameworks += new TestFramework("weaver.framework.CatsEffect")

// scalac options come from the sbt-tpolecat plugin so need to set any here

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full)

scalacOptions += "-Ymacro-annotations"