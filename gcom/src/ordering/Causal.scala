package gcom.ordering;

import gcom.common.NodeID
import gcom.common.Message
import gcom.common.CausalMessage
import gcom.communication.Communication


class Causal(c: Communication, callbck: Message => Unit, me: NodeID) extends Ordering(c, callbck){
  
  private var vectorClock = Vector[Int]();
  private var holdBacks = List[(Message, CausalMessage)]();
  private var indexOf = Map[NodeID,Int]();
  private var clock = 0;
  private val myAddr = me;
  
  def receiveMessage(msg: Message){
    msg match {
      case Message(_, cm : CausalMessage, _) => handle_message(msg, cm);
    }
  }
  
  private def handle_message(newM: Message, newCm: CausalMessage){
    holdBacks = (newM, newCm) :: holdBacks
    var changed = true;
    
    while(changed){
      changed = false;
      holdBacks.foreach{ case (m, cm) =>
        val index = indexOf(m.senders.head);
        if(cm.clock(index) == vectorClock(index) + 1 &&
            earlierByOthers(index,vectorClock,cm.clock)){
        	println("deliver", m);
            callbck(m);
            holdBacks = holdBacks.filter(_ != m)
            vectorClock = vectorClock updated (index,vectorClock(index) + 1);
            changed = true
          }
          
      };
    }
  }

  private def earlierByOthers(
      index: Int, 
      myClock: Vector[Int], 
      otherClock: Vector[Int]): Boolean = {
        val a = for(i <- 0 until myClock.length
          if(i != index && otherClock(i) > myClock(i))) yield{
            false;
          }
     //If the above predicate was true for zero elements, it was earlier than others
     a.length == 0 
  }
  
  /**
   * @param nodes is a sequence of tuples containing nodes and their current clock
   */
  def updateView(nodes: Seq[(NodeID,Int)]) = {
      //Only remember messages from nodes in view
      holdBacks = holdBacks.filter({case (m,cm) => nodes.contains(m.senders.head) })
      
      //Assume nodes index are their index in the view
      indexOf = Map(nodes.map(_._1) zipWithIndex: _*)
      
      // Move 
      vectorClock = Vector( ( nodes.map(_._2) ) : _*);
    }
  
  def createOrdering(): CausalMessage = {
    val index = indexOf(me);
    clock = clock + 1; 
    CausalMessage(vectorClock updated (index, clock ));
  }
}

object Causal {
  def create(t : Communication, callbck : Message => Unit, thisNode: NodeID) : Causal = {
    val ord = new Causal(t, callbck,thisNode)
    t.setOnReceive(ord.receiveMessage)
    return ord
  }
}
