/*
rule = ScalafixSilenceDeprecated
ScalafixSilenceDeprecated.since = [
  xxx-lib 1.2.3
]
*/
package fix

object DummyClass {

  @deprecated("Foo is bad", "xxx-lib 1.2.3")
  trait Foo {}

  @deprecated("FooS is bad", "xxx-lib 1.2.3")
  sealed trait FooS {}

  @deprecated("Bar is bad", "xxx-lib 1.2.3")
  case class Bar(s: String)

  @deprecated("BarO is bad", "xxx-lib 1.2.3")
  object BarO {}

  trait Base

  // xxx
  // TODO: not supported yet
  class Dummy
    extends Base
      with Foo
      with FooS {}

  // xxx
  class Funny
}
