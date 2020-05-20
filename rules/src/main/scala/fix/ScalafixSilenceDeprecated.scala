package fix

import metaconfig.Configured
import scalafix.v1._

import scala.meta.{Position, _}
import scala.meta.inputs.Input.{File, VirtualFile}

case class ScalafixSilenceDeprecatedConfig(since: List[String] = List.empty, debug: Boolean = false, quiet: Boolean = false) {
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

    if (config.debug) {
      println("Tree.syntax: " + doc.tree.syntax)
      println("Tree.structure: " + doc.tree.structure)
      println("Tree.structureLabeled: " + doc.tree.structureLabeled)
    }

    val deprecatedTerms = doc.tree.collect {
      case t @ Defn.Trait(Mod.Annot(Init(Type.Name("deprecated"), _, List(List(_, Lit.String(deprecatedSince))))) :: _, name, _, _, _) if config.isSince(deprecatedSince) => (t, deprecatedSince)
      case t @ Defn.Class(Mod.Annot(Init(Type.Name("deprecated"), _, List(List(_, Lit.String(deprecatedSince))))) :: _, name, _, _, _) if config.isSince(deprecatedSince) => (t, deprecatedSince)
      case t @ Defn.Object(Mod.Annot(Init(Type.Name("deprecated"), _, List(List(_, Lit.String(deprecatedSince))))) :: _, name, _) if config.isSince(deprecatedSince) => (t, deprecatedSince)
    }

    if (config.debug) {
      println("Deprecated: " + deprecatedTerms)
    }

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

    def describe: Any => String = {
      case VirtualFile(path, _) => path
      case File(path: AbsolutePath, _) => path.toString
      case pos: Position =>
        List(
          describe(pos.input),
          s"${pos.startLine}"
        ).mkString(":")
    }

    doc.tree.collect {
      case t @ Defn.Val(_,_,_,rhs) =>
        getDeprecatedCall(rhs) match {
          case since :: _ =>
            if (!config.quiet) {
              println(s"Silencing ${describe(t.pos)}: $since")
            }
            Patch.addLeft(t, s"""@silence("deprecated") // since $since\n""")
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
          case since :: _ =>
            if (!config.quiet) {
              println(s"Silencing ${describe(t.pos)}: $since")
            }
            Patch.addLeft(t, s"""@silence("deprecated") // since $since\n""")
          case List() => Patch.empty
        }

    }.asPatch
  }

}
