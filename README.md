# scala-runtime-manifest [![Build Status](https://travis-ci.org/deaktator/scala-runtime-manifest.svg?branch=master)](https://travis-ci.org/deaktator/scala-runtime-manifest) [[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.deaktator/scala-runtime-manifest_2.10/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.deaktator/scala-runtime-manifest_2.10)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.deaktator%22) #

[scala-runtime-manifest](https://github.com/deaktator/scala-runtime-manifest) generates untyped
`scala.reflect.Manifest[_]` instances at runtime.  While this is most important for in Scala 
2.10, the project is cross built for Scala 2.10, 2.11 and 2.12 in the case that more recent 
code based on 2.11+ is using "legacy" reflection code.

This can be useful in situations when a `Manifest` needs to be created outside of Scala 
compiler, for instance in interop code.  An example would be Java calling a Scala library whose
API requires a `Manifest`.  Typically, this is somewhat of a pain but this little library makes
it easy to produce a `Manifest`.  It's also useful for configuration in [Spring](http://spring.io).

This library could have used [toolboxes](http://www.scala-lang.org/api/2.11.1/scala-compiler/index.html#scala.tools.reflect.ToolBox) but they were avoided because of some of the 
thread-safety bugs in Scala 2.10.  Instead the library is based on parser combinators and making
calls to the `Manifest` classes directly.  Hopefully this will avoid an thread-safety bugs. See the
test [work correctly when called in parallel](https://github.com/deaktator/scala-runtime-manifest/blob/master/src/test/scala/deaktator/reflect/runtime/manifest/ManifestParserTest.scala#L147).


## Importing into your project

```scala
// In your build.sbt
libraryDependencies ++= Seq(
  "com.github.deaktator" %% "scala-runtime-manifest" % "1.0.0"
)
```

*OR*

```xml
<!-- In Maven pom.xml -->
<dependency>
  <groupId>com.github.deaktator</groupId>
  <artifactId>scala-runtime-manifest_2.10</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Usage

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
