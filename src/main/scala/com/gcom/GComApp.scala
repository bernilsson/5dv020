package com.gcom

object GComApp extends App {

  /**
   * IP-address for the Central Authority Server to connect to.
   */
  var centralIP = "";

  if(args.length != 0){
    centralIP = args(0);
  }
  /*Handle the argument, create central server or not.
  //Central server should make sure it has its own registry.
  
   Hmm, different networks can't share registry, how do we make
    sure of this?*/
  
}


