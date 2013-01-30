package com.brbmoon.broker;

public class Message {
	public String Receiver;
	public String Message;
	public String[] Data;
	
	public Message(String pReceiver, String pMessage, String[] pData){
		Receiver = pReceiver;
		Message = pMessage;
		Data = pData;
	}
	
	public Message(String pReceiver, String pMessage){
		Receiver = pReceiver;
		Message = pMessage;
	}
	
	public Message(){}
}
