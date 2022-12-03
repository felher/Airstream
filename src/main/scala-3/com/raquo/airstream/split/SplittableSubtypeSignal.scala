package com.raquo.airstream.split

import com.raquo.airstream.core.Signal
import com.raquo.airstream.split.Splittable.IdSplittable
import com.raquo.airstream.util.Id
import scala.deriving._
import scala.annotation.implicitNotFound

class SplittableSubtypeSignal[Input](val signal: Signal[Input]) extends AnyVal {
  @inline def splitSubtype(using @implicitNotFound("You can only split by subtype on enums or sealed traits/classes") mirror: Mirror.SumOf[Input]): SubtypeSplitter[Signal, Signal, Input, mirror.MirroredElemTypes, Nothing] = 
    splitSubtypeIntoSignals

  @inline def splitSubtypeIntoSignals(using @implicitNotFound("You can only split by subtype on enums or sealed traits/classes") mirror: Mirror.SumOf[Input]): SubtypeSplitter[Signal, Signal, Input, mirror.MirroredElemTypes, Nothing] = {
      val splitOne = [O] =>
        (
          input: Signal[Input],
          key: Input => Int,
          project: (Int, Input, Signal[Input]) => O
        ) => input.splitOne(key)(project)

      SubtypeSplitter(splitOne, signal, mirror.ordinal, Array.empty)
  }
}
