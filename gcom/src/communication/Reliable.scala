package gcom.communication;

import gcom.common._;
import gcom.transport.Transport;
import scala.collection.immutable.IntMap

class Reliable(t : Transport, callbck : Message => Unit, hostCallbck: () => List[NodeID])
                 extends Communication(t, callbck) {
  
  var hostCallback = hostCallbck;
  var localSeq = 0;
  var sequences = Map[NodeID,IntMap[Boolean]]();

  def sendToAll(dsts : List[NodeID], payload: String, ordering: MessageOrdering) : List[NodeID] = {
    var retList = List[NodeID]()
    dsts.foreach { dst =>
      val mid = transport.sendMessage(dst, Message(ReliableMessage(localSeq),ordering,payload))
      localSeq = localSeq + 1
      retList = mid.map({ id => id :: retList}).getOrElse(retList)
    }
    return retList
  }

  def receiveMessage(msg : Message) = { msg match {
   case Message(rm: ReliableMessage,_ , _) => 
    val from = msg.senders.head;
    if(!sequences.contains(from)){
      sequences += (from -> IntMap()) 
    } 
    if(sequences(from).contains(rm.seq)){
      //Message already received, ignore
    }else if(from != t.nodeID){
      val updatedSequence = sequences(from) + (rm.seq -> true)
      sequences += (from -> updatedSequence)
      
      hostCallback().map({ host =>
    	  transport.sendMessage(host, msg)  
      })
      callback(msg)
    }
  
  case Message(um: UnreliableMessage,_ , _) => 
      callback(msg)
  }
  }
}

object Reliable {
  def create(t : Transport, callbck : Message => Unit, hostCallback: () => List[NodeID]) : Reliable = {
    val comm = new Reliable(t, callbck, hostCallback)
    t.setOnReceive(comm.receiveMessage)
    return comm
  }
}

