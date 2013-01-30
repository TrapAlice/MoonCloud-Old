package com.brbmoon.broker;

public class Task {
	public int ID;
	public String Worker;
	public String Data;
	public String Result;
	public boolean Complete = false;
	
	public Task(int pID, String pData){
		ID = pID;
		Data = pData;
	}
	
}
