package com.seattleweb.emprouter;

import java.io.IOException;
import java.util.HashMap;


public class EmpRouterMessageReceiver extends MessageReceiver{

	HashMap<String,ClassDConnection> classDMap;
	HashMap<String,AMQPConnection> amqpMap;
	
	
	public EmpRouterMessageReceiver(HashMap<String,ClassDConnection> classDMap,HashMap<String,AMQPConnection> amqpMap){
		this.classDMap = classDMap;
		this.amqpMap = amqpMap;
		
	}
	
	
	@Override
	public void onMessage(byte[] data) {
		
		System.out.println("message received: \n" + ByteUtils.toHexDump(data));
		
		
		EmpMessage empMessage = new EmpMessage(data);
		
		String destAddress = empMessage.getDestAddress();
		
		System.out.println("received message of type " + empMessage.getMessageTypeID() + " bound for "+ destAddress);
		
		ClassDConnection conn = classDMap.get(destAddress);
		
		if(conn!=null){
			try {
				conn.send(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			AMQPConnection amqpConnection = amqpMap.get(destAddress);
			if( amqpConnection !=null){
				System.out.println("sending AMQP message to AMQP client at " + destAddress);
				amqpConnection.send(data);
			}else{
				System.out.println("can't find connection for address " + destAddress);
			}
		}

		
	}

}
