# odelay

[![Build Status](https://travis-ci.org/softprops/odelay.png?branch=master)](https://travis-ci.org/softprops/odelay)

A small set of primatives supporting delayed reactions in scala, reusing what tools you have on hand.

## usage

Odelay executes tasks after a specified [FiniteDuration][fd].
This differs from the behavior the default execution of [Futures][fut], which are executed at some non-deterministic time.
`Futures` are useful primatives for deferring tasks that may take a non trival amount of time to execute.
Delayed operations are useful when you know up front when you want a given task to be executed.

Odelay defines two primary primatives.

An `odelay.Timer` which executes a tasks after the provided delay and an `odelay.Timeout` which is the result of an `odelay.Timers` task application.
These primatives often work offstage, unseen by user code.

The interface for delaying tasks is `odelay.Delay`.

```scala
import scala.concurrent.duration._
odelay.Delay(2.seconds) {
  println("executed")
}
```

A delayed operation requires a `FiniteDuration` and some arbitrary block of code to execute.

### Timers

In order for the example above to compile, an `odelay.Timer` needs to be in implicit scope.

`odelay.Timers` implement an interface for making the delay possible.
Implementations for `odelay.Timers` are defined for a number of environments and platforms so you can make the most of the tools you alread have on hand.

#### JdkTimer

The default Timer is a jdk backed Timer.

To make the example above compile. Import the default `Timer`.

```scala
import scala.concurrent.duration._
import odelay.Default.timer

odelay.Delay(2.seconds) {
  println("executed")
}
```

For flexibility, if you already have a [ScheduledExecutorService][ses], you may define your own jdk timer with resources you've already allocated.

```scala
import scala.concurrent.duration._
implicit val myJdkTimer = new odelay.jdk.JdkTimer(
  myScheduledExecutorService, interuptOnCancel)
 
odelay.Delay(2.seconds) {
  println("executed")
}
```

#### Netty(3)Timers

If your application is built with [netty][netty], there's a good chance you will want to use the `odelay-netty` ( netty 4 ) or `odelay-netty3` ( netty 3 )
modules which are backed by a netty [HashedWheelTimer][hwt].

```scala
import scala.concurrent.duration._

implicit val timer = odelay.netty.Default.timer
odelay.Delay(2.seconds) {
  println("executed")
}
```

Odelay provides the same flexibility to create your own if your application has already allocated a HashedWheelTimer.

```scala
import scala.concurrent.duration._
implicit val timer = new odelay.netty.NettyTimer(myHashedWheelTimer)
odelay.Delay(2.seconds) {
  println("executed")
}
```

#### TwitterTimers

If your application is build around the [twitter util][tu] suite of utilities, there's a good chance you will want to use the `odelay-twitter` module which 
defines an `odelay.Timer` in terms of twitter util's own timer interface, `com.twitter.util.Timer`. A default Timer is provided backed by a `com.twitter.util.JavaTimer`

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

### releasing resources

`odelay.Timers` use thread resources to do their work. In order for a jvm to be shutdown cleanly, these thread resources need to be released.
A typical application should only use _one_ instance of an `odelay.Timer`.
When an application terminates, it should ensure that the `stop()` method of that `odelay.Timer` is invoked.

### Periodic delays

Odelay also provides an interface for usecases where you may wish to execute a task after a series of perodic delays.
You can do so with the `odelay.Delay#every` interface which takes 3 argments: a `scala.concurrent.duration.FiniteDuration` representing the periodic delay, an optional `scala.concurrent.duration.FiniteDuration` representing the initial delay (the default is no delay), and a block of code to execute periodically.

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

`odelay.Timeouts` expose an `future` member which is a `Future` that will be satisfied as a success with the return type of block supplied to `odelay.Delay` when the future is scheduled. `odelay.Timeouts` may be also be canceled. This cancelation will satisfy the future in a failure state. Using this interface you may chain dependent actions in a "data flow" fashion. Note, the return type of a Timeout is determined by the block of code supplied. If your block returns a Future itself, the timeouts future being satisfied doesn't imply the blocks future will also be satsified as well.

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
