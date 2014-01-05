# odelay

A small set of primatives supporting delayed reactions

## usage

Odelay executes tasks after a specified [scala.concurrent.duration.FiniteDuration][fd].
This differs from the behavior the default execution of [scala.concurrent.Futures][fut], which are executed at some non-deterministic time.
`scala.concurrent.Futures` are useful primatives for deferring tasks that may take a non trival amount of time to execute.
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

A delayed operation requires a `scala.concurrent.duration.FiniteDuration` and some arbitrary block of code to execute.

### Timers

In order for the example above to compile, a `odelay.Timer` needs to be in, implicit scope.

`odelay.Timers` implement an interface for making the delay possible.
Implementations for `odelay.Timers` are defined for a number of environments and platforms.

#### JdkTimer

The default is a jdk backed Timer.

To make the example above compile. Import the default `Timer`.

```scala
import scala.concurrent.duration._
import odelay.Default.timer

odelay.Delay(2.seconds) {
  println("executed")
}
```

To be more flexible, if you already have a [ScheduledExecutorService][ses], you may define your own jdk timer with resources you've already allocated.

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
modules which are backed by a netty [HashedWhileTimer][hwt].

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
defines an `odelay.Timer` in terms of twitter util's own timer interface, `com.twitter.util.Timer`. Since twitter's timers are abstractions over various implementations, you will probably have a predefined instance of one which you can adapt to with an `odelay.twitter.Timer`

```scala
import scala.concurrent.duration._
implicit val timer = new odelay.twitter.Timer(myTwitterTimer)
odelay.Delay(2.seconds) {
  println("executed")
}
```

### releasing resources

`odelay.Timers` use thread resources to do work. In order for a jvm to be shutdown cleanly, these resources need to be released. A typical application should only use _one_ instance of an `odelay.Timer`. When an application terminates it should ensure that the `stop()` method of that `odelay.Timer` is invoked.


### Periodic delays

Odelay also provides an interface for use cases where you may wish to execute a task after a perodic delay. You can do so with the `odelay.Delay#every` interface which takes 3 argments, a `scala.concurrent.duration.FiniteDuration` representing the periodic delay, an optional `scala.concurrent.duration.FiniteDuration` representing the initial delay (the default is no delay), and a block of code to execute periodically.

```scala
import odelay.Default.timer

odelay.Delay.every(2.seconds)() {
  println("executed")
}
```

### Timeouts

Like `scala.concurrent.Futures` which provide a interface for _reacting_ to change, odelay delays return an `odelay.Timeout` value which
can be used to react to changes as well.


Doug Tangren (softprops) 2014

[fd]: http://www.scala-lang.org/api/current/index.html#scala.concurrent.duration.FiniteDuration
[fut]: http://www.scala-lang.org/api/current/index.html#scala.concurrent.Future
[ses]: http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ScheduledExecutorService.html
[netty]: http://netty.io/
[hwt]: http://netty.io/4.0/api/io/netty/util/HashedWheelTimer.html
[tu]: http://twitter.github.io/util/
