package slitstark.tests

import slitstark.anyval.*
import slitstark.anyval.given
import scala.compiletime.*

class AnyValMirrorSuite extends munit.FunSuite {
  import AnyValMirrorSuite.*

  test("can get USD mirror") {
    val mirror = slitstark.anyval.derived[USD]
    summon[mirror.type <:< AnyValMirrorOf[USD]]
  }

  test("can summon USD mirror") {
    summon[AnyValMirrorOf[USD]]
  }

  test("USD mirror has correct labels") {
    val mirror = summon[AnyValMirrorOf[USD]]
    assertEquals(constValue[mirror.MirroredLabel], "USD")
    assertEquals(constValue[mirror.MirroredElemLabel], "amount")
  }

  test("USD has correct types") {
    val mirror = summon[AnyValMirrorOf[USD]]
    summon[mirror.MirroredMonoType =:= USD]
    summon[mirror.MirroredElemType =:= Int]
  }

  test("can construct USD") {
    val mirror = slitstark.anyval.derived[USD]
    val usd = mirror.construct(100)
    assertEquals(usd.amount, 100)
  }
}

object AnyValMirrorSuite {
  case class USD(amount: Int) extends AnyVal
}
