import java.rmi.registry.Registry
import java.rmi.registry.LocateRegistry

/* We run our own registry so that CLASSPATH is always correct. */
object GComRegistry {

  /* Option parsing. */
  import org.clapper.argot.ArgotParser
  import org.clapper.argot.ArgotUsageException
  import org.clapper.argot.ArgotConverters._

  val parser = new ArgotParser(
    "gcom-nameserver",
    preUsage=Some("gcom-nameserver 0.1")
  )

  /* Option values. */
  val portopt = parser.option[Int](List("p", "port"), "n", "port number")
  lazy val port = portopt.value.getOrElse(31337)


  def main(args : Array[String]) {
    if (System.getSecurityManager == null) {
      System.setSecurityManager(new SecurityManager)
    }

    LocateRegistry.createRegistry(port)
    println("Running GCom registry on port " + port.toString)
    readLine()
  }
}
