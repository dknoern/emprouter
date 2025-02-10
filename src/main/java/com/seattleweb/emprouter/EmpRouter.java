package com.seattleweb.emprouter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmpRouter {

	public static Logger logger = LoggerFactory.getLogger(EmpRouter.class);
	public EmpRouter() throws IOException {

		AppConfigReader appConfigReader = new AppConfigReader();

		List<AppConfig> list = appConfigReader.read();

		QpidConfigurer.configure(list);
		
		HashMap<String,ClassDConnection> classDMap = new HashMap<String,ClassDConnection>();
		HashMap<String, AMQPConnection> amqpMap = new HashMap<String,AMQPConnection>();

		logger.info("Starting emprouter 0.1");

		MessageReceiver receiver = new EmpRouterMessageReceiver(classDMap,amqpMap);

		for (AppConfig appConfig : list) {

			String appTypeString = "AMQP";
			if(appConfig.getAppType()==AppConfig.APP_TYPE_CLASSD){
				appTypeString = "CLASSD";
			}
			logger.info("Initialzing connection of type " + appTypeString + " for EMP address " + appConfig.getEmpAddress());
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
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		
		try {
			new EmpRouter();
		} catch (IOException e) {
			logger.error("initialize failure: " + e.getMessage());
			System.exit(1);
		}
	}
}
