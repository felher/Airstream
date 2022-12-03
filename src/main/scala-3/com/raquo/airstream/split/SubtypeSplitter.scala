package com.raquo.airstream.split

import com.raquo.airstream.core.{EventStream, Signal}
import scala.deriving._
import scala.compiletime._
import scala.quoted.*

class SubtypeSplitter[
    M[+_],
    MHandler[+_],
    Input,
    Todo <: Tuple,
    Output
](
  splitOne: [O] => (M[Input], Input => Int, (Int, Input, MHandler[Input]) => O) => M[O],
  input: M[Input],
  key: Input => Int,
  handlers: Array[(Any, Any) => Output]
) {
  inline def close: M[Output] = {
    inline erasedValue[Todo] match {
      case _: EmptyTuple.type =>
        splitOne(input, key, (key, initial, subSig) => handlers(key)(initial, subSig))

      case _: *:[h, t] =>
        SubtypeSplitter.printCloseError(SubtypeSplitter.nameOf[h])
    }
  }

  def handle[OO >: Output](
      caseHandler: (Tuple.Elem[Todo, 0], MHandler[Tuple.Elem[Todo, 0]]) => OO
  ): SubtypeSplitter[M, MHandler, Input, Tuple.Drop[Todo, 1], OO] =
    SubtypeSplitter(splitOne, input, key, handlers :+ caseHandler.asInstanceOf)
}


object SubtypeSplitter {
  private def nameOfImpl[A](using t: Type[A], ctx: Quotes): Expr[String] = Expr(Type.show[A])
  private inline def nameOf[A] = ${ nameOfImpl[A] }

  private inline def printCloseError(name: String) = {
    error("""
You can only close the split after all cases are handled.
Case """ + name + """ is not handled yet.
Add another handler with .handle((in: Signal[""" + name + """]) => result))
      """
    )
  }
}
