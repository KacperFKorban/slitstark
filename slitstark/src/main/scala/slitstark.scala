package slitstark

import scala.quoted.*

object anyval {

  trait AnyValMirror { self =>
    /** The mirrored *-type */
    type MirroredMonoType <: AnyVal

    /** The name of the type */
    type MirroredLabel <: String

    /** The names of the product elements */
    type MirroredElemLabel <: String

    /** The types of the product elements */
    type MirroredElemType

    /** The mirrored type */
    def construct(param: MirroredElemType): MirroredMonoType

    /** Get the value of the inner element */
    def value(mirrored: MirroredMonoType): MirroredElemType

    // def toProductMirror: scala.deriving.Mirror.ProductOf[self.MirroredMonoType] = new scala.deriving.Mirror.Product {
    //   type MirroredMonoType = self.MirroredMonoType
    //   type MirroredLabel = self.MirroredLabel
    //   type MirroredElemTypes = self.MirroredElemType *: EmptyTuple
    //   type MirroredElemLabels = self.MirroredElemLabel *: EmptyTuple
    //   def fromProduct(p: scala.Product): MirroredMonoType = construct(p.productElement(0).asInstanceOf[MirroredElemType])
    // }
  }

  type AnyValMirrorOf[T] = AnyValMirror { type MirroredMonoType = T }

  transparent inline given derived[T <: AnyVal]: AnyValMirrorOf[T] = ${ macros.derivedAnyValMirrorImpl[T] }

  object AnyValMirror {
    private[slitstark] def make[MirroredMonoType0 <: AnyVal, MirroredLabel0 <: String, MirroredElemLabel0 <: String, MirroredElemType0](
      constr: (MirroredElemType0) => MirroredMonoType0,
      value0: (MirroredMonoType0) => MirroredElemType0
    ): AnyValMirror {
      type MirroredMonoType = MirroredMonoType0
      type MirroredLabel = MirroredLabel0
      type MirroredElemLabel = MirroredElemLabel0
      type MirroredElemType = MirroredElemType0
    } = new AnyValMirror {
      type MirroredMonoType = MirroredMonoType0
      type MirroredLabel = MirroredLabel0
      type MirroredElemLabel = MirroredElemLabel0
      type MirroredElemType = MirroredElemType0
      def construct(param: MirroredElemType): MirroredMonoType = constr(param)
      def value(mirrored: MirroredMonoType0): MirroredElemType = value0(mirrored)
    }
  }
}
