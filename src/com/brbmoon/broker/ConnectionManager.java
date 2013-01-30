package com.brbmoon.broker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager implements iMessageHandler, Runnable {
	private ConcurrentHashMap<String, SocketChannel> _ConnectedNodes = new ConcurrentHashMap<String, SocketChannel>();
	private ConcurrentHashMap<String, String> _ConnectedNodesPort = new ConcurrentHashMap<String, String>();
	private Selector _Selector;
	private ServerSocketChannel _SSC;
	private ByteBuffer _Message;
	private Thread _OwnThread;
	private int _NodeNumber=1;
	
	public ConnectionManager(){
	  if(init(9099)){
	    System.out.println("Connection Manager - Online");
	  }
	}
	
	@Override
  public void processMessage(Message pMessage) {
    switch(pMessage.Message){
    case "SendMessage":
      sendMessage(pMessage.Data[0],pMessage.Data[1]);
      break;
    case "SendPort":
      if(pMessage.Data[1]!=""){
        sendMessage(pMessage.Data[0],_ConnectedNodesPort.get(pMessage.Data[1]));
      } else {
        sendMessage(pMessage.Data[0],"0");
      }
      break;
    }
    
  }
	
	@SuppressWarnings("static-access")
  @Override
	public void run(){
	  while(!_OwnThread.interrupted()){
	    try{
	      int num = _Selector.select();
	      if(num > 0){
	        Set<SelectionKey> selectedKeys = _Selector.selectedKeys();
	        Iterator<SelectionKey> it = selectedKeys.iterator();
	        
	        while(it.hasNext()){
	          SelectionKey key = it.next();
	          it.remove();
	          
	          if(!key.isValid()) continue;
	          if(key.isAcceptable()){
	            SocketChannel sc = _SSC.accept();
	            sc.configureBlocking(false);
	            sc.register(_Selector, SelectionKey.OP_READ);
	            _ConnectedNodes.put("Node"+_NodeNumber, sc);
	            messageReceived("Node"+_NodeNumber, "Connect");
	            _NodeNumber++;
	          }
	          if(key.isReadable()){
	            SocketChannel sc = (SocketChannel)key.channel();
	            int numRead=0;
	            String data;
	            String from="";
	            for(Entry<String, SocketChannel> k : _ConnectedNodes.entrySet()){
                if(sc.equals(k.getValue())){
                  from = k.getKey();
                  break;
                }
              }
	            try{
	              numRead = sc.read(_Message);
	              _Message.flip();
	              data = new String(_Message.array());
	              data = data.subSequence(0, _Message.limit()).toString();
	              _Message.clear();
	              messageReceived(from,data);
	            }catch (Exception e){
	              key.channel().close();
	              key.cancel();
	              messageReceived(from,"Disconnect");
	            }
	          }
	        }
	      }
	    } catch (Exception e){}
	  }
	}
	
	public boolean init(int pPort){
	  return openConnection(pPort);
	}
	
	public boolean openConnection(int pPort){
	  try{
	    _SSC = ServerSocketChannel.open();
	    _SSC.socket().bind(new InetSocketAddress(pPort));
	    _SSC.configureBlocking(false);
	    _Message = ByteBuffer.allocate(2048);
	    _Selector = Selector.open();
	    _SSC.register(_Selector, SelectionKey.OP_ACCEPT);
	  } catch (Exception e){ e.printStackTrace(); return false; }
	  
	  _OwnThread = new Thread(this);
	  _OwnThread.setName("Broker-ConnectionThread");
	  _OwnThread.start();
	  
	  return true;
	}
	
	public boolean close(){
	  try{
	    for(String k : _ConnectedNodes.keySet()){
	      _ConnectedNodes.get(k).close();
	    }
	    _SSC.close();
	  } catch (Exception e){ e.printStackTrace(); return false; }
	  return true;
	}
	
	private void messageReceived(String pFrom, String pMsg){
	  String[] data = pMsg.split(" ");
	  MessageSystem.AddMessage(new Message("Output", "NewMessage", new String[]{pFrom, "Broker", pMsg}));
	  int dataUsed = 0;
	  switch(data[0]){
	  case "Disconnect":
	    dataUsed = 1;
	    removeClient(pFrom);
	    break;
	  case "Connect":
	    dataUsed = 1;
	    //addClient(pFrom);
	    break;
	  case "Port":
	    dataUsed = 2;
	    _ConnectedNodesPort.put(pFrom, data[1]);
	    System.out.println("Received Port data: "+data[1]);
	    addClient(pFrom);
	    break;
	  case "JobRequest":
	    dataUsed = 6;
	    data[0]=pFrom;
	    MessageSystem.AddMessage(new Message("Task", "NewJob", data));
	    break;
	  case "JobResponse":
	    dataUsed = 6;
	    data[0]=pFrom;
      MessageSystem.AddMessage(new Message("Task", "JobResponse", data));
      break;
	  case "Active":
	    dataUsed = 1;
	    MessageSystem.AddMessage(new Message("Task","ChangeState",new String[]{pFrom,"BUSY"}));
	    break;
	  case "Idle":
	    dataUsed = 1;
	    MessageSystem.AddMessage(new Message("Task","ChangeState",new String[]{pFrom,"IDLE"}));
      MessageSystem.AddMessage(new Message("Task","ProcessTasks"));
      break;
	  case "GetIdleNode":
	    dataUsed = 1;
	    MessageSystem.AddMessage(new Message("Task","GetIdleNode",new String[]{pFrom}));
	    break;
	  default:
	    dataUsed = data.length;
	    sendMessage(pFrom, new String[]{"UnknownMessage", Util.ArrayToString(data)});
	    break;
	  }
	  
	  if(dataUsed < data.length){
      String newMessage = "";
      for(int i=dataUsed; i<data.length; i++){
        newMessage+=data[i]+" ";
      }
      messageReceived(pFrom, newMessage);
    }
	}

  private void addClient(String pName){
    MessageSystem.AddMessage(new Message("Task", "AddNode", new String[]{pName}));
    MessageSystem.AddMessage(new Message("Task", "ProcessTasks"));
    MessageSystem.AddMessage(new Message("Output", "NewNode", new String[]{pName}));
  }
  
  private void removeClient(String pName){
    MessageSystem.AddMessage(new Message("Task", "RemoveNode", new String[]{pName}));
    MessageSystem.AddMessage(new Message("Output", "RemoveNode", new String[]{pName}));
    try {
      _ConnectedNodes.get(pName).close();
      _ConnectedNodes.remove(pName);
      _ConnectedNodesPort.remove(pName);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private void sendMessage(String pName, String[] pMessage){
    sendMessage(pName, Util.ArrayToString(pMessage));
  }
  
  private void sendMessage(String pName, String pMessage){
    ByteBuffer message = ByteBuffer.wrap(pMessage.getBytes());
    try {
      _ConnectedNodes.get(pName).write(message);
      MessageSystem.AddMessage(new Message("Output", "NewMessage", new String[]{"Broker", pName, pMessage}));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
	
	
	
}
