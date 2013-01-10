package gcom.common

import java.net.InetAddress
import java.util.UUID

object Util {
  /** Get the name of the host we're running on. In general, this is not
   *  guaranteed to be the same as the host name visible externally, but we don't
   *  care. */
  def getLocalHostName() : String = InetAddress.getLocalHost().getHostName()

  /** Get a random UUID. */
  def getRandomUUID() : String = UUID.randomUUID().toString()
}
