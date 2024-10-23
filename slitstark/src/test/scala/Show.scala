package slitstark.tests

trait Show[T]:
  def show(x: T): String

object Show:
  given Show[Int] with
    def show(x: Int) = x.toString
  
  given Show[String] with
    def show(x: String) = x

  import scala.deriving.*
  import scala.compiletime.*

  inline def summonInstances[T, Elems <: Tuple]: List[Show[?]] =
    inline erasedValue[Elems] match
      case _: (elem *: elems) => deriveOrSummon[T, elem] :: summonInstances[T, elems]
      case _: EmptyTuple => Nil

  inline def deriveOrSummon[T, Elem]: Show[Elem] =
    inline erasedValue[Elem] match
      case _: T => deriveRec[T, Elem]
      case _    => summonInline[Show[Elem]]

  inline def deriveRec[T, Elem]: Show[Elem] =
    inline erasedValue[T] match
      case _: Elem => error("infinite recursive derivation")
      case _       => Show.derived[Elem](using summonInline[Mirror.Of[Elem]])

  def showSum[T](s: Mirror.SumOf[T], elems: => List[Show[?]]) = new Show[T]:
    def show(x: T): String = {
      val ord = s.ordinal(x)
      val elem = elems(ord).asInstanceOf[Show[T]]
      elem.show(x)
    }

  inline def showProduct[T](p: Mirror.ProductOf[T], elems: => List[Show[?]]): Show[T] = new Show[T]:
    def show(x: T): String = {
      val productLabelNames = constValueTuple[p.MirroredElemLabels].toList.asInstanceOf[List[String]]
      val productElems = x.asInstanceOf[Product].productIterator.toList
      val product = productLabelNames.lazyZip(productElems).lazyZip(elems).map { case (name, value, inst) =>
        s"$name = ${inst.asInstanceOf[Show[Any]].show(value)}"
      }
      val label = constValue[p.MirroredLabel]
      s"${label}(${product.mkString(", ")})"
    }

  inline def derived[T](using m: Mirror.Of[T]): Show[T] =
    lazy val elemInstances = summonInstances[T, m.MirroredElemTypes]
    inline m match {
      case s: Mirror.SumOf[T] => showSum(s, elemInstances)
      case p: Mirror.ProductOf[T] => showProduct(p, elemInstances)
    }

  inline given [T](using Mirror.Of[T]): Show[T] = derived[T]

  import slitstark.anyval.*
  export slitstark.anyval.given

  inline def derivedAnyVal[T](using m: AnyValMirrorOf[T]): Show[T] =
    lazy val elemInstance = summonInline[Show[m.MirroredElemType]]
    val label = constValue[m.MirroredLabel]
    val elemLabel = constValue[m.MirroredElemLabel]
    new Show[T]:
      def show(x: T): String = {
        val elem = m.value(x)
        s"$label($elemLabel = ${elemInstance.show(elem)})"
      }

  inline given [T](using m: AnyValMirrorOf[T]): Show[T] = derivedAnyVal[T]