package gcom.communication;

import gcom.common._;
import gcom.transport.Transport;

class NonReliable(t : Transport, callbck : Message => Unit)
                 extends Communication(t, callbck) {

  def sendToAll(dsts : Set[NodeID],
                payload: String, ordering: OrderingData) : Set[NodeID] = {
    val msg = Message(NoReliabilityData(), ordering, payload)
    var actuallySendTo = dsts
    if(drop){
      actuallySendTo = dsts take 2
    }
    sendWithDelay(actuallySendTo, msg)
    dsts.filter({dst => transport.pingNode(dst)})
  }

  def receiveMessage(msg : Message) = callback(msg)

  def setHostCallback (callbck : () => Set[NodeID]) = { ; }
}

object NonReliable {
  def create(t : Transport, callbck : Message => Unit) : NonReliable = {
    val comm = new NonReliable(t, callbck)
    t.setOnReceive(comm.receiveMessage)
    return comm
  }
}
