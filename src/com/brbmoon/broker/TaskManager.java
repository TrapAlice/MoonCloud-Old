package com.brbmoon.broker;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TaskManager implements iMessageHandler {
	
	private ConcurrentHashMap<String,String> _AgentStates = new ConcurrentHashMap<String,String>();
	private ConcurrentHashMap<String,Task> _AgentTasks = new ConcurrentHashMap<String,Task>();
	private Queue<Task> _QueuedTasks = new Queue<Task>();
	private HashMap<Integer, TaskGroup> _Tasks = new HashMap<Integer, TaskGroup>();
	private int _TaskNumber=0;

	public TaskManager(){
	  System.out.println("Task Manager - Online");
	}
	
	@Override
	public void processMessage(Message pMessage) {
		switch(pMessage.Message){
		case "ProcessTasks":
			processTasks();
			break;
		case "AddNode":
			_AgentStates.put(pMessage.Data[0], "IDLE");
			processTasks();
			break;
		case "RemoveNode":
			removeNode(pMessage.Data[0]);
			break;
		case "ChangeState":
			changeAgentState(pMessage.Data[0], pMessage.Data[1]);
			break;
		case "NewJob":
			jobRequest(pMessage.Data[0], pMessage.Data);
			break;
		case "JobResponse":
			jobResponse(pMessage.Data[0], pMessage.Data);
			break;
		case "GetIdleNode":
		  MessageSystem.AddMessage(new Message("Connection","SendPort", new String[]{pMessage.Data[0], searchForIdleAgent()}));
		  break;
		default:
			System.out.println("TASK-> Message Not Understood");
		}
		
	}
	
	private void jobRequest(String pFrom, String[] pData){
		if(pData[1].equals("Start")){
			int amount = Integer.parseInt(pData[2]);
			TaskGroup newTask = new TaskGroup(0, pFrom, pData[3], amount);
			_Tasks.put(_TaskNumber, newTask);
			MessageSystem.AddMessage(new Message("Connection","SendMessage", new String[]{pFrom, "JobID "+Integer.toString(_TaskNumber)}));
			_TaskNumber++;
		} else {
			try{
				TaskGroup temp = _Tasks.get(Integer.parseInt(pData[1]));
				String newData = Util.ArrayToString(Util.ExtractData(pData));
				Task newTask = new Task(temp.ID, newData);
				if(temp.addTask(newTask)){
					_QueuedTasks.push(newTask);
				}
				if(temp.full()){
					processTasks();
				}
				
				MessageSystem.AddMessage(new Message("Output", "NewTask", new String[]{pData[1], pData[2]+"."+pData[3], pFrom}));
			} catch (Exception e){e.printStackTrace();}
		}
	}
	
	private void removeNode(String pFrom){
	  _AgentStates.remove(pFrom);
	  jobInterrupted(pFrom);
	}
	
	private void jobResponse(String pFrom, String[] pData){
		switch(pData[1]){
		case "Yes":
			jobAccepted(pFrom);
			break;
		case "No":
			jobRefused(pFrom);
			break;
		case "Successful":
			jobSuccessful(pFrom, pData);
			break;
		case "Interrupted":
			jobInterrupted(pFrom);
			break;
		}
	}
	
	private void jobAccepted(String pFrom){
		changeAgentState(pFrom, "BUSY");
		Task temp = _AgentTasks.get(pFrom);
		MessageSystem.AddMessage(new Message("Connection", "SendMessage", new String[]{pFrom, "JobResponse Data "+temp.Data}));
	}
	
	private void jobRefused(String pFrom){
		changeAgentState(pFrom, "UNKNOWN");
		Task temp = _AgentTasks.get(pFrom);
		temp.Worker = "";
		_QueuedTasks.push(temp);
		_AgentTasks.remove(pFrom);
		processTasks();
	}
	
	private void jobSuccessful(String pFrom, String[] pData){
		Task temp = _AgentTasks.get(pFrom);
		for(Integer k : _Tasks.keySet()){
			if(_Tasks.get(k).owner(temp)){
				temp.Complete = true;
				temp.Result = Util.ArrayToString(Util.ExtractData(pData));
				_AgentTasks.remove(pFrom);
				if(_Tasks.get(k).complete()){
					// TODO Process results differently depending on the TaskGroup Type
					MessageSystem.AddMessage(new Message("Connection", "SendMessage", new String[]{_Tasks.get(k).Client, "JobResponse Complete "+_Tasks.get(k).getResult()}));
					
				}
				changeAgentState(pFrom,"IDLE");
			}
		}
		processTasks();
	}
	
	private void jobInterrupted(String pFrom){
		Task temp = _AgentTasks.get(pFrom);
		if(temp!=null){
		  System.out.println("TASK INTERRUPTED");
		  _AgentTasks.remove(pFrom);
		  _QueuedTasks.push(temp);
	    processTasks();
		}
	}
	
	private void changeAgentState(String pName, String pState){
		_AgentStates.replace(pName, pState);
		MessageSystem.AddMessage(new Message("Output", "NodeChange", new String[]{pName, pState}));
	}
	
	private void processTasks(){
		Task temp;
		Queue<Task> spareTasks = new Queue<Task>();
		while((temp= _QueuedTasks.pull())!=null){
			String selectedAgent;
			selectedAgent = searchForIdleAgent();
			if(selectedAgent != ""){
				_AgentTasks.put(selectedAgent, temp);
				temp.Worker = selectedAgent;
				MessageSystem.AddMessage(new Message("Connection","SendMessage",new String[]{selectedAgent,"JobRequest"}));
				MessageSystem.AddMessage(new Message("Output","TaskChange",new String[]{Integer.toString(temp.ID),selectedAgent}));
			} else {
				spareTasks.push(temp);
			}
		}
		while((temp = spareTasks.pull()) != null){
		  _QueuedTasks.push(temp);
		}
	}
	
	private String searchForIdleAgent(){
		for(String k : _AgentStates.keySet()){
			if(_AgentStates.get(k).equals("IDLE")){
			  changeAgentState(k, "UNKNOWN");
				return k;
			}
		}
		return "";
	}

}
