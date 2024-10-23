package slitstark.tests

class ShowSuite extends munit.FunSuite {
  import ShowSuite.*
  import Show.*
  import Show.given

  test("can derive Show for Person") {
    val show = summon[Show[Person]]
    assertEquals(show.show(Person("Alice", 42)), "Person(name = Alice, age = 42)")
  }

  test("can derive Show for USD") {
    val show = summon[Show[USD]]
    assertEquals(show.show(USD(100)), "USD(amount = 100)")
  }

  test("can derive Show for AnyValPerson") {
    val show = summon[Show[AnyValPerson]]
    assertEquals(show.show(AnyValPerson(Person("Alice", 42))), "AnyValPerson(person = Person(name = Alice, age = 42))")
  }
}

object ShowSuite {
  case class USD(amount: Int) extends AnyVal
  case class Person(name: String, age: Int)
  case class AnyValPerson(person: Person) extends AnyVal
}
