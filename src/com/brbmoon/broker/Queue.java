package com.brbmoon.broker;

import java.util.ArrayList;

public class Queue<O> {
	private ArrayList<O> _Objects = new ArrayList<O>();
	
	public O pull(){
		O object = peek();
		if(object != null) _Objects.remove(0);
		return object;
	}
	
	public void push(O pObject){
		_Objects.add(pObject);
	}
	
	public int size(){
	  return _Objects.size();
	}
	
	public boolean isEmpty(){
		return _Objects.isEmpty();
	}
	
	public O peek(){
		if(isEmpty()) return null;
		return _Objects.get(0);
	}
}
