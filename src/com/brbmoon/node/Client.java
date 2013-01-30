package com.brbmoon.node;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {
  private SocketChannel _Node;
  private ByteBuffer _Message;
  
  public boolean GetIdleNode(String pIP, int pPort){
    try{
      _Node = SocketChannel.open(new InetSocketAddress(pIP,pPort));
      _Message = ByteBuffer.allocate(1024);
      sendMessage("GetIdleNode");
      int port = Integer.parseInt(readMessage());
      if(port==0){
        System.out.println("No Idle Node Found");
        return false;
      }
      System.out.println("Idle Node found, port: "+port);
      return ConnectToNode("127.0.0.1",port);
      
    }catch(Exception e){e.printStackTrace(); return false;}
  }
  
  public boolean ConnectToNode(String pIP, int pPort){
    try{
      _Node = SocketChannel.open(new InetSocketAddress(pIP,pPort));
      _Message = ByteBuffer.allocate(1024);
      sendMessage("Active");
    } catch (Exception e){e.printStackTrace(); return false;}
    return true;
  }
  
  public int GetJobID(int pAmount, String pType){
    String message = "JobStart "+pAmount+" "+pType;
    sendMessage(message);
    try{
      String[] data = readMessage().split(" ");
      return Integer.parseInt(data[0]);
    } catch (Exception e){
      e.printStackTrace();
      return -1;
    }
  }
  
  public void ProcessTask(int pID, String pClass, String pMethod, String pData){
    String message = "JobData "+pID+" "+pClass+" "+pMethod+" "+pData;
    sendMessage(message);
  }
  
  public String GetResult(){
    return readMessage();
  }
  
  public void CloseConnection(){
    try {
      sendMessage("Idle");
      _Node.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private void sendMessage(String pMessage){
    pMessage+= " ";
    try{
      if(_Node!=null){
        _Message.put(pMessage.getBytes(), 0, pMessage.length());
        _Message.flip();
        _Node.write(_Message);
        _Message.clear();
        System.out.println("Message Send: "+pMessage);
      }
    } catch (Exception e){ e.printStackTrace(); }
  }
  
  private String readMessage(){
    String message="";
    try {
      if(_Node != null){
        _Node.read(_Message);
        
        _Message.flip();
        message = new String(_Message.array());
        message = message.subSequence(0, _Message.limit()).toString();
        _Message.clear();
        System.out.println("Message Read: "+message);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    return message;
  }
}
