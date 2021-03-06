package gcom.ordering;

import gcom.common._
import gcom.communication.Communication


class FIFO(c: Communication, callbck: Message => Unit) extends Ordering(c, callbck){
  
  private var holdBacks = Map[NodeID,List[(Message, FIFOMessage)]]();
  private var sequences = Map[NodeID,Int]();
  private var curSeq = 0;
  
  def receiveMessage(msg : Message) {
    msg match {
      case Message(_, fm: FIFOMessage, _) => handle_message(msg,fm);
      case m : Message => callback(m)
    }
    Unit
  }
  
  private def handle_message(newm: Message, newfm: FIFOMessage){
    val from = newm.senders.head
    if(holdBacks.get(from).isEmpty){
      holdBacks += (from -> List());
    }
    if(sequences.get(from).isEmpty){
      sequences += (from -> 0);
    }
    if(sequences(from) == newfm.seq){
      sequences += from -> (newfm.seq + 1);
      callback(newm)
      check_queue(from);
    }
    else {
      holdBacks += from-> ((newm,newfm) :: holdBacks(from));  
    }
  }
  
  private def check_queue(host: NodeID){
    var sequence = sequences(host)
    var list = holdBacks(host).sortBy(m => m._2.seq);
    while(list.nonEmpty && sequence == list.head._2.seq){
      callback(list.head._1)
      sequence += 1;
      list = list.tail;
    }
    holdBacks += (host -> list);
    sequences += (host -> sequence);
  }
  
  def createOrdering(): FIFOMessage = {
    val msg = FIFOMessage(curSeq);
    curSeq += 1;
    return msg;
  }
  
  def updateView(nodes: Seq[(NodeID, Int)]) = {
    //Remember messages not yet delivered
    val newHoldBacks = for (n <- nodes) yield {
      if(holdBacks.contains(n._1)) { (n._1, holdBacks(n._1)  ) }
      else                      { (n._1, List() ) }
    };
    holdBacks = Map(newHoldBacks: _*);
    
    sequences = Map(nodes: _*);
  }
}

object FIFO{
  def create(t: Communication, callbck: Message => Unit) : FIFO = {
    val fifo = new FIFO(t, callbck)
    t.setOnReceive(fifo.receiveMessage)
    fifo
  }
}
