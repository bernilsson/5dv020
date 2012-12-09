package com.gcom.test
import java.rmi.registry.Registry
import java.rmi.registry.LocateRegistry
import org.scalatest._
import com.gcom.ComModule
import java.rmi.server.UnicastRemoteObject
import com.gcom.Host
import com.gcom.IntegerSet
import com.gcom.GroupCom
import com.gcom.OrderingModule
import scala.collection.mutable.Queue
import com.gcom.OrderingModule
import com.gcom.Message
import com.gcom.BasicCom
import com.gcom.IM
import com.gcom.DataMessage
import com.gcom.DM

class testComModule extends FunSuite with BeforeAndAfter with BeforeAndAfterAll  {
  var nodes = List[Host]();
  val dummy = new GroupCom(){
      def getHosts(groupName: String): List[Host] = {
        return nodes;
      }
      def hostDown(h: Host) = {
        println("Failed");
      }
      var i = 0;
      
  }
  
  val message = "A long Data String";
  
  val dummyO = new Queue[String] with OrderingModule[String]{
      def insert(m: IM[String]){
        this.enqueue(m.dm.d);
      };
      def get(): String = {
        dequeue
      }
      def getAll(): Seq[String] = {
        dequeueAll({p => true});
      }
      def createMessage(d:String): DataMessage[String] = {
        DM(d);
      }
  }
  
  val PORTNUMBER = 33333;
  
  //Create Registry;
  var registry = LocateRegistry.createRegistry(PORTNUMBER);
  //Create The objects;
  var modules = List(
      new BasicCom(Host("localhost",PORTNUMBER,"1"), "group", dummy,dummyO),
      new BasicCom(Host("localhost",PORTNUMBER,"2"), "group", dummy,dummyO),
      new BasicCom(Host("localhost",PORTNUMBER,"3"), "group", dummy,dummyO)
      );
      
  nodes = modules map {_.me}
  //Bind the objects to registry;
  modules.foreach({
    module => registry.rebind(
        module.me.name,
        UnicastRemoteObject.exportObject(module.inboundQ,0));
  });
  
  before {
	  //Reset the nodes
  }
  
  after {
    UnicastRemoteObject.unexportObject(registry, true);
  }
  
  test("Sent messages should be recieved by all recievers"){
    modules.head.send(message);
    
    val result = modules map({ m =>
      println("Waiting for data")
      m.get();
    })
    println(result)
    //Sleep for a while, so messages can arrive
    assert(result === List(message,message,message) );
  }
	
}


