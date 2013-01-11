package gcom.communication;

import gcom.common._;
import gcom.transport.Transport;

abstract class Communication(t : Transport, callbck : Message => Unit) {
  val transport = t
  var callback  = callbck

  /** Send a message to all nodes in the list. Return the IDs of those that were
   *  reachable. */
  def sendToAll(dst : List[NodeID], payload : String, ordering: MessageOrdering) : List[NodeID]

  /** Change the onReceive callback. */
  def setOnReceive(callbck : Message => Unit) : Unit = callback = callbck

}
