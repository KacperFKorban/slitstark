package slitstark

import scala.quoted.*
import scala.annotation.*
import slitstark.anyval.*

@nowarn("msg=match may not be exhaustive")
private[slitstark] object macros {
  def derivedAnyValMirrorImpl[T: Type](using Quotes): Expr[AnyValMirrorOf[T]] = {
    import quotes.reflect.*
    val tpe = TypeRepr.of[T]
    val AnyValSymbol = Symbol.requiredClass("scala.AnyVal")
    if !tpe.derivesFrom(AnyValSymbol) then
      report.error("AnyValMirrors are only provided for value classes")
    val mirroredMonoType = TypeRepr.of[T]
    val mirroredLabel = ConstantType(StringConstant(mirroredMonoType.typeSymbol.name))
    val tConstructor = tpe.classSymbol.get.primaryConstructor
    val tConstructorParams = tConstructor.paramSymss.flatten
    val mirroredElemLabelString = tConstructorParams.map(_.name.toString()).headOption.getOrElse("")
    val mirroredElemLabel = ConstantType(StringConstant(mirroredElemLabelString))
    val mirroredElemType = tConstructorParams.filter(_.isTerm).map(_.tree.asInstanceOf[ValDef].tpt.tpe)
      .headOption.getOrElse(report.errorAndAbort("No constructor parameters found"))
    val constr = Lambda(Symbol.spliceOwner, MethodType(List("param"))(_ => List(mirroredElemType), _ => mirroredMonoType), {
      case (mtpe, List(paramTree)) => constructImpl(paramTree.asExpr).asTerm
    })
    val value = Lambda(Symbol.spliceOwner, MethodType(List("mirrored"))(_ => List(mirroredMonoType), _ => mirroredElemType), {
      case (mtpe, List(mirroredTree)) => Select.unique(mirroredTree.asExpr.asTerm, mirroredElemLabelString)
    })
    val anyValMirrorObject = Symbol.requiredModule("slitstark.anyval.AnyValMirror")
    val make = Select.unique(Ref(anyValMirrorObject), "make")
    val makeApplied = make.appliedToTypes(List(mirroredMonoType, mirroredLabel, mirroredElemLabel, mirroredElemType)).appliedTo(constr, value)
    makeApplied.asExprOf[AnyValMirrorOf[T]]
  }

  private def constructImpl[T: Type](param: Expr[Any])(using Quotes): Expr[T] = {
    import quotes.reflect.*
    val tpe = TypeRepr.of[T]
    val paramTerm = param.asTerm
    val newTerm = New(Inferred(tpe))
    val tConstructor = tpe.classSymbol.get.primaryConstructor
    val selectTerm = newTerm.select(tConstructor)
    val applyTerm = selectTerm.appliedTo(paramTerm)
    applyTerm.asExprOf[T]
  }
}
