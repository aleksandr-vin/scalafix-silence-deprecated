package fix

object SimpleFunctionWithConfig {

  @deprecated("Foo is bad", "xxx-lib 1.2.3")
  trait Foo {}

  @deprecated("FooS is bad", "xxx-lib 1.2.3")
  sealed trait FooS {}

  @deprecated("Bar is bad", "xxx-lib 1.2.3")
  case class Bar(s: String)

  @deprecated("BarO is bad", "zzz-lib 1.2.3")
  object BarO {}

  /**
   * Docs
   */
  def some(): Unit = {
    val x = Bar("xx")
    // @silence("deprecated") // since zzz-lib 1.2.3
val s = BarO
    val z = Unit
  }

  def foo1: Bar = {
    Bar("xxxx")
  }
}