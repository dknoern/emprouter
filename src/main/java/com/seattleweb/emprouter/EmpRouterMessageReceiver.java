package com.seattleweb.emprouter;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmpRouterMessageReceiver extends MessageReceiver{

	public static Logger logger = LoggerFactory.getLogger(EmpRouterMessageReceiver.class);

	HashMap<String,ClassDConnection> classDMap;
	HashMap<String,AMQPConnection> amqpMap;
	
	public EmpRouterMessageReceiver(HashMap<String,ClassDConnection> classDMap,HashMap<String,AMQPConnection> amqpMap){
		this.classDMap = classDMap;
		this.amqpMap = amqpMap;
		
	}
	
	@Override
	public void onMessage(byte[] data) {
		
		logger.info("message received: \n" + ByteUtils.toHexDump(data));
		
		EmpMessage empMessage = new EmpMessage(data);
		
		String destAddress = empMessage.getDestAddress();
		
		logger.info("received message of type " + empMessage.getMessageTypeID() + " bound for "+ destAddress);
		
		ClassDConnection conn = classDMap.get(destAddress);
		
		if(conn!=null){
			try {
				logger.info("found classD connection");
				conn.send(data);
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}else{
			logger.info("looking for AMQP connection");
			AMQPConnection amqpConnection = amqpMap.get(destAddress);
			if( amqpConnection !=null){
				logger.info("sending AMQP message to AMQP client at " + destAddress);
				amqpConnection.send(data);
			}else{
				logger.error("can't find connection for address " + destAddress);
			}
		}
	}
}
