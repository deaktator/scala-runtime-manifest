package deaktator.reflect.runtime.manifest

import scala.reflect.ManifestFactory

/**
  * Provides combinators for `scala.reflect.Manifest`.
  * Created by deaktator on 11/19/16.
  */
trait ManifestCombinators extends RefInfoCombinators[Manifest] {

  /**
    * Create a `Manifest` for an unparameterized class.
    * @param c Class to turn into a Manifest.
    * @return
    */
  protected[this] def classManifest(c: Class[_]): Manifest[_] = ManifestFactory.classType(c)

  /**
    * Create a `Array` `Manifest` from a Manifest.
    * @param tpe a manifest for the element type.
    * @return
    */
  protected[this] def arrayManifest(tpe: Manifest[_]): Manifest[Array[Nothing]] = ManifestFactory.arrayType(tpe)

  /**
    * Create a `Manifest` for a parameterized type given a string representation of a type constructor and
    * a sequence of `Manifest`s, one for each type parameter.
    * @param clss Class for the type constructor
    * @param typeParams Manifests for each type parameter.
    * @return
    */
  protected[this] def parameterizedManifest(clss: Class[_], typeParams: Seq[Manifest[_]]): Manifest[_] = {
    val (first, rest) = typeParams.splitAt(1)
    ManifestFactory.classType(clss, first.head, rest:_*)
  }

  protected[this] def objRI     = ManifestFactory.Object
  protected[this] def anyRI     = ManifestFactory.Any
  protected[this] def anyValRI  = ManifestFactory.AnyVal
  protected[this] def anyRefRI  = ManifestFactory.AnyRef
  protected[this] def nothingRI = ManifestFactory.Nothing

  protected[this] def booleanRI = ManifestFactory.Boolean
  protected[this] def byteRI    = ManifestFactory.Byte
  protected[this] def charRI    = ManifestFactory.Char
  protected[this] def doubleRI  = ManifestFactory.Double
  protected[this] def floatRI   = ManifestFactory.Float
  protected[this] def intRI     = ManifestFactory.Int
  protected[this] def longRI    = ManifestFactory.Long
  protected[this] def shortRI   = ManifestFactory.Short
  protected[this] def unitRI    = ManifestFactory.Unit
}
