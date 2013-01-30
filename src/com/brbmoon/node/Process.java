package com.brbmoon.node;

import java.lang.reflect.Method;

public class Process extends Thread {
  private String _Class;
  private String _Method;
  private String _Data;
  private String _Result;
  private boolean _Complete = false;
  private boolean _Terminated = false;
  
  Process(String pClass, String pMethod, String pData){
    _Class = pClass;
    _Method = pMethod;
    _Data = pData;
  }
  
  public void run(){
    try{
      Object classInstance = null;
      classInstance = Class.forName(_Class).newInstance();
      Class cl = Class.forName(_Class);
      cl.newInstance();
      Method m = cl.getDeclaredMethod(_Method,String.class);
      _Result = (String)m.invoke(classInstance, _Data);
      _Complete = true;
    } catch(Exception e){e.printStackTrace();}
  }
  
  public String result(){
    return _Result;
  }
  
  public boolean isComplete(){
    return _Complete;
  }
  
  public void terminate(){
    _Terminated = true;
  }
}
