package odelay.testing

import odelay.Timer

class TimerSpecJVM extends TimerSpec {
  def newTimer = odelay.Timer.default
  def timerName = "JVM Timer"

}
