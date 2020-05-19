package fix

import scalafix.v1._
import scala.meta._

class ScalafixSilenceDeprecated extends SemanticRule("ScalafixSilenceDeprecated") {

  override def fix(implicit doc: SemanticDocument): Patch = {
    println("Tree.syntax: " + doc.tree.syntax)
    println("Tree.structure: " + doc.tree.structure)
    println("Tree.structureLabeled: " + doc.tree.structureLabeled)

    val deprecatedSince = "xxx-lib 1.2.3"

    val deprecatedTerms = doc.tree.collect {
      case t @ Defn.Trait(Mod.Annot(Init(Type.Name("deprecated"), _, List(List(_, Lit.String(`deprecatedSince`))))) :: _, name, _, _, _) => t
      case t @ Defn.Class(Mod.Annot(Init(Type.Name("deprecated"), _, List(List(_, Lit.String(`deprecatedSince`))))) :: _, name, _, _, _) => t
      case t @ Defn.Object(Mod.Annot(Init(Type.Name("deprecated"), _, List(List(_, Lit.String(`deprecatedSince`))))) :: _, name, _) => t
    }

    println("Deprecated terms: " + deprecatedTerms)

    def hasDeprecatedType(t: Type): Boolean = {
      t match {
        case t @ Type.Name(name) if deprecatedTerms.exists { dt => dt.name.value == name } => true
        case t @ Type.Apply(
          tpe @ Type.Name(_),
          args @ names
          ) if names.exists { name => hasDeprecatedType(name) } => true
        case _ => false
      }
    }

    def callsDeprecated(t: Tree): Boolean = {
      t.collect {
        case t @ Term.Name(name) if deprecatedTerms.exists { dt => dt.name.value == name } => true


      }.nonEmpty
    }

    doc.tree.collect {
      case t @ Defn.Val(_,_,_,rhs) if callsDeprecated(rhs) => Patch.addLeft(t, "// @silence(\"deprecated\") // since xxx-lib 1.2.3\n")

      case t @ Defn.Def(
      mods,
      name,
      tparams,
      paramss,
      decltpe @ Some(tpe),
      body) if hasDeprecatedType(tpe) => Patch.addLeft(t, "// @silence(\"deprecated\") // since xxx-lib 1.2.3\n")

    }.asPatch
  }

}
