# scala-runtime-manifest [![Build Status](https://travis-ci.org/deaktator/scala-runtime-manifest.svg?branch=master)](https://travis-ci.org/deaktator/scala-runtime-manifest) [[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.deaktator/scala-runtime-manifest_2.10/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.deaktator/scala-runtime-manifest_2.10)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.deaktator%22) #

[scala-runtime-manifest](https://github.com/deaktator/scala-runtime-manifest) generates 
untyped `scala.reflect.Manifest[_]` instances at Runtime in Scala 2.10.

This can be useful in situations when using Scala 2.10.x where a `Manifest` needs to be created
outside of the Scala compiler.  This can be useful in interop code.  For instance, when calling 
from Java into a Scala library whose API requires a `Manifest`.  Typically, this is somewhat of a 
pain but this little library makes it easy to produce a `Manifest`.


## Import into your project

```scala
// In your build.sbt
libraryDependencies ++= Seq(
  "com.github.deaktator" %% "scala-runtime-manifest" % "0.0.1"
)
```

*OR*

```xml
<!-- In Maven pom.xml -->
<dependency>
  <groupId>com.github.deaktator</groupId>
  <artifactId>scala-runtime-manifest_2.10</artifactId>
  <version>0.0.1</version>
</dependency>
```

## Use

```scala
import deaktator.reflect.runtime.manifest.ManifestParser

val compilerGeneratedManifest = manifest[Seq[String]]

// Manifest on the right if successful.
// Error message are on the left if a failure occurred.
val parsed: Either[String, Manifest[_]] =
  ManifestParser.parse("scala.collection.Seq[java.lang.String]")

// For expository purposes.
parsed match {
  case Right(runtimeGeneratedManifest) =>
    assert(runtimeGeneratedManifest == compilerGeneratedManifest, "Should be the same.")
  case Left(errorMessage) =>
    assert(false, s"Failed with message: $errorMessage")
}
```
