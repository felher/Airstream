package com.raquo.airstream.split

import com.raquo.airstream.core.{EventStream, Signal}
import com.raquo.airstream.split.Splittable.IdSplittable
import com.raquo.airstream.util.Id
import scala.deriving._
import scala.annotation.implicitNotFound

class SplittableSubtypeEventStream[Input](val stream: EventStream[Input]) extends AnyVal {
  @inline def splitSubtype(using @implicitNotFound("You can only split by subtype on enums or sealed traits/classes") mirror: Mirror.SumOf[Input]): SubtypeSplitter[EventStream, EventStream, Input, mirror.MirroredElemTypes, Nothing] = 
    val splitOne = [O] =>
      (
        input: EventStream[Input],
        key: Input => Int,
        project: (Int, Input, EventStream[Input]) => O
      ) => input.splitOne(key)(project)

    SubtypeSplitter(splitOne, stream, mirror.ordinal, Array.empty)

  @inline def splitSubtypeIntoSignals(using @implicitNotFound("You can only split by subtype on enums or sealed traits/classes") mirror: Mirror.SumOf[Input]): SubtypeSplitter[EventStream, Signal, Input, mirror.MirroredElemTypes, Nothing] = {
    val splitOne = [O] =>
      (
        input: EventStream[Input],
        key: Input => Int,
        project: (Int, Input, Signal[Input]) => O
      ) => input.splitOne(key)((key, initialValue, eventStream) => project(key, initialValue, eventStream.toSignal(initialValue)))

    SubtypeSplitter(splitOne, stream, mirror.ordinal, Array.empty)
  }
}
