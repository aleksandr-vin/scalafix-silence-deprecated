/*
rule = ScalafixSilenceDeprecated
*/
package fix

object SimpleFunction {

  @deprecated("Foo is bad", "xxx-lib 1.2.3")
  trait Foo {}

  @deprecated("FooS is bad", "xxx-lib 1.2.3")
  sealed trait FooS {}

  @deprecated("Bar is bad", "xxx-lib 1.2.3")
  case class Bar(s: String)

  @deprecated("BarO is bad", "xxx-lib 1.2.3")
  object BarO {}

  /**
   * Docs
   */
  def some(): Unit = {
    val x = Bar("xx")
    val s = BarO
    val z = Unit
  }

  def foo1: Bar = {
    Bar("xxxx")
  }
}
