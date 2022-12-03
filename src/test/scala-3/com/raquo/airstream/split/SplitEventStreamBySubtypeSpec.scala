package com.raquo.airstream.split

import com.raquo.airstream.UnitSpec
import com.raquo.airstream.eventbus.EventBus
import com.raquo.airstream.core.EventStream
import com.raquo.airstream.fixtures.{Effect, TestableOwner}
import com.raquo.airstream.util.Id
import com.raquo.airstream.state.Var

import scala.collection.mutable

class SplitEventStreamBySubtypeSpec extends UnitSpec {
  sealed trait Food
  final case class Steak(count: Int) extends Food

  sealed trait Fruit extends Food
  final case class Apple(count: Int) extends Fruit
  final case class Orange(count: Int) extends Fruit

  it("splits stream into streams") {
    val effects = mutable.Buffer[Effect[String]]()

    val bus = new EventBus[Food]

    val owner = new TestableOwner

    val stream = bus.events.splitSubtype
      .handle((steakInit, steakEvents) => {
          effects += Effect("init-child", "S")
          steakEvents.map(s => "S" + s.count)
        }
      )
      .handle((fruitInit, fruitEvents) => {
          effects += Effect("init-child", "F")
          val subStream = fruitEvents.splitSubtype
            .handle((appleInit, appleEvents) => {
                effects += Effect("init-sub-child", "FA")
                appleEvents.map(a => "FA" + a.count)
              }
            )
            .handle((orangeInit, orangeEvents) => {
                effects += Effect("init-sub-child", "FO")
                orangeEvents.map(o => "FO" + o.count)
              }
            )
            .close

          subStream.flatMap(s => s)
        }
      )
      .close


    stream.flatMap(s => s).foreach { result =>
      effects += Effect("result", result.toString)
    }(owner)

    bus.writer.onNext(Steak(1))
    bus.writer.onNext(Steak(2))

    effects shouldEqual mutable.Buffer(
      Effect("init-child", "S"),
      Effect("result", "S1"),
      Effect("result", "S2"),
    )

    effects.clear()

    bus.writer.onNext(Apple(1))
    bus.writer.onNext(Apple(2))

    effects shouldEqual mutable.Buffer(
      Effect("init-child", "F"),
      Effect("init-sub-child", "FA"),
      Effect("result", "FA1"),
      Effect("result", "FA2"),
    )

    effects.clear()

    bus.writer.onNext(Orange(1))
    bus.writer.onNext(Orange(2))

    effects shouldEqual mutable.Buffer(
      Effect("init-sub-child", "FO"),
      Effect("result", "FO1"),
      Effect("result", "FO2"),
    )

    effects.clear()

    bus.writer.onNext(Steak(1))

    effects shouldEqual mutable.Buffer(
      Effect("init-child", "S"),
      Effect("result", "S1"),
    )
  }
}
