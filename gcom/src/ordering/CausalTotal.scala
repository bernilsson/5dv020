package gcom.ordering;

import gcom.communication.Communication
import gcom.common.Message
import gcom.common.NodeID
import gcom.common.MessageOrdering

class CausalTotal(c: Communication, callbck : Message => Unit, me: NodeID , nextOrder : () => Int) extends Ordering (c: Communication, callbck : Message => Unit) {
  def receiveMessage(msg : Message) : Unit
  def sendToAll(dst: List[NodeID],payload: String) : Unit = {
    communicator.sendToAll(dst, payload,createOrdering())
  }
  protected def createOrdering() : MessageOrdering
}