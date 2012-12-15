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

trait InternalCommunicator[T]{

  type ErrorCallback = (Host) => Unit;
  
  def send(hosts: List[Host], dm: DM[T]): Unit;
  def get(): IM[T] = q.take();
  def poll(): Boolean = !q.isEmpty
  
  private val recievers = Map[Host,Remote]();
  protected val onError: ErrorCallback;
  protected val q = new LinkedBlockingQueue[IM[T]]();
  
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
  protected class AsyncSender
    (g: List[Host],m: IM[T],future: Promise[List[Host]]) extends Runnable{
    def run() = {
        future.setValue(g.map({ h =>
        try{
          hostToRemote(h) recv(m)
          None;
        } catch{
        case e : Exception => {
            onError(h)
            //TODO Log exception
            Some(h);
          }
        }
        }).flatten);
      }
  }
  
  protected def internal_send(g: List[Host], im: IM[T]) = {
    val future = new Promise[List[Host]];
    (new Thread(new AsyncSender(g,im,future))).start();
    future;
  }
  
}


class BasicCom[T]
  (val me:Host, val onError: Host => Unit)
  extends InternalCommunicator[T] with Reciever[T] {
  
  def send(hosts: List[Host],dm: DM[T]){
    internal_send(hosts, IM(SimpleMessage(me),dm));
  }
  def recv(im: IM[T]){
    q.put(im);
  }
}
 

class ReliableCom[T]
    (val me:Host, val onError: Host => Unit, val hostCallback: () => List[Host])
    extends InternalCommunicator[T] with Reciever[T]{
  var rseq = 0;
  val sequences = Map[Host,IntegerSet]();
  def recv(im: IM[T]){
    im match {
      case IM(rm: ReliableMessage, dm: DataMessage[T]) => {

        if(!sequences.contains(rm.from)){
          sequences += (rm.from -> new IntegerSet());
        }
        if(sequences(rm.from).contains(rm.rseq)){
          //Message already received once, ignore
        }else if(rm.from != me){
          internal_send(hostCallback(), im)
          q.put(im);
        }
      }
      }
  }

  def send(hosts: List[Host], dm: DM[T]){
    rseq+=1;
    internal_send(hosts, IM(ReliableMessage(me,rseq),dm));
  }

}
