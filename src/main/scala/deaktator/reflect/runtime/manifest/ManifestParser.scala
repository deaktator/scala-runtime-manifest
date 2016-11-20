package deaktator.reflect.runtime.manifest

/**
  * Parser that takes string input and constructs `scala.reflect.Manifest` instances.
  * {{{
  * // Example usages:
  *
  * // Either[String,scala.reflect.Manifest[_]]
  * val em = ManifestParser.parse("scala.collection.immutable.List[scala.Double]")
  * assert("scala.collection.immutable.List[Double]" == em.right.get.toString)
  *
  * val em2 = ManifestParser.parse("scala.collection.immutable.Map[Int, scala.Float]")
  * assert("scala.collection.immutable.Map[Int, Float]" == em2.right.get.toString)
  * }}}
  *
  * @author deaktator
  */
object ManifestParser  {

  /**
    * Given a String representation of a type, produce an untyped Manifest.
    * @param strRep a string representation of Manifest
    * @return a Manifest on the Right if successful and an error message on the left
    *         in the event of an error.
    */
  def parse(strRep: CharSequence): Either[String, Manifest[_]] = ManifestGrammar.parseToEither(strRep)

  /** The final grammar for Manifests.  Package private for testing. */
  private[manifest] object ManifestGrammar
                    extends RefInfoGrammar[Manifest]
                       with ManifestCombinators
}
