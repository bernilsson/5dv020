package gcom.ordering;

import gcom.common.Message;
import gcom.communication.Communication;
import gcom.transport.Transport;
import gcom.common.NoOrderingData

class NonOrdered(callbck : Message => Unit)
      extends InternalOrdering(callbck) {
  def receiveMessage(msg : Message) {
    callback(msg)
  }
  def createOrdering() = NoOrderingData()
  def setOrderCallback( callback: () => Int ) = { ; }
}
