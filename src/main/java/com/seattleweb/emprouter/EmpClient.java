package com.seattleweb.emprouter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.log4j.BasicConfigurator;



public class EmpClient {

	public static void main(String args[]) throws IOException {

		
		BasicConfigurator.configure();
		
		if (args.length != 2)
			usage();

		String sourceAddress = args[0];

		int port = 0;
		String queue = null;

		try {
			port = Integer.parseInt(args[1]);

		} catch (NumberFormatException e) {
			queue = args[1];
		}

		if (port == 0 && queue == null)
			usage();

		ClassDConnection classDConnection = null;
		AMQPConnection amqpConnection = null;

		EmpMessageReceiver messageReceiver = new EmpMessageReceiver();

		if (port > 0) {
			classDConnection = new ClassDConnection("localhost", port);
			classDConnection.setMessageReceiver(messageReceiver);
		} else if (queue != null) {
			amqpConnection = new AMQPConnection(queue,"FromApp.Ex/FromAppQueue");
			amqpConnection.setMessageReceiver(messageReceiver);
		}

		System.out
				.println("Enter EMP address to send a test message to (type 'quit' to exit): ");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		String destAddress = ""; // Line read from standard in

		while (!(destAddress.equals("quit"))) {
			try {
				destAddress = in.readLine();
				
				
				EmpMessage empMessage = new EmpMessage();
				
				empMessage.setSourceAddress(sourceAddress);
				empMessage.setDestAddress(destAddress);		
				empMessage.setMessageNumber(131072);
				empMessage.setMessageTime(new Date());
				empMessage.setMessageBody("Hello, EMP".getBytes());
				
				
				if (port > 0) {
					classDConnection.send(empMessage.toByteArray());
				} else if (queue != null) {
					amqpConnection.send(empMessage.toByteArray());
				}											
																
				

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	private static void usage() {
		System.out
				.println("usage: empclient SRC_ADDRESS CLASSD_PORT|AMQP_QUEUE");
		System.exit(1);
	}
}
