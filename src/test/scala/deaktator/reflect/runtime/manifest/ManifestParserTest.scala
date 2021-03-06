package deaktator.reflect.runtime.manifest

/**
  * Created by ryan on 8/18/16.
  */

import org.scalatest._
import org.scalatest.Assertions._

/**
  * @author deaktator
  */
class ManifestParserTest extends FlatSpec with Matchers {
  import ManifestParserTest._

  "ManifestParser" should "correctly parse AnyVals" in {
    testPredef(AnyVals)
  }

  it should "correctly parse other Predef values" in {
    testPredef(OtherPredefs diff Set("AnyRef"))

    // AnyRef Manifests *act like* Object ones but they are not the same under equality.
    ManifestParser.parse("scala.AnyRef").toString should be ("Right(Object)")
    ManifestParser.parse("AnyRef").toString should be ("Right(Object)")
  }

  it should "correctly parse Map instances" in {
    ManifestParser.parse("scala.collection.immutable.Map[Int, scala.Float]").toString should be {
      "Right(scala.collection.immutable.Map[Int, Float])"
    }
  }

  it should "successfully parse with untrimmed whitespace" in {
    Seq(
      " scala.Float",
      "scala.Float ",
      " Array[Float]",
      "Array[Float] "
    ) foreach { v =>
      assert(ManifestParser.parse(v).isRight)
    }
  }

  it should "parse Iterables parameterized by Predef values" in {
    OtherPredefs.filter(v => v != "AnyRef").foreach { v =>
      val s = s"scala.collection.immutable.Iterable[$v]"
      assert(ManifestParser.parse(s).toString == s"Right($s)")
    }

    assert(ManifestParser.parse("scala.collection.immutable.Iterable[AnyRef]").toString ==
           "Right(scala.collection.immutable.Iterable[Object])")
  }

  it should "correctly parse primative array Manifests" in {
    Seq("scala.Array[Float]", "Array[scala.Float]").foreach { in =>
      ManifestParser.parse(in) match {
        case Right(s) => assert(s.runtimeClass.getCanonicalName == "float[]")
        case Left(m) => fail(s"Failed to parse '$in'. Error: $m")
      }
    }
  }

  it should "correctly parse nested primative array Manifests" in {
    Seq("scala.Array[Array[Float]]", "Array[scala.Array[scala.Float]]").foreach { in =>
      ManifestParser.parse(in) match {
        case Right(s) => assert(s.runtimeClass.getCanonicalName == "float[][]")
        case Left(m) => fail(s"Failed to parse '$in'. Error: $m")
      }
    }
  }

  it should "fail to parse when a space appears in the canonical class name" in {
    Seq(
      "java.lang. String",
      "java.lang .String",
      "java. lang.String",
      "java .lang.String",
      "scala.collection.immutable. Map[Int, scala.Float]"
    ) foreach { v =>
      val res = ManifestParser.parse(v).left.map(_ => "")
      val left = Left("")
      assert(res == left)
    }
  }

  it should "fail to parse a double path separator" in {
    ManifestParser.parse("scala..Int").isRight should be (false)
  }

  it should "identify AnyRef and Objects as the same type of Manifest." in {
    val o = ManifestParser.parse("scala.collection.immutable.Iterable[Object]")
    assert(o.isRight)
    assert(ManifestParser.parse("scala.collection.immutable.Iterable[AnyRef]") == o)
  }

  it should "differentiate between AnyVal and AnyRef Manifests" in {
    val v = ManifestParser.parse("scala.collection.immutable.Iterable[AnyVal]")
    val r = ManifestParser.parse("scala.collection.immutable.Iterable[AnyRef]")

    (v, r) match {
      case (Right(vs), Right(rs)) => assert(vs != rs)
      case (Left(m), _)           => fail(s"Parse failure on Iterable[AnyVal]. Failure: $m")
      case (_, Left(m))           => fail(s"Parse failure on Iterable[AnyVar]. Failure: $m")
    }
  }

  it should "correctly parse nested unparameterized static inner classes" in {
    ManifestParser.parse("a.B.C").right.get
    ManifestParser.parse("a.B.C.D").right.get
    ManifestParser.parse("a.B.C.D.E").right.get
  }

  it should "correctly parse nested unparameterized static private inner classes" in {
    ManifestParser.parse("a.B.P").right.get
  }

  it should "correctly parse nested parameterized static inner classes" in {
    ManifestParser.parse("a.B.C.DP[Int]").right.get
    ManifestParser.parse("a.B.C.D.EP[a.B.C.DP[Double]]").right.get
  }

  it should "fail with an appropriate message on a non-existent class" in {
    val err = ManifestParser.parse("a.B.X").left.get
    val exp =
      """
        |Problem at character 5:
        |'a.B.X
        |      ^
        |class a.B.X couldn't be parsed: java.lang.ClassNotFoundException: a.B.X
      """.stripMargin.trim
    assert(err == exp)
  }

  it should "fail with an appropriate message on a non-existent parameterized class" in {
    val err = ManifestParser.parse("a.B.X[java.lang.String]").left.get
    val exp =
      """
        |Problem at character 5:
        |'a.B.X[java.lang.String]
        |      ^
        |class a.B.X couldn't be parsed: java.lang.ClassNotFoundException: a.B.X
      """.stripMargin.trim
    assert(err == exp)
  }

  it should "work correctly when called in parallel" in {
    val pkg = "scala.collection.immutable."
    val basicTypes = AnyVals ++ OtherPredefs
    val seqTypes = Seq("List", "Seq", "Set", "Vector").map(c => s"$pkg$c") :+ "scala.collection.Seq"
    val seqManStrs = for (s <- seqTypes; x <- basicTypes) yield s"$s[$x]"
    val mapManStrs = for (k <- basicTypes; v <- basicTypes) yield s"scala.collection.immutable.Map[$k,$v]"
    val allTypes = (seqManStrs ++ mapManStrs).toSeq.sorted
    val seq = allTypes.map(s => ManifestParser.parse(s).right.get).toVector
    val par = allTypes.par.map(s => ManifestParser.parse(s).right.get).toVector
    assert(par == seq)
  }

  "Parsed unparameterized manifest strings" should "produces a Manifest equal to a compiler-generated one." in {
    assert(ManifestParser.parse("java.lang.String").right.get == manifest[String])
  }

  "Parsed array manifest strings" should "produce a Manifest equal to a compiler-generated one." in {
    assert(ManifestParser.parse("Array[java.lang.String]") == Right(manifest[String].arrayManifest))
  }

  "Parsed parameterized manifest strings" should "produce a Manifest equal to a compiler-generated one." in {
    val mis = manifest[Iterable[String]]
    val ss = "java.lang.String"
    val si = "scala.collection.Iterable"
    assert(ManifestParser.parse(s"$si[$ss]").right.get == mis)
  }

  private[this] def testPredef(testCases: Traversable[String]): Unit = {
    testCases foreach { v =>
      val res = s"Right($v)"
      ManifestParser.parse(s"scala.$v").toString should be (res)
      ManifestParser.parse(s"$v").toString should be (res)
    }
  }
}

object ManifestParserTest {
  val AnyVals = Set("Boolean", "Byte", "Char", "Double", "Float", "Int", "Long", "Short", "Unit")
  val OtherPredefs = Set("Object", "Any", "AnyVal", "AnyRef", "Nothing")
}
