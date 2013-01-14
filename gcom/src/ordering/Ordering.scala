package gcom.ordering;

import gcom.common.Message
import gcom.communication.Communication
import gcom.common.NodeID
import gcom.common.OrderingData
import scala.swing.Publisher

class Ordering(
    c: Communication,
    first: InternalOrdering,
    nodes: Set[NodeID],
    initialView: Int) extends Publisher {
  var views = Map[Int, InternalOrdering]()
  var view = initialView
  val communication = c;
  
  //Avoid loop
  deafTo(this)
  //Forward from all views
  reactions += {
    case x => publish(x)
  }
  views += view -> first
  
  def updateView(nodes: Set[NodeID], newView: Int){
    val newOrder = views(view).updateView(nodes, newView)
    listenTo(newOrder)
    views += newView -> newOrder  
  }
  def receiveMessage(msg : Message) : Unit = {
    views(msg.ordering.view).receiveMessage(msg)
  }
  def setOnReceive(callbck : Message => Unit) = {
    views.map (_._2.setOnReceive(callbck))
  } 
  def sendToAll(dst: Set[NodeID], payload: String) : Set[NodeID] = {
    communication.sendToAll(dst, payload,views(view).createOrdering())
  }
  def setOrderCallback( callback: () => Int ) = {
    views.map (_._2.setOrderCallback(callback))
  }

}

abstract class InternalOrdering (
    callbck : Message => Unit)
  extends Publisher {
  var callback = callbck
  def setOnReceive(callbck : Message => Unit) = callback = callbck
  def receiveMessage(msg : Message) : Unit

  def createOrdering() : OrderingData

  def updateView(nodes: Set[NodeID], newView: Int): InternalOrdering
  // For Total and CausalTotal.
  def setOrderCallback( callback: () => Int )
}


object Ordering {
  def create(t : Communication, first: InternalOrdering, callbck : Message => Unit) : Ordering = {
    val ord = new Ordering(t, first, Set(), 0)
    t.setOnReceive(ord.receiveMessage)
    return ord
  }
}

