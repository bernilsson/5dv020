package gcom.ordering;

import gcom.common.Message
import gcom.communication.Communication
import gcom.common.NodeID
import gcom.common.OrderingData
import scala.swing.Publisher

abstract class Ordering (
    c: Communication,
    callbck : Message => Unit)
  extends Publisher {

  var callback = callbck
  val communication = c;
  def setOnReceive(callbck : Message => Unit) = callback = callbck
  def receiveMessage(msg : Message) : Unit
  def sendToAll(dst: Set[NodeID], payload: String) : Set[NodeID] = {
    communication.sendToAll(dst, payload,createOrdering())
  }
  protected def createOrdering() : OrderingData

  // For Total and CausalTotal.
  def setOrderCallback( callback: () => Int )
}
