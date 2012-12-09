package com.gcom

import java.rmi.registry.Registry
import java.rmi.registry.LocateRegistry
import java.rmi.Remote
import java.net.Inet4Address
import java.net.InetAddress
import scala.compat.Platform
import com.twitter.util.{Future, Promise}
import com.twitter.util.FutureTask
import java.util.concurrent.Callable
import java.util.AbstractQueue
import java.util.concurrent.LinkedBlockingQueue
import scala.collection.mutable.Queue
import scala.collection.mutable.Map


case class Host(host: String, port:Int, name:String);



trait ComModule[T]{
  val recievers = Map[Host,Remote]();
  def send(data: T): Unit;
  def get(): T;
  def poll(): Boolean;
  protected def hostToRemote(h: Host): Reciever[T] = {
      val r = recievers.get(h);
      r match{
      case Some(x : Reciever[T]) => x;
      case None => {
        val reg = LocateRegistry.getRegistry(h.host, h.port); // TODO What if we get nothing
        val x = reg.lookup(h.name).asInstanceOf[Reciever[T]]; // TODO classcast Exception
        recievers += h -> x;
        x;
      }
      }
  }
}

trait OrderingModule[T]{
  def insert(m: IM[T]);
  def get(): T;
  def getAll(): Seq[T];
  def createMessage(d:T): DataMessage[T];
}

trait GroupCom{
  def getHosts(groupName: String): List[Host];
  def hostDown(h: Host);
}

class BasicCom[T]
  (val me:Host, val g: String, gMod: GroupCom, val order: OrderingModule[T])
  extends ComModule[T] with Runnable {
  
  val inboundQ = new BlockingReciever[T]; 
  private val q = new LinkedBlockingQueue[T]();
  private val holdBack = new Queue[T]();
  
  private class AsyncSender
    (g: List[Host],m: IM[T],future: Promise[List[Host]]) extends Runnable{
    def run() = {
        future.setValue(g.map({ h =>
        try{
          hostToRemote(h) recv(m)
          None;
        } catch{
        case e : Exception => {
            println(e)
            gMod.hostDown(h);
            Some(h);
          }
        }
        }).flatten);
      }
  }
  
  private def internal_send(g: List[Host], im: IM[T]) = {
    val future = new Promise[List[Host]];
    (new Thread(new AsyncSender(g,im,future))).start();
    future;
  }

  
  def send(d: T){
    internal_send(gMod.getHosts(g), IM(SimpleMessage(me),order.createMessage(d)));
  }
  
  def get(): T = q.take();
  def poll(): Boolean = !q.isEmpty
  
  def run(){
    while(true){
      val m = inboundQ.take();
      println("in Run" + m);
      order.insert(m);
      order.getAll.foreach({ d =>
        println("in get all" + d);
        q.put(d);
      })
    }
    
  }
  
  (new Thread(this)).start();
  
}

/*DONT EAT ME*/


/* class ComModule(val me: Host, group: String){

  var curSeq = 0;
  var recievers = Map[Host,Remote]();
  var reliableSeqs = Map[Host,Int]();
  val reciever = new BlockingReciever();
  
  val localhost = InetAddress.getLocalHost().getHostAddress();
  
  private class AsyncSender(g: List[Host],m: Message,future: Promise[List[Host]]) extends Runnable{
    def run() = {
        future.setValue(g.map({ h =>
        try{
          
          hostToRemote(h) recv(m)
          None;
        } catch{
        case _ => {
            bMulti(List(me), SystemMessage(("Down",h)));
            Some(h);
          }
        }
        }).flatten);
      }
  }
  
  /*these three need to be non-blocking*/
  private def internal_send(g: List[Host], m: Message) = {
    val future = new Promise[List[Host]];
    (new Thread(new AsyncSender(g,m,future))).start();
    future;
  }


  def rMulti(g: List[Host], m:DataMessage){
    internal_send(g, ReliableMessage(me,group,curSeq,m))
    curSeq += 1;
  }

  def bMulti(g: List[Host], m: DataMessage){
    internal_send(g,SimpleMessage(me,group,m))
  }




  private def hostToRemote(h: Host): Reciever = {
      val r = recievers.get(h);
      r match{
      case Some(x : Reciever) => x;
      case None => {
        val reg = LocateRegistry.getRegistry(h.host, h.port) // TODO What if we get nothing
            val x = reg.lookup(h.name).asInstanceOf[Reciever]; // TODO classcast Exception
        recievers += h -> x;
        x;
      }
      }
  }

}*/