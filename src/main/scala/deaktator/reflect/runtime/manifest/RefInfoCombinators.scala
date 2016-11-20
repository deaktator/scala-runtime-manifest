package deaktator.reflect.runtime.manifest

import scala.language.higherKinds

/**
  * Created by deak on 11/19/16.
  */
trait RefInfoCombinators[RefInfo[_]] {
  /**
    * Create a `Manifest` for an unparameterized class.
    * @param c Class to turn into a Manifest.
    * @return
    */
  protected[this] def classManifest(c: Class[_]): RefInfo[_]

  /**
    * Create a `Array` `Manifest` from a Manifest.
    * @param tpe a manifest for the element type.
    * @return
    */
  protected[this] def arrayManifest(tpe: RefInfo[_]): RefInfo[Array[Nothing]]

  /**
    * Create a `Manifest` for a parameterized type given a string representation of a type constructor and
    * a sequence of `Manifest`s, one for each type parameter.
    * @param clss Class for the type constructor
    * @param typeParams Manifests for each type parameter.
    * @return
    */
  protected[this] def parameterizedManifest(clss: Class[_], typeParams: Seq[RefInfo[_]]): RefInfo[_]

  protected[this] def objRI: RefInfo[Object]
  protected[this] def anyRI: RefInfo[Any]
  protected[this] def anyValRI: RefInfo[AnyVal]
  protected[this] def anyRefRI: RefInfo[AnyRef]
  protected[this] def nothingRI: RefInfo[Nothing]

  protected[this] def booleanRI: RefInfo[Boolean]
  protected[this] def byteRI: RefInfo[Byte]
  protected[this] def charRI: RefInfo[Char]
  protected[this] def doubleRI: RefInfo[Double]
  protected[this] def floatRI: RefInfo[Float]
  protected[this] def intRI: RefInfo[Int]
  protected[this] def longRI: RefInfo[Long]
  protected[this] def shortRI: RefInfo[Short]
  protected[this] def unitRI: RefInfo[Unit]
}
