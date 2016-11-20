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
  def classManifest(c: Class[_]): Manifest[_] = ManifestFactory.classType(c)

  /**
    * Create a `Array` `Manifest` from a Manifest.
    * @param tpe a manifest for the element type.
    * @return
    */
  def arrayManifest(tpe: Manifest[_]): Manifest[Array[Nothing]] = ManifestFactory.arrayType(tpe)

  /**
    * Create a `Manifest` for a parameterized type given a string representation of a type constructor and
    * a sequence of `Manifest`s, one for each type parameter.
    * @param clss Class for the type constructor
    * @param typeParams Manifests for each type parameter.
    * @return
    */
  def parameterizedManifest(clss: Class[_], typeParams: Seq[Manifest[_]]): Manifest[_] = {
    val (first, rest) = typeParams.splitAt(1)
    ManifestFactory.classType(clss, first.head, rest:_*)
  }

  def objRI     = ManifestFactory.Object
  def anyRI     = ManifestFactory.Any
  def anyValRI  = ManifestFactory.AnyVal
  def anyRefRI  = ManifestFactory.AnyRef
  def nothingRI = ManifestFactory.Nothing

  def booleanRI = ManifestFactory.Boolean
  def byteRI    = ManifestFactory.Byte
  def charRI    = ManifestFactory.Char
  def doubleRI  = ManifestFactory.Double
  def floatRI   = ManifestFactory.Float
  def intRI     = ManifestFactory.Int
  def longRI    = ManifestFactory.Long
  def shortRI   = ManifestFactory.Short
  def unitRI    = ManifestFactory.Unit
}
