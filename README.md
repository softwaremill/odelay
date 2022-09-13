# odelay

[![Build Status](https://travis-ci.org/softwaremill/odelay.png?branch=master)](https://travis-ci.org/softwaremill/odelay)

Delayed reactions, fashioned from tools you already have sitting around your shed.

## installation

The current version of odelay is `0.4.0` and targets scala 2.12+. The odelay-twitter module is not published for 2.11.*.

### modules

* `odelay-core` odelay core interfaces and default jdk backed timer

```scala
libraryDependencies += "com.softwaremill.odelay" %% "odelay-core" % "0.4.0"
```

* `odelay-netty` netty 4 backed odelay timer interface

```scala
libraryDependencies += "com.softwaremill.odelay" %% "odelay-netty" % "0.4.0"
```

* `odelay-netty3` netty 3 backed odelay timer interface

```scala
libraryDependencies += "com.softwaremill.odelay" %% "odelay-netty3" % "0.4.0"
```

* `odelay-twitter` twitter util backed odelay timer interface

```scala
libraryDependencies += "com.softwaremill.odelay" %% "odelay-twitter" % "0.4.0"
```

## usage

Odelay provides a simple interface producing Delays. Delays are to operations as [Futures][fut] are to values, for given [FiniteDurations][fd].

### primitives

Odelay separates execution from interface by defining two primitives:

* an `odelay.Timer`, which defers task execution
* an `odelay.Delay`, which represents a delayed operation.

A delayed operation requires a [FiniteDuration][fd] and some arbitrary block of code which will execute after that duration.

Typical usage is as follows.

```scala
import scala.concurrent.duration._

// print "executed" after a 2 second delay
odelay.Delay(2.seconds) {
  println("executed")
}
```

### Timers

In order for the example above to compile, an instance of an `odelay.Timer` needs to be in implicit scope, just as an ExecutionContext would when working with Scala Futures.

`odelay.Timers` define an interface for task scheduling. Implementations of `odelay.Timers` are defined for a number of environments and platforms.

#### JdkTimer

The default Timer is a standard jdk [ScheduledExecutorService][ses] backed Timer.

To make the example above compile, import the default `Timer`.

```scala
import scala.concurrent.duration._

// bring default timer into scope
import odelay.Timer.default

odelay.Delay(2.seconds) {
  println("executed")
}
```

If you have already allocated your own [ScheduledExecutorService][ses], you may define your own jdk timer reusing those thread resources and bring that into implicit scope.

```scala
import scala.concurrent.duration._

// define a new JdkTimer instance with resources preallocated
implicit val myJdkTimer = new odelay.jdk.JdkTimer(
  myScheduledExecutorService, interuptOnCancel)
 
odelay.Delay(2.seconds) {
  println("executed")
}
```

#### Netty(3)Timers

If your application's classpath includes [netty][netty], a widely adopted library for writing asynchronous services on the JVM, there's a good chance you will want to use the `odelay-netty` ( netty 4 ) or `odelay-netty3` ( netty 3 ) modules which are backed by a netty [HashedWheelTimer][hwt].

To use one of these, bring an instance of the default netty timer into scope

```scala
import scala.concurrent.duration._

// create a new netty timer and bring it into explicit scope
implicit val timer = odelay.netty.NettyTimer.newTimer

odelay.Delay(2.seconds) {
  println("executed")
}
```

If your application has already allocated a HashedWheelTimer, you can easily create your own odelay.Timer instance backed with resources you have
already allocated.

```scala
import scala.concurrent.duration._

implicit val timer = new odelay.netty.NettyTimer(myHashedWheelTimer)

odelay.Delay(2.seconds) {
  println("executed")
}
```

Netty 4+ defines a new concurrency primitive called an `io.netty.util.concurrent.EventExecutorGroup`. Odelay's netty module defines a Timer interface for that as well. You will most likely have an EventExecutorGroup defines in your
netty pipeline. To create a Timer instance from one of those, you can do the following

```scala
import scala.concurrent.duration._

implicit val timer = new odelay.netty.NettyGroupTimer(
  myEventExecutorGroup)

odelay.Delay(2.seconds) {
  println("executed")
}
```

#### TwitterTimers

If your application has the [twitter util][tu] suite of utilities on its classpath, there's a good chance you will want to use the `odelay-twitter` module which defines an `odelay.Timer` in terms of twitter util's own timer interface, `com.twitter.util.Timer`. A default Timer is provided backed by a `com.twitter.util.JavaTimer`

```scala
import scala.concurrent.duration._
implicit val timer = odelay.twitter.TwitterTimer.newTimer
odelay.Delay(2.seconds) {
  println("executed")
}
```

You may also define your own `odelay.Timer` in terms of a `com.twitter.util.Timer` which you may already have in scope.

```scala
import scala.concurrent.duration._
implicit val timer = new odelay.twitter.TwitterTimer(myTwitterTimer)
odelay.Delay(2.seconds) {
  println("executed")
}
```

### Releasing resources

`odelay.Timers` use thread resources to do their work. In order for a jvm to be shutdown cleanly, these thread resources need to be released.
Depending on your applications needs, you should really only need _one_ instance of an `odelay.Timer` for a given process.

When an application terminates, it should be instrumented in a way that ensures the `stop()` method of that `odelay.Timer` is invoked. This ensures thread resources are released so your application can shutdown cleanly. Calling `stop()` on a Timer will most likely result failed promises if
a new Delay is attempted with the stopped timer.

### Periodic delays

Odelay also provides an interface for cases where you wish to execute a task on a repeating interval of periodic delays.
You can do so with the `odelay.Delay#every` interface which takes 3 curried arguments: a `scala.concurrent.duration.FiniteDuration` representing the periodic delay, an optional `scala.concurrent.duration.FiniteDuration` representing the initial delay (the default is no delay), and a block of code to execute periodically.

The following example will print "executed" every two seconds until the resulting time out is canceled or the timer is stopped.

```scala
import scala.concurrent.duration._

import odelay.Timer.default

odelay.Delay.every(2.seconds)() {
  println("executed")
}
```

### Delays

Like [Futures][fut], which provide a interface for _reacting_ to changes of a deferred value, odelay operations produce `odelay.Delay` values, which can be used to _react_ to timer operations.

Since Delays represent deferred operations, `odelay.Delays` expose a `future` method which returns a `Future` that will be satisfied as a success with the return type of block supplied to `odelay.Delay` when the operation is scheduled. 

`odelay.Delays` may be canceled. Cancellation will satisfy the Future in a failure state. Armed with this knowledge, you can chain dependent actions in a "data flow" fashion. Note, the return type of a Delay's Future is determined by the block of code supplied. If your block returns a Future itself, the Delay's future being satisfied doesn't imply the blocks future will also be satisfied as well. If you wish to chain these together, simply `flatMap` the results, `delay.future.flatMap(identity)`.

Below is an example of reacting the a delay's execution.

```scala
import scala.concurrent.duration._

// future execution
import scala.concurrent.ExecutionContext.Implicits.global

// delay execution
import odelay.Timer.default

odelay.Delay(2.seconds) {
  println("executed")
}.future.onSuccess {
  case _ => println("task scheduled")
}
```

Note, the import of the `ExecutionContext`. An implicit instance of one must be in scope for the invocation of a Future's `onSuccess` method.

#### Periodically Delayed futures

A periodic delay's future should intuitively never complete, as a Future can only be satisfied once and a period delay will be executed a number of times.

However, a canceled periodic delay will satisfy a periodic delay's Future in a failure state.

```scala
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import odelay.Timer.default

val delay = odelay.Delay.every(2.seconds)() {
  println("executed")
}

delay.future.onSuccess {
  case _ => println("this will never get called")
}

delay.future.onFailure {
  case _ => println("this can get called, if you call delay.cancel()")
}
```

## Credits

Originally created by [Doug Tangren](https://github.com/softprops), maintained by [SoftwareMill](https://softwaremill.com).

[fd]: http://www.scala-lang.org/api/current/index.html#scala.concurrent.duration.FiniteDuration
[fut]: http://www.scala-lang.org/api/current/index.html#scala.concurrent.Future
[ses]: http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ScheduledExecutorService.html
[netty]: http://netty.io/
[hwt]: http://netty.io/4.0/api/io/netty/util/HashedWheelTimer.html
[tu]: http://twitter.github.io/util/
