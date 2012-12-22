package com.gcom

object NameServer {
  def listGroups() : List[Group] = {
    List[Group]();
  }
  
  def joinGroup[T](g: JoinGroup, onRecv: (T) => Unit) : InternalCommunicator[T] = {
    new BasicCom[T](Node("me",1,"bla"),{p => Unit})
  }
  
  def killGroup(g: Group) : Boolean = {
    false
  }
}