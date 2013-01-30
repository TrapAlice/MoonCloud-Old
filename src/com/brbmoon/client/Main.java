package com.brbmoon.client;

public class Main {
	public static void main(String[] args){
	  int Amount = 1;
	  long t = System.currentTimeMillis();
	  Test t1 = new Test();
	  for(int x=0; x<Amount; x++){
	    t1.Add5("5");
	  }
	  long timeTaken = System.currentTimeMillis() - t;
	  System.out.println("\nTime Taken: "+timeTaken);
	  t = System.currentTimeMillis();
		Client c = new Client();
		//System.out.println("Creating Client\nConnecting..");
		//c.ConnectToNode("127.0.0.1", Integer.parseInt(args[0]));
		c.GetIdleNode("127.0.0.1", 9099);
		//System.out.println("Connection Complete");
		int jobID;
		//System.out.println("Getting Job ID");
		jobID = c.GetJobID(Amount, "Map");
		//System.out.println("Job ID: "+jobID);
		if(jobID==-1){
		  System.out.println("FAILED");
		  return;
		}
		//System.out.println("Requesting Job");
		//for(int x=0; x<Amount; x++){
		  c.ProcessTask(jobID, "com.brbmoon.node.Test", "SquareAdd", "5");
    //}
		//System.out.println("Awaiting Results");
		System.out.println("Results: "+c.GetResult());
		//System.out.println("END");
		c.CloseConnection();
		timeTaken = System.currentTimeMillis() - t;
    System.out.println("Time Taken: "+timeTaken);
	}
}
