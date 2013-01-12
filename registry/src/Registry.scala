import java.rmi.registry.Registry
import java.rmi.registry.LocateRegistry

/* We run our own registry so that CLASSPATH is always correct. */
object GComRegistry {

  def main(args : Array[String]) {
    if (System.getSecurityManager == null) {
      System.setSecurityManager(new SecurityManager)
    }

    val port = 31337
    LocateRegistry.createRegistry(port)
    println("Running GCom registry on port " + port.toString)
    readLine()
  }
}
