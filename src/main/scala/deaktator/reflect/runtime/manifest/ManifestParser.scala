package deaktator.reflect.runtime.manifest

import scala.annotation.tailrec
import scala.reflect.ManifestFactory
import scala.util.parsing.combinator.RegexParsers
import scala.util.{Try, Success, Failure}

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
object ManifestParser {

  /**
    * Parse a string representation of a typed `Manifest`.
    * Example":
    * {{{
    * ManifestParser.parse("scala.collection.immutable.Map[Int, scala.Float]").right.get
    * }}}
    * @param strRep a string representation of the `Manifest`.
    * @return either an error on the left or a `Manifest` (missing it's type parameter)
    *         on the right
    */
  def parse(strRep: CharSequence): Either[String, Manifest[_]] = {
    import Grammar.{Success => PSuccess, Error => PError, Failure => PFailure}

    Grammar.parseAll(Grammar.manifests, strRep) match {
      case PSuccess(man, _)    => Right(man)
      case PError(err, next)   => Left(errorMsg(strRep, err, next))
      case PFailure(err, next) => Left(errorMsg(strRep, err, next))
    }
  }

  /**
    * Provides an error reporting message.
    * @param strRep string representation of the `Manifest` that should have been created.
    * @param err the error message returned by the parser.
    * @param next the rest of the input remaining at the location of the error.
    * @return an error message.
    */
  private[this] def errorMsg(strRep: CharSequence, err: String, next: Grammar.Input): String = {
    val whiteSpace = Seq.fill(next.offset + 1)(" ").mkString("")
    s"Problem at character ${next.offset}:\n'$strRep\n$whiteSpace^\n$err"
  }

  /**
    * A grammar for matching scala Manifests.
    * This is separated from the rest of the module code for readability.
    *
    * @author deaktator
    */
  private[this] object Grammar extends RegexParsers {

    /**
      * The root production rule.
      * This is typed for two reasons:
     - ''it is the root production''
     - `withArg` ''and'' `array` ''rules refer to this rule, making it recursively thereby necessitating the type.''
      */
    lazy val manifests: Parser[Manifest[_]] = array | withArg | noArg

    /**
      * Production rule for manifests with (possibly multiple) type parameters.
      */
    lazy val withArg = handleClassManifestErrors (
      path ~ (oBracket ~> rep1sep(manifests, ",") <~ cBracket) ^^ {
        case p ~ params => (p, ManifestParser.parameterizedManifest(p, params))
      }
    )

    /**
      * Production rule for Array types.
      * Arrays can only have one type parameter.
      */
    lazy val array = arrayToken ~> oBracket ~> manifests <~ cBracket ^^ { ManifestParser.arrayManifest }

    lazy val noArg = special | classManifest

    lazy val classManifest = handleClassManifestErrors (
      path ^^ { p => (p, ManifestParser.classManifest(p)) }
    )

    lazy val special = opt(scalaPkg) ~> (obj | anyRef | anyVal | any | nothing | anyVals)

    lazy val obj     = "Object"  ^^^ ManifestFactory.Object
    lazy val any     = "Any"     ^^^ ManifestFactory.Any
    lazy val anyVal  = "AnyVal"  ^^^ ManifestFactory.AnyVal
    lazy val anyRef  = "AnyRef"  ^^^ ManifestFactory.AnyRef
    lazy val nothing = "Nothing" ^^^ ManifestFactory.Nothing

    lazy val anyVals = boolean | byte | char | double | float | int | long | short | unit

    lazy val boolean = "Boolean" ^^^ ManifestFactory.Boolean
    lazy val byte    = "Byte"    ^^^ ManifestFactory.Byte
    lazy val char    = "Char"    ^^^ ManifestFactory.Char
    lazy val double  = "Double"  ^^^ ManifestFactory.Double
    lazy val float   = "Float"   ^^^ ManifestFactory.Float
    lazy val int     = "Int"     ^^^ ManifestFactory.Int
    lazy val long    = "Long"    ^^^ ManifestFactory.Long
    lazy val short   = "Short"   ^^^ ManifestFactory.Short
    lazy val unit    = "Unit"    ^^^ ManifestFactory.Unit

    lazy val path = """[a-zA-Z_][a-zA-Z0-9_]*(\.[a-zA-Z_][a-zA-Z0-9_]*)*""".r
    lazy val oBracket = "["
    lazy val cBracket = "]"
    lazy val scalaPkg = """(scala\.)?""".r

    /**
      * An array token should only match the remaining input starting from its beginning.
      */
    // TODO: Determine if the '^' prefix is necessary.
    lazy val arrayToken = """^(scala\.)?Array""".r

    private def handleClassManifestErrors(p: Parser[(String, Try[Manifest[_]])]) = {
      p ^? (ManifestParser.classManifestSuccess, ManifestParser.classManifestError)
    }
  }

  /**
    * Create a `Manifest` for an unparameterized class.
    * @param tpe string representation of the type.
    * @return
    */
  private[manifest] def classManifest(tpe: String): Try[Manifest[_]] = {
    stringToClass(tpe).map(c => ManifestFactory.classType(c))
  }

  private[manifest] def stringToClass(tpe: String): Try[Class[_]] = {
    @tailrec def retry(origEx: ClassNotFoundException, tpe: String): Try[Class[_]] = {
      // Use old-style try/catch so the function is tail recursive.

      // Replace the last '.' with a '$'.
      val newTpe = tpe.replaceFirst("""(\.)([^\.]+)$""", """\$$2""")
      if (tpe == newTpe)
        Failure(origEx)
      else
        try {
          Success(Class.forName(newTpe))
        }
        catch {
          case e: ClassNotFoundException => retry(origEx, newTpe)
          case e: Throwable => Failure(e)
        }
    }

    Try { Class.forName(tpe) }.recoverWith { case e: ClassNotFoundException => retry(e, tpe) }
  }

  private[manifest] val classManifestSuccess: PartialFunction[(String, Try[Manifest[_]]), Manifest[_]] = {
    case (p, Success(cm)) => cm
  }

  private[manifest] val classManifestError: ((String, Try[Manifest[_]])) => String = {
    case (p, tm) =>
      val e = tm.failed.get
      s"class $p couldn't be parsed: ${e.getClass.getCanonicalName}: ${e.getMessage}"
  }

  /**
    * Create a `Array` `Manifest` from a Manifest.
    * @param tpe a manifest for the element type.
    * @return
    */
  private[manifest] def arrayManifest(tpe: Manifest[_]) = ManifestFactory.arrayType(tpe)

  /**
    * Create a `Manifest` for a parameterized type given a string representation of a type constructor and
    * a sequence of `Manifest`s, one for each type parameter.
    * @param tpe string representation for the type constructor
    * @param typeParams Manifests for each type parameter.
    * @return
    */
  private[manifest] def parameterizedManifest(tpe: String, typeParams: Seq[Manifest[_]]): Try[Manifest[_]] = {
    stringToClass(tpe) map { clas =>
      val (first, rest) = typeParams.splitAt(1)
      ManifestFactory.classType(clas, first.head, rest:_*)
    }
  }
}
