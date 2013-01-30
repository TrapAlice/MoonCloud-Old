package com.brbmoon.node;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.lang.reflect.Method;

public class Node implements Runnable {
  private SocketChannel _Broker;
  private SocketChannel _Client;
  private ByteBuffer _Message;
  private String _Status;
  private Thread _OwnThread;
  private Selector _Selector;
  private ServerSocketChannel _SSC;
  private int _OwnPort;
  private Process _CurrentProcess;
  
  public Node(String pIP, int pTargetPort, int pOwnPort){
    _OwnPort = pOwnPort;
    init(pIP,pTargetPort);
    _Status = "Idle";
  }
  
  public void run(){
    while(true){
      try{
        if(_CurrentProcess!=null){
          if(_CurrentProcess.isComplete()){
            String result = _CurrentProcess.result();
            if(result!="")sendMessage("JobResponse Successful "+result);
            _CurrentProcess = null;
            _Status = "Idle";
          }
        }
        
        checkForMessages();
      }catch (Exception e){ e.printStackTrace(); _OwnThread.interrupt(); }
    }
  }
  
  private void checkForMessages(){
    try{
      int num = _Selector.select(20);
      if(num > 0){
        Set<SelectionKey> selectedKeys = _Selector.selectedKeys();
        Iterator<SelectionKey> it = selectedKeys.iterator();
        
        while(it.hasNext()){
          SelectionKey key = it.next();
          it.remove();
          if(!key.isValid()){continue;}
          if(key.isAcceptable() && _Client==null){
            SocketChannel sc = _SSC.accept();
            sc.configureBlocking(false);
            sc.register(_Selector, SelectionKey.OP_READ);
            System.out.println("New Client has connected");
            _Client = sc;
          }
          if(key.isReadable()){
            SocketChannel sc = (SocketChannel)key.channel();
            int numRead = 0;
            String data = "";
            try{
              numRead = sc.read(_Message);
              _Message.flip();
              data = new String(_Message.array());
              data = data.subSequence(0, _Message.limit()).toString();
              _Message.clear();
              messageReceived(data);
            } catch (Exception e){ 
              System.out.println("Client Disconnected");
              key.channel().close();
              key.cancel();
            }
            if(numRead == -1){
              key.channel().close();
              key.cancel();
            }
          }
        }
      }
    }catch (Exception e){ e.printStackTrace(); _OwnThread.interrupt(); }  
    
  }
  
  public boolean init(String pIP, int pPort){
    try{
      _SSC = ServerSocketChannel.open();
      _SSC.socket().bind(new InetSocketAddress(_OwnPort));
      _SSC.configureBlocking(false);
      
      _Message = ByteBuffer.allocate(1024);
      _Selector = Selector.open();
      _SSC.register(_Selector, SelectionKey.OP_ACCEPT);
      System.out.println("Selecter - Online");
      _Broker = SocketChannel.open(new InetSocketAddress(pIP,pPort));
      _Broker.configureBlocking(false);
      _Broker.register(_Selector, SelectionKey.OP_READ);
      System.out.println("Connected to Broker on :"+pIP+":"+pPort);
      sendMessage("Port "+_OwnPort);
    } catch (Exception e){ e.printStackTrace(); }
    _OwnThread = new Thread(this);
    _OwnThread.setName("MoonCloud-Node");
    _OwnThread.start();
    return true;
  }
  
  private void messageReceived(String pMessage){
    System.out.println("Message Received: \t"+pMessage);
    String[] data = pMessage.split(" ");
    int dataUsed = 0;
    switch(data[0]){
    case "JobRequest":
      dataUsed=1;
      if(_Status.equals("Idle")){
        sendMessage("JobResponse Yes");
      } else {
        sendMessage("JobResponse No");
      }
      break;
    case "JobStart":
      dataUsed=3;
      sendMessage("JobRequest Start "+data[1]+" "+data[2]);
      break;
    case "JobData":
      dataUsed=5;
      sendMessage("JobRequest "+data[1]+" "+data[2]+" "+data[3]+" "+data[4]);
      break;
    case "Active":
      dataUsed=1;
      if(_CurrentProcess!=null){
        sendMessage("JobResponse Interrupted");
        _CurrentProcess = null;
      }
      sendMessage("Active");
      break;
    case "Idle":
      dataUsed=1;
      sendMessage("Idle");
      break;
    case "JobResponse":
      
      if(data[1].equals("Data")){
        dataUsed = 5;
        processTask(data);
      } else if (data[1].equals("Complete")){
        dataUsed = data.length;
        toClient(pMessage);
        _Client=null;
      }
      break;
    case "JobID":
      dataUsed=2;
      toClient(data[1]);
      break;
    default:
      System.out.println("UNKNOWN MESSAGE");
    }
    if(dataUsed < data.length){
      String newMessage = "";
      for(int i=dataUsed; i<data.length; i++){
        newMessage+=data[i]+" ";
      }
      messageReceived(newMessage);
    }
  }
  
  private void processTask(String[] pData){
    _Status = "Busy";
    String result = "";
    _CurrentProcess = new Process(pData[2],pData[3],pData[4]);
    _CurrentProcess.start();
    /*result = _CurrentProcess.result();
    if(result!="")sendMessage("JobResponse Successful "+result);
    _CurrentProcess = null;
    _Status = "Idle";*/
  }
  
  private synchronized void sendMessage(String pMessage){
    pMessage+=" ";
    try{
      if(_Broker!=null){
        _Message.put(pMessage.getBytes(), 0, pMessage.length());
        _Message.flip();
        _Broker.write(_Message);
        _Message.clear();
        System.out.println("Message Sent: \t\t"+pMessage);
      }
    } catch (Exception e){ e.printStackTrace(); }
  }
  
  private synchronized void toClient(String pMessage){
    pMessage+=" ";
    try{
      if(_Client!=null){
        _Message.put(pMessage.getBytes(), 0, pMessage.length());
        _Message.flip();
        _Client.write(_Message);
        _Message.clear();
        System.out.println("Message Sent: "+pMessage);
      }
    } catch (Exception e){ e.printStackTrace(); }
  }
}
