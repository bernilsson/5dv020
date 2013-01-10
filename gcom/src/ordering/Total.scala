package gcom.ordering;

import gcom.common.Message
import gcom.communication.Communication
import gcom.transport.Transport
import gcom.common.TotalMessage
import gcom.common.TotalOrderUpdate
import gcom.common.TotalMessage


/**
 * callbck is executed on recieved messages when they are to be delivered
 * nextOrder is run on each createMessage to order the message
 */
class Total(callbck : Message => Unit, nextOrder : () => Int) extends Ordering(callbck) {
  var order = 0;
  var holdBack = List[TotalMessage]();
  def receiveMessage(msg : Message) { msg match{
    case totalMsg: TotalMessage => {
      /*
       * We want to get every consecutive message from holdbacks
       * 
       * By sorting and zipping each message with its number we can 
       * then group the holdback by the messages that are in order 
      */
      val zippedWithIndex = (totalMsg :: holdBack).sortBy(_.order) zipWithIndex
      //GroupBy returns a map Boolean -> Result
      val consecMap = shiftIndex(zippedWithIndex, order).groupBy(tuple => tuple match {
        case (m, mOrder) => m.order == mOrder;
      })
      
      holdBack = consecMap.get(false).getOrElse(List()).map(_._1)
      consecMap.get(true).getOrElse(List()).foreach({ tuple =>
        val deliver = tuple._1.message;
        callbck(deliver)
        order = order + 1
      })
      
      
    }
    case msg: Message => callback(msg)
    } 
  }
  
  private def shiftIndex(list: List[(TotalMessage, Int)], amount: Int) = {
    list.map(a => (a._1,a._2+amount))
  }

  def updateView(order: Int) = this.order = order
  def createMessage(msg: Message) = TotalMessage( nextOrder(), msg )
}

object Total {
  def create(t : Communication, callbck : Message => Unit, nextOrder : () => Int ) : Total = {
    val ord = new Total(callbck, nextOrder)
    t.setOnReceive(ord.receiveMessage)
    return ord
  }
}
