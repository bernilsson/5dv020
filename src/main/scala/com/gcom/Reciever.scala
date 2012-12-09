package com.gcom

import java.rmi.Remote
import java.rmi.RemoteException
import scala.actors.threadpool.BlockingQueue
import scala.actors.threadpool.LinkedBlockingQueue
import scala.collection.mutable.Queue
import scala.collection.mutable.Map


/*Cases for message module*/
abstract class Message{
  val from: Host;
}
case class SimpleMessage(from: Host) extends Message;
case class ReliableMessage(from: Host, rseq: Int) extends Message;

case class IM[T](m:Message,dm:DataMessage[T])

/*
class SeqMessage[T](val seq: Int, d: T) extends DataMessage[T]{
  var from = Host("Pleasing the compiler",1,"Never allow me to be null");
  val data = d;
};*/

case class SeqM[T](seq: Int, d: T) extends DataMessage[T];
case class DM[T](d: T) extends DataMessage[T];
case class CausalMessage[T](vector: Vector[Int], d: T) extends DataMessage[T];

abstract class DataMessage[T]{
  val d: T;
}
/*
Cases for order module*/

/*case class SystemMessage(data: Any) extends DataMessage;
case class UnorderedMessage(data: Any) extends DataMessage;


case class OrderedMessage(order: Int, data: Any) extends DataMessage;
*/
trait Reciever[T] extends Remote {
  @throws(classOf[RemoteException])
  def recv(m : IM[T]) : Unit 
}

class BlockingReciever[T]() extends LinkedBlockingQueue[IM[T]] with Reciever[T]{
  def recv(m: IM[T]): Unit = {
    println("recv" + m)
    this.add(m);
  }
} 

/*

class FIFOQueue() extends Order{
  private var curSeq = 0;
  private val sequences = Map[Host,Int]();
  private val holdbacks = Map[Host,List[(Message,SeqMessage)]]();
  val deliveryQueue = Queue[Any]();
  
  def add(m: Message){
    m.data match {
      case data: SeqMessage => handle_message(m, data); 
    }
  }
  
  private def handle_message(m: Message, data: SeqMessage){
    if(holdbacks.get(m.from).isEmpty){
      holdbacks += (m.from -> List[(Message,SeqMessage)]());
    }
    if(sequences.get(m.from).isEmpty){
      sequences += (m.from -> 0);
    }
    if(sequences(m.from) == data.seq){
      sequences += m.from -> (data.seq + 1);
      deliveryQueue += (data.data);
      check_queue(m.from);
    }
    else {
      holdbacks += m.from-> ((m,data) :: holdbacks(m.from));  
    }
  }
  
  private def check_queue(host: Host){
    var sequence = sequences(host)
    var list = holdbacks(host).sortBy(t => t._2.seq);
    while(list.nonEmpty && sequence == list.head._2.seq){
      deliveryQueue += list.head._2.data;
      sequence += 1;
      list = list.tail;
    }
    holdbacks += (host -> list);
    sequences += (host -> sequence);
  }
  
  def createMessage(data: (Any)): SeqMessage = {
    val mes = SeqMessage(curSeq,data);
    curSeq += 1;
    return mes;
  }
  
}

*/