package com.brbmoon.broker;

public class Output implements iMessageHandler {

  public Output(){
    System.out.println("Output - Online");
  }
  
  @Override
  public void processMessage(Message pMessage) {
    switch(pMessage.Message){
    case "NewNode":
      System.out.println("New Node: "+pMessage.Data[0]);
      break;
    case "RemoveNode":
      System.out.println("Node Removed: "+pMessage.Data[0]);
      break;
    case "NewMessage":
      System.out.println("New Message: "+pMessage.Data[0]+" -> "+pMessage.Data[1]+" -> "+pMessage.Data[2]);
      break;
    }
  }

}
