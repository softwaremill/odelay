/** Odelay defines a set of primitives for delaying
 *  the execution of operations.
 * 
 *  This is differs from scala.concurrent.Futures in that
 *  the execution of an operation will not occur until a provided delay, specified
 *  as a scala.concurrent.duration.Duration. The delay of a
 *  task may also may be canceled. Operations may also be executed after a series
 *  of delays, also represented by scala.concurrent.duration.Durations.
 *
 *  These primitives can be used to complement the usage of scala.concurrent.Futures by defining
 *  a deterministic delay for the future operation as well as a way
 *  to cancel the future operation.
 *
 *  An odelay.Delay represents a delayed operation and defines a future method which may be used to trigger dependent actions
 *  and delay cancellations.
 */
package object odelay
