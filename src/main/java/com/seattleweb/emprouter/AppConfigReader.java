package com.seattleweb.emprouter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class AppConfigReader {

	public AppConfigReader(){
	}
	
	public List<AppConfig> read(){
		
		
		ArrayList<AppConfig> list = new ArrayList<AppConfig>();

		String filename = "config/emprouter.cfg";

	    try {
	        BufferedReader input =  new BufferedReader(new FileReader(filename));
	        try {
	          String line = null; 

	          while (( line = input.readLine()) != null){
	        	  if(!line.startsWith("#")){
	        		  AppConfig appConfig = new AppConfig();
	        		  StringTokenizer st = new StringTokenizer(line,",");
	        		  	
	        		  appConfig.setEmpAddress(st.nextToken());
	        		  
	        		  String appTypeString = st.nextToken();
	        		  if("AMQP".equals(appTypeString)){
	        			  appConfig.setAppType(AppConfig.APP_TYPE_AMQP);
        				  String queueName = st.nextToken();
        				  appConfig.setQueueName(queueName);

	        		  }else{
	        			  appConfig.setAppType(AppConfig.APP_TYPE_CLASSD);
        				  int port = Integer.parseInt(st.nextToken());
        				  appConfig.setPort(port);

	        		  }
	        		  
	      			  	      			
	      			  list.add(appConfig);
	        	  }	           
	          }
	        }
	        finally {
	          input.close();
	        }
	      }catch (FileNotFoundException ex){
		        System.out.println("Config file ["+filename+"] not found");
		        System.exit(1);
	      }catch (IOException ex){
		        ex.printStackTrace();
	      }
	      
	      return list;

	}
	
	
	
}
