package gcom.ordering;

import gcom.common.NodeID
import gcom.common.Message
import gcom.common.CausalMessage


class Causal(callbck: Message => Unit, me: NodeID) extends Ordering(callbck){
  
  private var vectorClock = Vector[Int]();
  private var holdBacks = List[CausalMessage]();
  private var indexOf = Map[NodeID,Int]();
  
  private val myAddr = me;
  
  def receiveMessage(msg: Message){
    msg match {
      case cmsg : CausalMessage => handle_message(cmsg);
      case dmsg : Message => callbck(msg)
    }
  }
  
  private def handle_message(newIm: CausalMessage){
    holdBacks = newIm :: holdBacks
    var changed = true;
    
    while(changed){
      holdBacks.foreach{ m =>
        val index = indexOf(m.senders.head);
        changed = if(m.clock(index) == vectorClock(index) + 1 &&
            earlierByOthers(index,vectorClock,m.clock)){
            callbck(m.msg);
            vectorClock = vectorClock updated (index,vectorClock(index) + 1);
            true
          }
          else false
      };
    }
  }

  private def earlierByOthers(
      index: Int, 
      myClock: Vector[Int], 
      otherClock: Vector[Int]): Boolean = {
        val a = for(i <- 0 to myClock.length
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
      holdBacks = holdBacks.filter({holdBack => nodes.contains(holdBack.senders.head) })
      
      //Assume nodes index are their index in the view
      indexOf = Map(nodes.map(_._1) zipWithIndex: _*)
      
      // Move 
      vectorClock = Vector( ( nodes.map(_._2) ) : _*);
    }
  
  def createMessage(msg: Message): CausalMessage = {
    val index = indexOf(me);
    vectorClock = vectorClock updated (index, vectorClock( index + 1 ))
    CausalMessage(vectorClock, msg);
  }
}

