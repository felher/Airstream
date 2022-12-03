package com.raquo.airstream.core

import com.raquo.airstream.split.SplittableSubtypeEventStream

trait EventStreamPlatformSpecific {
  /** Provides methods on EventStream: splitSubtype, splitSubtypeIntoSignlas */
  implicit def toSplittableSubtypeEventStream[A](stream: EventStream[A]): SplittableSubtypeEventStream[A] = new SplittableSubtypeEventStream(stream)
}
