package odelay.netty

import io.netty.util.concurrent.DefaultEventExecutorGroup

class NettyGroupTimerSpec extends odelay.testing.TimerSpec {
  def newTimer = Default.groupTimer(new DefaultEventExecutorGroup(1))
  def timerName = "NettyGroupTimer"
}
