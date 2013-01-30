package com.brbmoon.utils;

public class Util {
	public static String ArrayToString(String[] pArray){
		StringBuffer result = new StringBuffer();
        try{
            if(pArray.length==1){ return pArray[0]; }
            for(int x=0; x<pArray.length;x++){
                result.append(pArray[x]);
                result.append(" ");
            }
        } catch(Exception e){e.printStackTrace();}
        
        return result.toString();
	}
	
	public static String[] ExtractData(String[] pData){
		String[] temp={};
        try{
            temp = new String[(pData.length-2)];
            for(int x=2;x<pData.length;x++){
                temp[x-2]=pData[x];
            }
        } catch(Exception e){e.printStackTrace();}
        return temp;
	}
	
}
