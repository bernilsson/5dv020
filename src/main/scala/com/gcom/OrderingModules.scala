package com.gcom

import scala.collection.mutable.Queue
import scala.collection.mutable.Map

trait OrderingModule[T]{
  def insert(m: IM[T]);
  def get(): T;
  def getAll(): Seq[T];
  def createMessage(d:T): DataMessage[T];
}


abstract class AbstractOrderingModule[T] extends OrderingModule[T]{
 val deliveryQueue = Queue[T]();
 def get(): T = deliveryQueue.dequeue;
 def getAll(): Seq[T] = deliveryQueue.dequeueAll({p => true})
}

class UnorderedQueue[T]() extends AbstractOrderingModule[T]{
  def insert(im: IM[T]){
    deliveryQueue.enqueue(im.dm.d);
  }
  def createMessage(d: T): DataMessage[T] = {
    DM(d);
  }
}

class FIFOQueue[T]() extends AbstractOrderingModule[T]{
  private val holdBacks = Map[Host,List[SeqM[T]]]();
  private val sequences = Map[Host,Int]();
  private var curSeq = 0;
  
  def insert(im: IM[T]){
    im match {
      case IM(m: Message,dm: SeqM[T]) => handle_message(m,dm); 
    }
  }
  private def handle_message(m: Message,dm: SeqM[T]){
    if(holdBacks.get(m.from).isEmpty){
      holdBacks += (m.from -> List[SeqM[T]]());
    }
    if(sequences.get(m.from).isEmpty){
      sequences += (m.from -> 0);
    }
    if(sequences(m.from) == dm.seq){
      sequences += m.from -> (dm.seq + 1);
      deliveryQueue += (dm.d);
      check_queue(m.from);
    }
    else {
      holdBacks += m.from-> (dm :: holdBacks(m.from));  
    }
  }
  
  private def check_queue(host: Host){
    var sequence = sequences(host)
    var list = holdBacks(host).sortBy(m => m.seq);
    while(list.nonEmpty && sequence == list.head.seq){
      deliveryQueue += list.head.d;
      sequence += 1;
      list = list.tail;
    }
    holdBacks += (host -> list);
    sequences += (host -> sequence);
  }
  
  def createMessage(d:T): DataMessage[T] = {
    val mes = SeqM(curSeq,d);
    curSeq += 1;
    return mes;
  }
}

class CausalQueue[T](index: Int) extends AbstractOrderingModule[T]{
  private var vectorClock = Vector[Int]();
  private var holdBacks = List[IM[T]]();
  private var indexOf = Map[Host,Int]();
  
  private var myIndex = index;
  def insert(im: IM[T]){
    im match {
      case IM(m: Message,dm: CausalMessage[T]) => handle_message(im); 
    }
  }
  
  private def handle_message(newIm: IM[T]){
    holdBacks = newIm :: holdBacks
    var changed = true;
    
    while(changed){
      holdBacks.foreach{ im =>
        val index = indexOf(im.m.from);
        val dm = im.dm.asInstanceOf[CausalMessage[T]];
        changed = if(dm.vector(index) == vectorClock(index) + 1 &&
          earlierByOthers(index,vectorClock,dm.vector)){
          deliveryQueue.enqueue(im.dm.d);
          vectorClock = vectorClock updated (index,vectorClock(index) + 1);
          true
        }
        else false
      };
    }
  }

  private def earlierByOthers(index: Int, myClock: Vector[Int], otherClock: Vector[Int]): Boolean = {
     val a = for(i <- 0 to myClock.length
        if(i != index && otherClock(i) > myClock(i))) yield{
          false;
        }
     a.length == 0
  }
  
  def createMessage(d:T): DataMessage[T] = {
    vectorClock = vectorClock updated (myIndex, vectorClock(myIndex) + 1)
    CausalMessage(vectorClock, d);
    
  }
}

