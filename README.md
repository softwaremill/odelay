# odelay

[![Build Status](https://travis-ci.org/softprops/odelay.png?branch=master)](https://travis-ci.org/softprops/odelay)

A small set of primitives supporting delayed reactions in scala, reusing what tools you have on hand.

## usage

Odelay delays the execution of a task until after a specified [FiniteDuration][fd].
This differs from the behavior of Scala's [Futures][fut], which are defers until some non-deterministic time.

`Futures` are useful primitives for deferring tasks that may take a non trival amount of time to execute.

Delayed operations are useful when you know when you want a given task to be executed.

Odelay defines two primary primitives.

An `odelay.Timer` which executes a task after the provided delay and an `odelay.Timeout` which is the result of an `odelay.Timer's` task application.
These primitives often operate offstage, unseen by user code.

The interface for delaying tasks is `odelay.Delay`.

```scala
import scala.concurrent.duration._
odelay.Delay(2.seconds) {
  println("executed")
}
```

A delayed operation requires a `FiniteDuration` and some arbitrary block of code to execute after that delay.

### Timers

In order for the example above to compile, an `odelay.Timer` needs to be in implicit scope.

`odelay.Timers` implement an interface for making the async delay possible. You probably already have the tools to so on hand.
`odelay.Timers` adapt to those facilities. Implementations of `odelay.Timers` are defined for a number of environments and platforms so you can make the most of the tools you alread have on hand.

#### JdkTimer

The default Timer is a [ScheduledExecutorService][ses] backed Timer.

To make the example above compile. Import the default `Timer`.

```scala
import scala.concurrent.duration._
import odelay.Default.timer

odelay.Delay(2.seconds) {
  println("executed")
}
```

For extra flexibility, if you already have a [ScheduledExecutorService][ses], you may define your own jdk timer with resources you've already allocate and bring that into implicit scope.

```scala
import scala.concurrent.duration._
implicit val myJdkTimer = new odelay.jdk.JdkTimer(
  myScheduledExecutorService, interuptOnCancel)
 
odelay.Delay(2.seconds) {
  println("executed")
}
```

#### Netty(3)Timers

If your application is depend on [netty][netty] a widely adopted libary for writing asynchrous services on the JVM, there's a good chance you will want to use the `odelay-netty` ( netty 4 ) or `odelay-netty3` ( netty 3 ) modules which are backed by a netty [HashedWheelTimer][hwt].

To use one of these, bring the default netty timer into scope

```scala
import scala.concurrent.duration._

implicit val timer = odelay.netty.Default.timer
odelay.Delay(2.seconds) {
  println("executed")
}
```

Odelay provides the same level of flexibility with netty timers. If your application has already allocated a HashedWheelTimer, you can easily create an odelay Timer with that instead.

```scala
import scala.concurrent.duration._
implicit val timer = new odelay.netty.NettyTimer(myHashedWheelTimer)
odelay.Delay(2.seconds) {
  println("executed")
}
```

Netty 4+ defines a new concurrency primative called an `io.netty.util.concurrent.EventExecutorGroup`. Odelay's netty module defines a Timer interface for that as well. You will most likely have an EventExecutorGroup defines in your
netty pipeline. To create a Timer from one of them, you can do the following

```
import scala.concurrent.duration._
implicit val timer = new odelay.netty.NettyGroupTimer(
  myEventExecutorGroup)
odelay.Delay(2.seconds) {
  println("executed")
}
```

#### TwitterTimers

If your application is has the [twitter util][tu] suite of utilities on your class path, there's a good chance you will want to use the `odelay-twitter` module which defines an `odelay.Timer` in terms of twitter util's own timer interface, `com.twitter.util.Timer`. A default Timer is provided backed by a `com.twitter.util.JavaTimer`

```scala
import scala.concurrent.duration._
implicit val timer = odelay.twitter.Default.timer
odelay.Delay(2.seconds) {
  println("executed")
}
```

You may also define your own `odelay.Timer` in terms of a `com.twitter.util.Timer` which you may already have in scope.

```scala
import scala.concurrent.duration._
implicit val timer = new odelay.twitter.Timer(myTwitterTimer)
odelay.Delay(2.seconds) {
  println("executed")
}
```

### Releasing resources

`odelay.Timers` use thread resources to do their work. In order for a jvm to be shutdown cleanly, these thread resources need to be released.
Depending on your applications needs, you should really only need _one_ instance of an `odelay.Timer`.
When an application terminates, it should ensure be instrumented in a way that the `stop()` method of that `odelay.Timer` is invoked.

### Periodic delays

Odelay also provides an interface for usecases where you may wish to execute a task on a repeating series of perodic delays.
You can do so with the `odelay.Delay#every` interface which takes 3 argments: a `scala.concurrent.duration.FiniteDuration` representing the periodic delay, an optional `scala.concurrent.duration.FiniteDuration` representing the initial delay (the default is no delay), and a block of code to execute periodically.

The following example will print "executed" every two seconds until the resulting time out is canceled or the timer is stopped.

```scala
import scala.concurrent.duration._
import odelay.Default.timer

odelay.Delay.every(2.seconds)() {
  println("executed")
}
```

### Timeouts

Like `scala.concurrent.Futures` which provide a interface for _reacting_ to change, odelay delays return an `odelay.Timeout` value which
can be used to react to delays.

Since these are essential deferred values, `odelay.Timeouts` expose an `future` member which is a `Future` that will be satisfied as a success with the return type of block supplied to `odelay.Delay` when the future is scheduled. `odelay.Timeouts` may be also be canceled. This cancelation will satisfy the future in a failure state. Using this interface you may chain dependent actions in a "data flow" fashion. Note, the return type of a Timeout is determined by the block of code supplied. If your block returns a Future itself, the timeouts future being satisfied doesn't imply the blocks future will also be satsified as well.

```scala
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import odelay.Default.timer

odelay.Delay(2.seconds) {
  println("executed")
}.future.onSuccess {
  case _ => println("task scheduled")
}
```

Note, the import of the execution context. An implicit instance of one must be in scope for the invocation of a future's `onSuccess`.

#### Periodic Timeout futures

A periodic delay should intuitively never complete as a future can only be satisified once and a period deplay will be executed a number of times.
A cancelled periodic delay, however will still result in a future failure.

```scala
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import odelay.Default.timer

val timeout = odelay.Delay.every(2.seconds)() {
  println("executed")
}

timeout.future.onSuccess {
  case _ => println("this will never get called")
}

timeout.future.onFailure {
  case _ => println("this can get called, if you call timeout.cancel()")
}
```

Doug Tangren (softprops) 2014

[fd]: http://www.scala-lang.org/api/current/index.html#scala.concurrent.duration.FiniteDuration
[fut]: http://www.scala-lang.org/api/current/index.html#scala.concurrent.Future
[ses]: http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ScheduledExecutorService.html
[netty]: http://netty.io/
[hwt]: http://netty.io/4.0/api/io/netty/util/HashedWheelTimer.html
[tu]: http://twitter.github.io/util/
