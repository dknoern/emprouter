package com.seattleweb.emprouter;

import java.io.IOException;

public class ClassDKeepAliveThread extends Thread{

	long delay;
	
	ClassDConnection conn;
	public ClassDKeepAliveThread(ClassDConnection conn, long delay){
		this.conn = conn;
		this.delay = delay;
	}
	public void run() {
		while (true) {
			try {
				Thread.sleep(delay);
				conn.keepAlive();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void setConnection(ClassDConnection conn2) {
		conn = conn2;
		
	}
}
