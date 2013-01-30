package com.brbmoon.broker;

public class Main {
	public static void main(String[] args){
	  System.out.println("Starting up MoonCloud Broker");
		MessageSystem.AddHandler("Connection", new ConnectionManager());
		MessageSystem.AddHandler("Task", new TaskManager());
		MessageSystem.AddHandler("Output",new Output());
		
		new MessageSystem();
	}
}
