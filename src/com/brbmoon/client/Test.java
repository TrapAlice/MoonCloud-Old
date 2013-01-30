package com.brbmoon.client;

public class Test {
  public String Add5(String data){
    for(int i = 0; i<=30000;i++){
      //System.out.print("a");
    }
    System.out.println("");
    int x;
    x = Integer.parseInt(data);
    x+=5;
    
    return Integer.toString(x);
  }
  
  public String SquareAdd(String data){

    int x;
    x = Integer.parseInt(data);
    Client c = new Client();
    c.GetIdleNode("127.0.0.1", 9099);
    int jobID;
    jobID = c.GetJobID(2, "Map");
    if(jobID==-1){
      return "0";
    }
    c.ProcessTask(jobID,"com.brbmoon.node.Test", "Square", data);
    c.ProcessTask(jobID,"com.brbmoon.node.Test", "Square", data);
    String result = c.GetResult();
    String[] newdata = result.split(" ");
    int finalresult = Integer.parseInt(newdata[3]) + Integer.parseInt(newdata[4]);
    System.out.println(result);
    
    return Integer.toString(finalresult);
  }
  
  public String Square(String data){
    int x;
    x = Integer.parseInt(data);
    x*=x;
    return Integer.toString(x);
  }
}
