package com.brbmoon.broker;

import java.util.ArrayList;

public class TaskGroup {
	private ArrayList<Task> Tasks = new ArrayList<Task>();
	public String Type;
	public String Client;
	public int Amount;
	public int ID;
	
	TaskGroup(int pID, String pClient, String pType, int pAmount){
		ID = pID;
		Client = pClient;
		Type = pType;
		Amount = pAmount;
	}
	
	public boolean addTask(Task pTask){
		if(Tasks.size()<Amount){
			Tasks.add(pTask);
			return true;
		}
		return false;
	}
	
	public boolean full(){
		return (Amount==Tasks.size());
	}
	
	public boolean owner(Task pTask){
		for(int i=0; i<Tasks.size(); i++){
			if(Tasks.get(i)==pTask) return true;
		}
		return false;
	}
	
	public boolean complete(){
		for(int i=0; i<Tasks.size(); i++){
			if(!Tasks.get(i).Complete) return false; 
		}
		return true;
	}
	
	public String getResult(){
	  String result = "";
	  for(int i=0; i<Tasks.size(); i++){
	    result+= " "+Tasks.get(i).Result;
	  }
	  
	  return result;
	}
}
