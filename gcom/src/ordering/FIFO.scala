package gcom.ordering;

import gcom.common._
import gcom.communication.Communication


class FIFO(callbck: Message => Unit) extends Ordering(callbck){
  
  private var holdBacks = Map[NodeID,List[FIFOMessage]]();
  private var sequences = Map[NodeID,Int]();
  private var curSeq = 0;
  
  def receiveMessage(msg : Message) {
    msg match {
      case m : FIFOMessage => handle_message(m);
      case m : Message => callback(m)
    }
    Unit
  }
  
  private def handle_message(m: FIFOMessage){
    val from = m.senders.head
    if(holdBacks.get(from).isEmpty){
      holdBacks += (from -> List[FIFOMessage]());
    }
    if(sequences.get(from).isEmpty){
      sequences += (from -> 0);
    }
    if(sequences(from) == m.seq){
      sequences += from -> (m.seq + 1);
      callback(m.msg)
      check_queue(from);
    }
    else {
      holdBacks += from-> (m :: holdBacks(from));  
    }
  }
  
  private def check_queue(host: NodeID){
    var sequence = sequences(host)
    var list = holdBacks(host).sortBy(m => m.seq);
    while(list.nonEmpty && sequence == list.head.seq){
      callback(list.head.msg)
      sequence += 1;
      list = list.tail;
    }
    holdBacks += (host -> list);
    sequences += (host -> sequence);
  }
  
  def createMessage(m: Message): FIFOMessage = {
    val msg = FIFOMessage(curSeq,m);
    curSeq += 1;
    return msg;
  }
  
  def updateView(nodes: Seq[(NodeID, Int)]) = {
    //Remember messages not yet delivered
    val newHoldBacks = for (n <- nodes) yield {
      if(holdBacks.contains(n._1)) { (n._1, holdBacks(n._1)  ) }
      else                      { (n._1, List[FIFOMessage]() ) }
    };
    holdBacks = Map(newHoldBacks: _*);
    
    sequences = Map(nodes: _*);
  }
}

object FIFO{
  def create(t: Communication, callbck: Message => Unit) : FIFO = {
    val fifo = new FIFO(callbck)
    t.setOnReceive(fifo.receiveMessage)
    fifo
  }
}
