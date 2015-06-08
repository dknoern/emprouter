package com.seattleweb.emprouter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import org.apache.log4j.Logger;

public class ClassDConnection extends Thread {

	public static Logger logger = Logger.getLogger("ClassDConnection");

	public static int MESSAGE_TYPE_DATA = 1;
	public static int MESSAGE_TYPE_ACK = 2;
	public static int MESSAGE_TYPE_NACK = 3;
	public static int MESSAGE_TYPE_KEEP_ALIVE = 4;

	String host = null;
	int port;
	
	private long commId = 1;
	OutputStream os = null;
	InputStream is = null;
	
	private MessageReceiver messageReceiver;
	ServerSocket serverSocket = null;
	Socket socket = null;
	
	public ClassDConnection(String host, int port) throws IOException {
		
		this.host = host;
		this.port = port;
		
		
		if(host== null){
			
			serverSocket = new ServerSocket(port);
		
		}else{
			socket = new Socket(host, port);
			is = socket.getInputStream();
			os = socket.getOutputStream();
		}
		
		start();	
	}

	public ClassDConnection(int port) throws IOException {
		this(null,port);
	}

	public void setMessageReceiver(MessageReceiver messageReceiver){
		this.messageReceiver = messageReceiver;
	}
	public void send(byte[] message) throws IOException {
		send(MESSAGE_TYPE_DATA, message);
	}

	public void acknowledge(byte[] ackCommId) throws IOException {
		send(MESSAGE_TYPE_ACK, ackCommId);
	}
	
	public void negativeAcknowledge() throws IOException{
		send(MESSAGE_TYPE_NACK,null);
	}
		
	public void keepAlive() throws IOException{
		send(MESSAGE_TYPE_KEEP_ALIVE,null);
	}

	public synchronized void send(int messageType, byte[] message)
			throws IOException {
		

		try{
		os.write(2);
		os.write(2); // protocol version
		getBytes(os, commId++, 4);
		os.write(messageType); // data
		os.write(2); // message version
		long messageLength = 0;
		if (message != null)
			messageLength = message.length;
		getBytes(os, messageLength, 4);
		if (message != null)
			os.write(message);
		os.write(3);
		}catch(SocketException e){
			logger.info("class D connection failure: "+ e.getMessage());
			
			if(messageType==MESSAGE_TYPE_KEEP_ALIVE){
				socket = new Socket(host, port);
				is = socket.getInputStream();
				os = socket.getOutputStream();
			}
			
		}
	}

	@Override
	public void run() {
		
		while (true) {
			
			if(host==null){

				try {
					socket = serverSocket.accept();
					
					logger.info("connection on port " + port + " accepted");
					
					is = socket.getInputStream();
					os = socket.getOutputStream();
					
				} catch (IOException e) {
					logger.info("Accept failed: " + port);
				}				
			}
						
			try {

				int c = 0;

				while (c != -1) {
			
					c = is.read();

					if (c == 2) {
						// STX

						int protocolVersion = is.read();
						byte[] commId = new byte[4];
						is.read(commId);
						int messageType = is.read();
						int messageVersion = is.read();
						byte[] dataLengthBytes = new byte[4];
						is.read(dataLengthBytes);
						long dataLength = getLong(dataLengthBytes);

						if (dataLength > 0) {
							byte[] data = new byte[(int) dataLength]; // TODO,
																		// fix
																		// sloppy
																		// cast
							is.read(data);

							if(messageReceiver!=null && messageType==MESSAGE_TYPE_DATA)messageReceiver.onMessage(data);
						}
						int etx = is.read();

						if (messageType == MESSAGE_TYPE_DATA) {
							this.acknowledge(commId);
						}else if(messageType == MESSAGE_TYPE_ACK) {
							//System.out.println("ACK received");
						}else if(messageType == MESSAGE_TYPE_KEEP_ALIVE) {
							//System.out.println("Keep alive received");
							this.acknowledge(commId);
						}

					}
				}

			} catch (SocketException e) {
				logger.info("connection on port " + port + " closed: "+ e.getMessage());
			} catch (IOException e) {
				logger.info("connection on port " + port + " closed: "+ e.getMessage());
			}
			
			if(host!=null){
				stop();
			}
		}

	}

	public void close() throws IOException {
		is.close();
		os.close();
		socket.close();
		
	}

	
	private static void getBytes(OutputStream bos, long value,
			int length) throws IOException {
		byte[] b = new byte[length];
		for (int i = 0; i < length; i++) {
			int offset = (b.length - 1 - i) * 8;
			b[i] = (byte) ((value >> offset) & 0xFF);
		}
		bos.write(b);
	}
	
	
	private static long getLong(final byte[] b) {
		long value = 0;
		for (int i = 0; i < b.length; i++) {
			value = (value << 8) + (b[i] & 0xff);
		}
		return value;
	}
}
