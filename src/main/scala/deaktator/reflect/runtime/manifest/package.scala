package deaktator.reflect.runtime

/**
  * This small library provides a parser to parse `scala.reflect.Manifest` instances from
  * `String`s.  The main grammar is decomposed so that `Manifest`s are never mentioned.
  * This is so that the library could be used for other type constructors that would be
  * useful for reflection.  This probably isn't necessary because the main use would be
  * for Scala '''2.10'''.  That's because in versions of Scala 2.10, reflection isn't
  * thread-safe and `TypeTag`s have issues:
  *
  - [[https://issues.scala-lang.org/browse/SI-7555 SI-7555]]
  - [[https://issues.scala-lang.org/browse/SI-7378 SI-7378]]
  *
  * When using Scala '''2.11'''+, it's probably preferable to use
  * [[http://docs.scala-lang.org/overviews/reflection/typetags-manifests WeakTypeTag]]s
  * and they can be created quite easily using ''ToolBoxes''.  A caveat is that failures
  * in producing `WeakTypeTag`s using ToolBoxes is that the error reporting is rather bad.
  * That being said, it is easy; for instance:
  *
  * {{{
  * val universe: scala.reflect.runtime.universe.type = scala.reflect.runtime.universe
  * import universe._
  * import scala.reflect.runtime.currentMirror
  * import scala.tools.reflect.ToolBox
  * val toolbox = currentMirror.mkToolBox()
  * val wttString = "List[Double]"
  * val exp = s"scala.reflect.runtime.universe.weakTypeTag[$wttString]"
  * val wtt = toolbox.eval(toolbox.parse(exp)).asInstanceOf[WeakTypeTag[_]]
  * }}}
  *
  * The entry point into parsing is [[deaktator.reflect.runtime.manifest.ManifestParser]].
  * It exposes a `parse` method to parse `CharSequence`s into `scala.reflect.Manifest`
  * instances.
  *
  * Basic Usage:
  *
  * {{{
  * //
  * val em: Either[String,scala.reflect.Manifest[_]] =
  *   ManifestParser.parse("scala.collection.immutable.List[scala.Double]")
  * assert("scala.collection.immutable.List[Double]" == em.right.get.toString)
  *
  * val em2 = ManifestParser.parse("scala.collection.immutable.Map[Int, scala.Float]")
  * assert("scala.collection.immutable.Map[Int, Float]" == em2.right.get.toString)
  * }}}
  *
  * Notice the type signature for [[deaktator.reflect.runtime.manifest.ManifestParser]]'s parse
  * method.  It returns an `Either` where, as is idiomatic, `Left` contains an error message and
  * `Right` contains an ''untyped'' `Manifest`.  This is because we can't verify the type at
  * compile time.
  */
package object manifest {}
