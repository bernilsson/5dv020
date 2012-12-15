package com.gcom

import java.rmi.Remote
import java.rmi.RemoteException
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
