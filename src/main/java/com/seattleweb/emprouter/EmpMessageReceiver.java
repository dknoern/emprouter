package com.seattleweb.emprouter;


public class EmpMessageReceiver extends MessageReceiver{

	@Override
	public void onMessage(byte[] data) {
		
		System.out.println("message received: \n" + ByteUtils.toHexDump(data));
		EmpMessage empMessage = new EmpMessage(data);
		System.out.println("received message of type " + empMessage.getMessageTypeID() + " bound for "+ empMessage.getDestAddress());
	}
}
