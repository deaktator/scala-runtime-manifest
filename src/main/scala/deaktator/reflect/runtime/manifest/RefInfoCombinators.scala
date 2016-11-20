package deaktator.reflect.runtime.manifest

import scala.language.higherKinds

/**
  * Created by deak on 11/19/16.
  */
trait RefInfoCombinators[RefInfo[_]] {
  /**
    * Create a `RefInfo` for an unparameterized class.
    * @param c Class to turn into a `RefInfo.
    * @return
    */
  def classManifest(c: Class[_]): RefInfo[_]

  /**
    * Create a `RefInfo[Array[_]]` from a `RefInfo`.
    * @param tpe a manifest for the element type.
    * @return
    */
  def arrayManifest(tpe: RefInfo[_]): RefInfo[Array[Nothing]]

  /**
    * Create a `RefInto` for a parameterized type given a Class of a type constructor and
    * a sequence of `RefInfo`s, one for each type parameter.
    * @param clss Class for the type constructor
    * @param typeParams `RefInfo`s for each type parameter.
    * @return
    */
  def parameterizedManifest(clss: Class[_], typeParams: Seq[RefInfo[_]]): RefInfo[_]

  def objRI: RefInfo[Object]
  def anyRI: RefInfo[Any]
  def anyValRI: RefInfo[AnyVal]
  def anyRefRI: RefInfo[AnyRef]
  def nothingRI: RefInfo[Nothing]

  def booleanRI: RefInfo[Boolean]
  def byteRI: RefInfo[Byte]
  def charRI: RefInfo[Char]
  def doubleRI: RefInfo[Double]
  def floatRI: RefInfo[Float]
  def intRI: RefInfo[Int]
  def longRI: RefInfo[Long]
  def shortRI: RefInfo[Short]
  def unitRI: RefInfo[Unit]
}
