package com.seattleweb.emprouter;

public abstract class MessageReceiver {
	public abstract void onMessage(byte[] data);
}
