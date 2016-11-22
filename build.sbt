import sbt.inc.IncOptions

name := "scala-runtime-manifest"

homepage := Some(url("https://github.com/deaktator/scala-runtime-manifest"))

licenses := Seq("MIT License" -> url("http://opensource.org/licenses/MIT"))

description := """Generate untyped scala.reflect.Manifest instances at Runtime in Scala 2.10.x."""

lazy val commonSettings = Seq(
  organization := "com.github.deaktator",
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.10.5", "2.11.8", "2.12.0"),
  crossPaths := true,
  incOptions := incOptions.value.withNameHashing(true),
  javacOptions ++= Seq("-Xlint:unchecked"),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-Xverify",
    "-Ywarn-inaccessible",
    "-Ywarn-dead-code"
  )
)

// : Seq[Def.Setting[_]]
lazy val versionDependentSettings = Seq(
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, scalaMajor)) if scalaMajor == 10 => Seq(
        "-Yinline",
        "-Yclosure-elim",
        "-Ydead-code"
      )
      case Some((2, scalaMajor)) if scalaMajor == 11 => Seq(
        "-Ywarn-unused",
        "-Ywarn-unused-import",

        // These options don't play nice with IntelliJ.  Comment them out to debug.
        "-Ybackend:GenBCode",
        "-Ydelambdafy:method",
        "-Yopt:l:project",
        "-Yconst-opt",

        "-Yinline", // 2.12 doesn't like it.  Maybe it's -X in 2.12.
        "-Yclosure-elim", // 2.12 doesn't like it.  Maybe it's -X in 2.12.
        "-Ydead-code" // 2.12 doesn't like it.  Maybe it's -X in 2.12.
      )
      case Some((2, scalaMajor)) if scalaMajor == 12 => Seq()
      case _ => Seq()
    }
  }
)

lazy val root = project.in( file(".") ).
  settings(commonSettings: _*).
  settings(versionDependentSettings: _*).
  settings (
    libraryDependencies ++= Seq(
      // TEST dependencies
      // 2.2.6
      "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    ),
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, scalaMajor)) if scalaMajor >= 11 =>
          Seq("org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4")
        case Some((2, 10)) => Seq()
      }
    }
  )

// ===========================   PUBLISHING   ===========================

sonatypeProfileName := "com.github.deaktator"

pomExtra in Global := (
    <scm>
      <url>git@github.com:deaktator/scala-runtime-manifest</url>
      <developerConnection>scm:git:git@github.com:deaktator/scala-runtime-manifest.git</developerConnection>
      <connection>scm:git:git@github.com:deaktator/scala-runtime-manifest.git</connection>
    </scm>
    <developers>
      <developer>
        <id>deaktator</id>
        <name>R M Deak</name>
        <url>https://deaktator.github.io</url>
      </developer>
    </developers>
  )


import ReleaseTransformations._

releaseCrossBuild := true

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  ReleaseStep(action = Command.process("publishSigned", _), enableCrossBuild = true),
  setNextVersion,
  commitNextVersion,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _), enableCrossBuild = true),
  pushChanges
)
