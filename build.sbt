name := "scala-runtime-manifest"

homepage := Some(url("https://github.com/deaktator/scala-runtime-manifest"))

licenses := Seq("MIT License" -> url("http://opensource.org/licenses/MIT"))

description := """Generate untyped scala.reflect.Manifest instances at Runtime in Scala 2.10.x."""

lazy val commonSettings = Seq(
  organization := "com.github.deaktator",
  scalaVersion := "2.10.5",
  crossScalaVersions := Seq(),
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
    "-Yinline",
    "-Yclosure-elim",
    "-Ydead-code",
    "-Xverify",
    "-Ywarn-inaccessible",
    "-Ywarn-dead-code"
  ),

  scalacOptions <++= scalaVersion map {
    case v: String if v.split("\\.")(1).toInt >= 11 =>
      Seq(
        "-Ywarn-unused",
        "-Ywarn-unused-import",

        // These options don't play nice with IntelliJ.  Comment them out to debug.
        "-Ybackend:GenBCode",
        "-Ydelambdafy:method",
        "-Yopt:l:project",
        "-Yconst-opt"
      )
    case _ =>
      Seq()
  }
)

lazy val root = project.in( file(".") ).
  settings(commonSettings: _*).
  settings (
    libraryDependencies ++= Seq(
      // TEST dependencies
      "org.scalatest" %% "scalatest" % "2.2.6" % "test"
    )
  )

// ===========================   PUBLISHING   ===========================

sonatypeProfileName := "com.github.deaktator"

pomExtra in Global := (
  <url>https://github.com/deaktator/pops</url>
    <licenses>
      <license>
        <name>MIT License</name>
        <url>http://opensource.org/licenses/MIT</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
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
