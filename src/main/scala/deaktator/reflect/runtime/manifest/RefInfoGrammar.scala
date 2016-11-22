package deaktator.reflect.runtime.manifest

import scala.annotation.tailrec
import scala.language.higherKinds
import scala.util.parsing.combinator.RegexParsers
import scala.util.Try


/**
  * Created by deak on 11/19/16.
  */
trait RefInfoGrammar[RefInfo[_]] extends RegexParsers { self: RefInfoCombinators[RefInfo] =>

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
  def parseToEither(strRep: CharSequence): Either[String, RefInfo[_]] = {
    parseAll(root, strRep) match {
      case Success(man, _)    => Right(man)
      case Error(err, next)   => Left(errorMsg(strRep, err, next))
      case Failure(err, next) => Left(errorMsg(strRep, err, next))
    }
  }

  /**
    * Provides an error reporting message.
    * @param strRep string representation of the `Manifest` that should have been created.
    * @param err the error message returned by the parser.
    * @param next the rest of the input remaining at the location of the error.
    * @return an error message.
    */
  def errorMsg(strRep: CharSequence, err: String, next: Input): String = {
    val whiteSpace = Seq.fill(next.offset + 1)(" ").mkString("")
    s"Problem at character ${next.offset}:\n'$strRep\n$whiteSpace^\n$err"
  }

  /**
    * The root production rule.
    */
  lazy val root: Parser[RefInfo[_]] = phrase(manifest)

  /**
    * The main manifest production rule.  It is typed because `withArg` ''and'' `array`
    * ''production refer to this one, making it recursive, and thereby necessitating the type.''
    */
  lazy val manifest: Parser[RefInfo[_]] = array | withArg | noArg

  /**
    * Production rule for manifests with (possibly multiple) type parameters.
    */
  lazy val withArg =
    clss ~ (oBracket ~> rep1sep(manifest, ",") <~ cBracket) ^^ {
      case c ~ params => parameterizedManifest(c, params)
    }

  /**
    * Production rule for Array types.
    * Arrays can only have one type parameter.
    */
  lazy val array = arrayToken ~> oBracket ~> manifest <~ cBracket ^^ { arrayManifest }

  lazy val noArg = special | classMan

  lazy val classMan = clss ^^ { classManifest }

  lazy val special = opt(scalaPkg) ~> (obj | anyRef | anyVal | any | nothing | anyVals)

  lazy val obj     = "Object"  ^^^ objRI
  lazy val any     = "Any"     ^^^ anyRI
  lazy val anyVal  = "AnyVal"  ^^^ anyValRI
  lazy val anyRef  = "AnyRef"  ^^^ anyRefRI
  lazy val nothing = "Nothing" ^^^ nothingRI

  lazy val anyVals = boolean | byte | char | double | float | int | long | short | unit

  lazy val boolean = "Boolean" ^^^ booleanRI
  lazy val byte    = "Byte"    ^^^ byteRI
  lazy val char    = "Char"    ^^^ charRI
  lazy val double  = "Double"  ^^^ doubleRI
  lazy val float   = "Float"   ^^^ floatRI
  lazy val int     = "Int"     ^^^ intRI
  lazy val long    = "Long"    ^^^ longRI
  lazy val short   = "Short"   ^^^ shortRI
  lazy val unit    = "Unit"    ^^^ unitRI

  lazy val clss = path ^^ { p => (p, stringToClass(p)) } ^? (classSuccess, classError)

  val path = """[a-zA-Z_][a-zA-Z0-9_]*(\.[a-zA-Z_][a-zA-Z0-9_]*)*""".r
  val oBracket = "["
  val cBracket = "]"
  val scalaPkg = """(scala\.)?""".r


  /**
    * An array token should only match the remaining input starting from its beginning.
    */
  // TODO: Determine if the '^' prefix is necessary.
  val arrayToken = """(scala\.)?Array""".r

  /**
    * Try to turn a String into a Class.  If it doesn't work, repeatedly try to change the last '.' into
    * a '$' until no more progress can be made.  If the String cannot successfully be turned into a Class,
    * return the first error encountered.
    * @param tpe a String to turn into a Class.
    * @return
    */
  def stringToClass(tpe: String): Try[Class[_]] = {
    @tailrec def retry(origEx: ClassNotFoundException, tpe: String): Try[Class[_]] = {
      // Replace the last '.' with a '$'.
      val newTpe = tpe.replaceFirst("""(\.)([^\.]+)$""", """\$$2""")
      if (tpe == newTpe)
        scala.util.Failure(origEx)
      else
        // Use old-style try/catch so the function is tail recursive.
        try {
          scala.util.Success(Class.forName(newTpe))
        }
        catch {
          case e: ClassNotFoundException => retry(origEx, newTpe)
          case e: Throwable => scala.util.Failure(e)
        }
    }

    Try { Class.forName(tpe) }.recoverWith { case e: ClassNotFoundException => retry(e, tpe) }
  }

  val classSuccess: PartialFunction[(String, Try[Class[_]]), Class[_]] = {
    case (p, scala.util.Success(c)) => c
  }

  /**
    * This is only called if classSuccess fails.  Therefore, the Try is a Failure.
    */
  val classError: ((String, Try[Class[_]])) => String = {
    case (p, tc) =>
      // We know it failed if this function is called after classSuccess.  Therefore, we
      // don't need to worry about it being successful.
      val e = tc.failed.get
      s"class $p couldn't be parsed: ${e.getClass.getCanonicalName}: ${e.getMessage}"
  }
}
