# slitstark

Poor man's Mirrors for value classes.

> Slitstark - Swedish for "durable", but also the cheapest metalic ladle at Ikea i.e. a very-poor man's mirror.

## Description

Slitstark provides a way to write derivations for value classes without the need to use macro-reflection.

It defines an `AnyValMirror` type and a given instance for it, which provides a similar API to Scala 3's Mirrors, except for value classes.

## Example

```scala
trait Show[T]:
  def show(x: T): String

object Show:
  given Show[Int] with
    def show(x: Int) = x.toString

  import slitstark.anyval.*

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

  export slitstark.anyval.given
```

When using the `Show` type class, you can now write the following code:

```scala
case class USD(amount: Int) extends AnyVal

import Show.given
val showUSD = summon[Show[USD]]
```

## Usage (in code)

Let's consider writing a derivation for the `Show` type class, defined as follows:

```scala
trait Show[T]:
    def show(x: T): String
```

A standard derivation (with some simple instances) for `Show` can be written as follows. Please don't read the whole thing, I mostly stole it from the official docs for `Eq`. ([link]())

```scala
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
```

In order to add the support for value classes i.e. classes that extend `AnyVal`, you can add the following code:

```scala
// object Show:
  import slitstark.anyval.*

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

  export slitstark.anyval.given // either export the given instance here or import it in the scope where you want to use it
```

When using the `Show` type class, you can now write the following code:

```scala
// case class USD(amount: Int) extends AnyVal

import Show.given
// import slitstark.anyval.given // if you didn't export the given instance in the Show object
val showUSD = summon[Show[USD]]
```

## Usage (with build tools)

TODO when it is published or something.
