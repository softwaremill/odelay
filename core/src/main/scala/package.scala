/** Deffered defines a set of primatives for deferring
 *  the execution of some task.
 * 
 *  This is differs from scala.concurrent.Futures in that
 *  the execution of a task it defined in terms of delay defined
 *  as a scala.concurrent.duration.Duration. The defferal of a
 *  task may also may be cancelled.
 *
 *  These primatives can be used to complement the usage of scala.concurrent.Futures by defineing
 *  a deterministic delay for the future operation as well as a way
 *  to cancel the future operation.
 */
package object deferred
