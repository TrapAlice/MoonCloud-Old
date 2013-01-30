package com.brbmoon.broker;

import java.util.HashMap;

public class MessageSystem implements Runnable {
	private static Queue<Message> _QueuedMessages = new Queue<Message>();
	private static HashMap<String,iMessageHandler> _MessagePorts = new HashMap<String,iMessageHandler>();
	private Thread _OwnThread;
	
	MessageSystem(){
	  _OwnThread = new Thread(this);
	  _OwnThread.setName("MessageSystem");
	  _OwnThread.start();
	  System.out.println("Message System - Online");
	}
	
	public void run(){
	  while(!_OwnThread.interrupted()){
	    try{
	      ProcessMessages();
	      _OwnThread.sleep(5);
	    } catch (Exception e){ e.printStackTrace(); }
	  }
	}
	
	public static void AddHandler(String pID, iMessageHandler pHandler){
		_MessagePorts.put(pID, pHandler);
	}
	
	public static void AddMessage(Message pMsg){
		_QueuedMessages.push(pMsg);
	}
	
	public static void ProcessMessages(){
		Message temp = _QueuedMessages.pull();
		if(temp != null){
			for(String key : _MessagePorts.keySet()){
				if(temp.Receiver.equals(key)){
					_MessagePorts.get(key).processMessage(temp);
				}
			}
		}
	}
}
