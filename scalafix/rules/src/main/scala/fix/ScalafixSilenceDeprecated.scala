package fix

import metaconfig.Configured
import scalafix.v1._

import scala.meta._

case class ScalafixSilenceDeprecatedConfig(since: List[String] = List.empty) {
  def isSince: Any => Boolean = since.contains _
}

object ScalafixSilenceDeprecatedConfig {
  def default = ScalafixSilenceDeprecatedConfig()
  implicit val surface =
    metaconfig.generic.deriveSurface[ScalafixSilenceDeprecatedConfig]
  implicit val decoder =
    metaconfig.generic.deriveDecoder(default)
}

class ScalafixSilenceDeprecated(config: ScalafixSilenceDeprecatedConfig) extends SemanticRule("ScalafixSilenceDeprecated") {

  def this() = this(ScalafixSilenceDeprecatedConfig())

  override def withConfiguration(config: Configuration): Configured[Rule] =
    config.conf
    .getOrElse("ScalafixSilenceDeprecated")(this.config)
    .map { newConfig => new ScalafixSilenceDeprecated(newConfig) }

  override def description: String = "Annotate with @silent(\"deprecated\") all usages of deprecated library"

  override def fix(implicit doc: SemanticDocument): Patch = {

    println("Tree.syntax: " + doc.tree.syntax)
    println("Tree.structure: " + doc.tree.structure)
    println("Tree.structureLabeled: " + doc.tree.structureLabeled)

    val deprecatedTerms = doc.tree.collect {
      case t @ Defn.Trait(Mod.Annot(Init(Type.Name("deprecated"), _, List(List(_, Lit.String(deprecatedSince))))) :: _, name, _, _, _) if config.isSince(deprecatedSince) => (t, deprecatedSince)
      case t @ Defn.Class(Mod.Annot(Init(Type.Name("deprecated"), _, List(List(_, Lit.String(deprecatedSince))))) :: _, name, _, _, _) if config.isSince(deprecatedSince) => (t, deprecatedSince)
      case t @ Defn.Object(Mod.Annot(Init(Type.Name("deprecated"), _, List(List(_, Lit.String(deprecatedSince))))) :: _, name, _) if config.isSince(deprecatedSince) => (t, deprecatedSince)
    }

    println("Deprecated terms: " + deprecatedTerms)

    def getDeprecatedType(t: Type): List[String] = {
      t match {
        case t @ Type.Name(name) => deprecatedTerms.collect { case (dt, since) if dt.name.value == name => since }
        case t @ Type.Apply(
          tpe @ Type.Name(_),
          args @ names
          ) => names.flatMap(getDeprecatedType)
        case _ => List()
      }
    }

    def getDeprecatedCall(t: Tree): List[String] = {
      t.collect {
        case t @ Term.Name(name) =>
          deprecatedTerms.collect { case (dt, since) if dt.name.value == name => since }
      }.flatten
    }

    doc.tree.collect {
      case t @ Defn.Val(_,_,_,rhs) =>
        getDeprecatedCall(rhs) match {
          case since :: _ => Patch.addLeft(t, s"""// @silence("deprecated") // since $since\n""")
          case List() => Patch.empty
        }

      case t @ Defn.Def(
      mods,
      name,
      tparams,
      paramss,
      decltpe @ Some(tpe),
      body) =>
        getDeprecatedType(tpe) match {
          case since :: _ => Patch.addLeft(t, s"""// @silence("deprecated") // since $since\n""")
          case List() => Patch.empty
        }

    }.asPatch
  }

}
