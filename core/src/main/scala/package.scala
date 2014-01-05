/** Odelay defines a set of primatives for delaying
 *  the execution of operations.
 * 
 *  This is differs from scala.concurrent.Futures in that
 *  the execution of an operation will not occur until a provided delay, specified
 *  as a scala.concurrent.duration.Duration. The delay of a
 *  task may also may be cancelled. Operations may also be executed after a series
 *  of delays, also represented by scala.concurrent.duration.Durations.
 *
 *  These primatives can be used to complement the usage of scala.concurrent.Futures by defining
 *  a deterministic delay for the future operation as well as a way
 *  to cancel the future operation.
 *
 *  An odelay.Timeout is the result of a delay, defines a future which may be used to trigger dependant actions
 *  and delay cancellations.
 */
package object odelay
