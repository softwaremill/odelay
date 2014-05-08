# odelay

[![Build Status](https://travis-ci.org/softprops/odelay.png?branch=master)](https://travis-ci.org/softprops/odelay)

Delayed reactions, fashioned from tools you already have sitting in your shed.

## usage

Odelay provides a simple interface producing Delays. Delays are to operations as Futures are to values, for given [FiniteDurations][fd].

### primitives

Odelay attempts to separate execution from interface by defining two primitives:

* an `odelay.Timer`, which defers task execution
* an `odelay.Delay`, which represents a delayed operation.

A delayed operation requires a [FiniteDuration][fd] and some arbitrary block of code to execute after that delay.

Typical usage is as follows.

```scala
import scala.concurrent.duration._
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
import odelay.Default.timer

odelay.Delay(2.seconds) {
  println("executed")
}
```

If you already have a [ScheduledExecutorService][ses] on hand, you may define your own jdk timer with resources you've already allocated and bring that into implicit scope.

```scala
import scala.concurrent.duration._
implicit val myJdkTimer = new odelay.jdk.JdkTimer(
  myScheduledExecutorService, interuptOnCancel)
 
odelay.Delay(2.seconds) {
  println("executed")
}
```

#### Netty(3)Timers

If your application's classpath includes [netty][netty], a widely adopted library for writing asynchronous services on the JVM, there's a good chance you will want to use the `odelay-netty` ( netty 4 ) or `odelay-netty3` ( netty 3 ) modules which are backed by a netty [HashedWheelTimer][hwt].

To use one of these, bring the default netty timer into scope

```scala
import scala.concurrent.duration._

implicit val timer = odelay.netty.Default.newTimer
odelay.Delay(2.seconds) {
  println("executed")
}
```

If your application has already allocated a HashedWheelTimer, you can easily create an odelay.Timer with that instead.

```scala
import scala.concurrent.duration._
implicit val timer = new odelay.netty.NettyTimer(myHashedWheelTimer)
odelay.Delay(2.seconds) {
  println("executed")
}
```

Netty 4+ defines a new concurrency primitive called an `io.netty.util.concurrent.EventExecutorGroup`. Odelay's netty module defines a Timer interface for that as well. You will most likely have an EventExecutorGroup defines in your
netty pipeline. To create a Timer from one of those, you can do the following

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
implicit val timer = odelay.twitter.Default.newTimer
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
Depending on your applications needs, you should really only need _one_ instance of an `odelay.Timer`.
When an application terminates, it should ensure be instrumented in a way that the `stop()` method of that `odelay.Timer` is invoked in ensure those thread resources are released so your application can shutdown cleanly.

### Periodic delays

Odelay also provides an interface for usecases where you wish to execute a task on a repeating series of periodic delays.
You can do so with the `odelay.Delay#every` interface which takes 3 curried arguments: a `scala.concurrent.duration.FiniteDuration` representing the periodic delay, an optional `scala.concurrent.duration.FiniteDuration` representing the initial delay (the default is no delay), and a block of code to execute periodically.

The following example will print "executed" every two seconds until the resulting time out is canceled or the timer is stopped.

```scala
import scala.concurrent.duration._
import odelay.Default.timer

odelay.Delay.every(2.seconds)() {
  println("executed")
}
```

### Delays

Like [Futures][fut], which provide a interface for _reacting_ to change, odelay delays return an `odelay.Delay` value which can be used to _react_ to delays.

Since Delays represent deferred operations, `odelay.Delays` expose a `future` method which returns a `Future` that will be satisfied as a success with the return type of block supplied to `odelay.Delay` when the future is scheduled. 

`odelay.Delays` may be also be canceled. This cancellation will satisfy the future in a failure state. Armed with this knowledge, you can chain dependent actions in a "data flow" fashion. Note, the return type of a Delay's Future is determined by the block of code supplied. If your block returns a Future itself, the Delay's future being satisfied doesn't imply the blocks future will also be satisfied as well. If you wish to chain these together, simply `flatMap` the results, `delay.future.flatMap(identity)`.

Below is an example of reacting the a delay's execution.

```scala
import scala.concurrent.duration._
// future execution
import scala.concurrent.ExecutionContext.Implicits.global
// delay execution
import odelay.Default.timer

odelay.Delay(2.seconds) {
  println("executed")
}.future.onSuccess {
  case _ => println("task scheduled")
}
```

Note, the import of the `ExecutionContext`. An implicit instance of one must be in scope for the invocation of a Future's `onSuccess` method.

#### Periodically Delayed futures

A periodic delay should intuitively never complete, as a Future can only be satisfied once and a period delay will be executed a number of times.

However, a canceled periodic delay will satisfy a periodic delay's Future in a failure state.

```scala
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import odelay.Default.timer

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

Doug Tangren (softprops) 2014

[fd]: http://www.scala-lang.org/api/current/index.html#scala.concurrent.duration.FiniteDuration
[fut]: http://www.scala-lang.org/api/current/index.html#scala.concurrent.Future
[ses]: http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ScheduledExecutorService.html
[netty]: http://netty.io/
[hwt]: http://netty.io/4.0/api/io/netty/util/HashedWheelTimer.html
[tu]: http://twitter.github.io/util/
