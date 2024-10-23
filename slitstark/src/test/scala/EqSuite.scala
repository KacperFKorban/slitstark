package slitstark.tests

class EqSuite extends munit.FunSuite {
  import EqSuite.*
  import Eq.*
  import Eq.given

  test("can derive Eq for Person") {
    val eq = summon[Eq[Person]]
    assertEquals(eq.eqv(Person("Alice", 42), Person("Alice", 42)), true)
    assertEquals(eq.eqv(Person("Alice", 42), Person("Bob", 42)), false)
    assertEquals(eq.eqv(Person("Alice", 42), Person("Alice", 43)), false)
  }

  test("can derive Eq for USD") {
    val eq = summon[Eq[USD]]
    assertEquals(eq.eqv(USD(100), USD(100)), true)
    assertEquals(eq.eqv(USD(100), USD(200)), false)
  }

  test("can derive Eq for AnyValPerson") {
    val eq = summon[Eq[AnyValPerson]]
    assertEquals(eq.eqv(AnyValPerson(Person("Alice", 42)), AnyValPerson(Person("Alice", 42))), true)
    assertEquals(eq.eqv(AnyValPerson(Person("Alice", 42)), AnyValPerson(Person("Bob", 42))), false)
    assertEquals(eq.eqv(AnyValPerson(Person("Alice", 42)), AnyValPerson(Person("Alice", 43))), false)
  }
}

object EqSuite {
  case class USD(amount: Int) extends AnyVal
  case class Person(name: String, age: Int)
  case class AnyValPerson(person: Person) extends AnyVal
}
