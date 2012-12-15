package com.gcom.test
import java.rmi.registry.Registry
import java.rmi.registry.LocateRegistry
import org.scalatest._
import java.rmi.server.UnicastRemoteObject
import com.gcom.IntegerSet
import com.gcom.OrderingModule
import scala.collection.mutable.Queue
import com.gcom.OrderingModule
import com.gcom.Message
import com.gcom.IM
import com.gcom.DataMessage
import com.gcom.DM
import com.gcom.Reciever
import com.gcom.Host
import com.gcom.BasicCom

class testComModule extends FunSuite with BeforeAndAfter with BeforeAndAfterAll  {
  
  val message = "A long Data String";

  val PORTNUMBER = 33333;
  
  //Create Registry;
  var registry = LocateRegistry.createRegistry(PORTNUMBER);
  //Create The objects;
  var errors = 0;
  val callback = {h: Host => errors += 1}
  var modules = List(
      new BasicCom[String](Host("localhost",PORTNUMBER,"1"), callback),
      new BasicCom[String](Host("localhost",PORTNUMBER,"2"), callback),
      new BasicCom[String](Host("localhost",PORTNUMBER,"3"), callback)
      );
      
  val nodes = modules map {_.me}
  //Bind the objects to registry;
  modules.foreach({
    module => registry.rebind(
        module.me.name,
        UnicastRemoteObject.exportObject(module.asInstanceOf[Reciever[String]],0));
  });
  
  before {
	  //Reset the nodes
  }
  
  override def afterAll {
    UnicastRemoteObject.unexportObject(registry, true);
  }
  
  test("Sent messages should be recieved by all recievers"){
    modules.head.send(nodes,DM(message));
    
    val result = modules map({ m =>
      m.get().dm.d;
    })
    assert(result === List(message,message,message) );
  }
	
  test("Afterwards no module should have any messages in queue"){
    val result = modules map({ m =>
      m.poll();
    })
    assert(result === List(false,false,false))
  }
}


