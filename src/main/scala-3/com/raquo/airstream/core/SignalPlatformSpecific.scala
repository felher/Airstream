package com.raquo.airstream.core

import com.raquo.airstream.split.SplittableSubtypeSignal

trait SignalPlatformSpecific {
  /** Provides methods on Signal: splitSubtype, splitSubtypeIntoSignals */
  implicit def toSplittableSubtypeSignal[A](signal: Signal[A]): SplittableSubtypeSignal[A] = new SplittableSubtypeSignal[A](signal)
}
