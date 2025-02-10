package com.seattleweb.emprouter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmpMessageReceiver extends MessageReceiver{
	public static Logger logger = LoggerFactory.getLogger(EmpMessageReceiver.class);

	@Override
	public void onMessage(byte[] data) {
		
		logger.info("message received: \n" + ByteUtils.toHexDump(data));
		EmpMessage empMessage = new EmpMessage(data);
		logger.info("received message of type " + empMessage.getMessageTypeID() + " bound for "+ empMessage.getDestAddress());
	}
}
