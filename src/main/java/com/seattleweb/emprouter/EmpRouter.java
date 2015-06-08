package com.seattleweb.emprouter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

public class EmpRouter {

	public static Logger logger = Logger.getLogger("EMPRouter");
	public EmpRouter() throws IOException {

		AppConfigReader appConfigReader = new AppConfigReader();

		List<AppConfig> list = appConfigReader.read();
		
		QpidThread.createConfigFile(list);
		
		HashMap<String,ClassDConnection> classDMap = new HashMap<String,ClassDConnection>();
		HashMap<String, AMQPConnection> amqpMap = new HashMap<String,AMQPConnection>();

		System.setProperty("QPID_HOME", ".");
		System.setProperty("QPID_WORK", "tmp");
		
		System.setProperty("conf", "config");
		logger.info("starting emprouter 0.1");
		QpidThread qpidThread = new QpidThread();
		//qpidThread.start();
		qpidThread.run();
		//logger.info("qpid server started");
		logger.info("qpid server started");
		
		MessageReceiver receiver = new EmpRouterMessageReceiver(classDMap,amqpMap);

		

		for (AppConfig appConfig : list) {

			String appTypeString = "AMQP";
			if(appConfig.getAppType()==AppConfig.APP_TYPE_CLASSD){
				appTypeString = "CLASSD";
			}
			logger.info("Initialzing application of type " + appTypeString + " for EMP address " + appConfig.getEmpAddress());
			if(appConfig.getAppType() == AppConfig.APP_TYPE_CLASSD){

					try {

						ClassDConnection conn = new ClassDConnection(appConfig.getPort());

						conn.setMessageReceiver(receiver);
						
						classDMap.put(appConfig.getEmpAddress(), conn);
						
						logger.info("connection setup done");
						
					} catch (IOException e) {
						logger.info("Accept failed: " + appConfig.getPort());
					}

			}else{
				AMQPConnection amqpConnection = new AMQPConnection(null,appConfig.getQueueName());
				amqpMap.put(appConfig.getEmpAddress(), amqpConnection);
			}
		}

		AMQPConnection amqpConnection = new AMQPConnection("FromApp.Ex/FromAppQueue",null);
		amqpConnection.setMessageReceiver(receiver);
		
		while(true){
			try {
				Thread.sleep(5000L);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) {
		try {
			EmpRouter agLight = new EmpRouter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("unable to initialize emprouter",e);
		}
	}

}
