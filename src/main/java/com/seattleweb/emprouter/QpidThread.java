package com.seattleweb.emprouter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class QpidThread extends Thread{
	
	
	public void run(){
		
		String[] args = new String[2];
		args[0] = "-c";
		args[1] = "config/config.xml";
		org.apache.qpid.server.Main.main(args);
		System.out.println("qpid initialized");
	}
	
	
	
	public static void createConfigFile(List<AppConfig> apps) throws IOException{

		File file = new File("tmp","virtualhosts.xml");
		file.getParentFile().mkdirs();
	    FileWriter outFile = new FileWriter(file);
	    PrintWriter out = new PrintWriter(outFile);

	    out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
	    //out.println("<!-- Auto-generated by EMPRouter, DO NOT EDIT -->");
	    out.println("<virtualhosts>");
	    out.println("  <default>test</default>");
	    out.println("  <virtualhost>");
	    out.println("    <name>test</name>");
	    out.println("    <test>");
	    out.println("      <store>");
	    out.println("        <class>org.apache.qpid.server.store.MemoryMessageStore</class>");
	    out.println("      </store>");
	    out.println("      <exchanges>");
	    out.println("        <exchange>");
	    out.println("          <type>direct</type>");
	    out.println("          <name>FromApp.Ex</name>");
	    out.println("          <durable>true</durable>");
	    out.println("        </exchange>");
	    out.println("        <exchange>");
	    out.println("          <type>direct</type>");
	    out.println("          <name>ToApp.Ex</name>");
	    out.println("          <durable>true</durable>");
	    out.println("        </exchange>");
	    out.println("      </exchanges>");
	    out.println("      <queues>");
	    out.println("        <minimumAlertRepeatGap>30000</minimumAlertRepeatGap>");
	    out.println("        <maximumMessageCount>50</maximumMessageCount>");
	    
	    out.println("        <queue>");
	    out.println("          <name>FromAppQueue</name>");
	    out.println("          <queue>");
	    out.println("            <exchange>FromApp.Ex</exchange>");
	    out.println("            <maximumQueueDepth>4235264</maximumQueueDepth>");
	    out.println("            <maximumMessageSize>2117632</maximumMessageSize>");
	    out.println("            <maximumMessageAge>600000</maximumMessageAge>");
	    out.println("          </queue>");
	    out.println("        </queue>");	    	    

		for (AppConfig appConfig : apps) {
			if (appConfig.getQueueName() != null
					&& appConfig.getQueueName().length() > 0) {
				out.println("        <queue>");
				out.println("          <name>" + appConfig.getQueueName() + "</name>");
				out.println("          <queue>");
				out.println("            <exchange>ToApp.Ex</exchange>");
				out.println("            <maximumQueueDepth>4235264</maximumQueueDepth>");
				out.println("            <maximumMessageSize>2117632</maximumMessageSize>");
				out.println("            <maximumMessageAge>600000</maximumMessageAge>");
				out.println("          </queue>");
				out.println("        </queue>");
			}
		}

	    out.println("      </queues>");
	    out.println("    </test>");
	    out.println("  </virtualhost>");
	    out.println("</virtualhosts>");

	    out.close();
		
		
		
	
	}
	

}
