package odelay.netty

import io.netty.util.concurrent.DefaultEventExecutorGroup

class NettyGroupTimerSpec extends odelay.testing.TimerSpec {
  def newTimer: odelay.Timer = NettyTimer.groupTimer(new DefaultEventExecutorGroup(1))
  def timerName = "NettyGroupTimer"
}
