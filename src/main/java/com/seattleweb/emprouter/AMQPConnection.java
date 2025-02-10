package com.seattleweb.emprouter;

import java.util.HashMap;
import java.util.Properties;

import jakarta.jms.BytesMessage;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Destination;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

public class AMQPConnection extends Thread {
	HashMap<String, String> amqpMap;
	HashMap<String, ClassDConnection> classDMap;

	MessageConsumer messageConsumer;
	MessageProducer messageProducer;
	MessageReceiver messageReceiver;
	Session session;

	public void setMessageReceiver(MessageReceiver messageReceiver) {
		this.messageReceiver = messageReceiver;
	}

	public AMQPConnection(String receiveQueue, String sendQueue) {

		Properties prop = new Properties();

		prop.setProperty("java.naming.factory.initial",
				"org.apache.qpid.jms.jndi.JmsInitialContextFactory");

		prop.setProperty("connectionfactory.qpidConnectionfactory",
				"amqp://localhost:5672");

		if (receiveQueue != null) {
			prop.setProperty("queue.receiver", receiveQueue);
		}

		if (sendQueue != null) {
			prop.setProperty("queue.sender", sendQueue);
		}
		
		try {
			Context context = new InitialContext(prop);

			ConnectionFactory connectionFactory = (ConnectionFactory) context
					.lookup("qpidConnectionfactory");
			Connection connection = connectionFactory.createConnection("admin","admin");

			connection.start();

			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			if (receiveQueue != null) {
				Destination destination = (Destination) context
						.lookup("receiver");
				
				messageConsumer = session.createConsumer(destination);
				start();
			}

			if (sendQueue != null) {

				Destination destination = (Destination) context
						.lookup("sender");

				messageProducer = session.createProducer(destination);
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void run() {

		while (true) {
			dequeue();
		}
	}

	public void dequeue() {
		try {
			BytesMessage message = (BytesMessage) messageConsumer.receive();

			byte[] messageInBytes = new byte[(int) ((BytesMessage) message)
					.getBodyLength()];
			((BytesMessage) message).readBytes(messageInBytes);

			if (messageReceiver != null) {
				messageReceiver.onMessage(messageInBytes);
			}

		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	public void send(byte[] message) {
		try {

			BytesMessage bytesMessage = session.createBytesMessage();
			bytesMessage.writeBytes(message);

			messageProducer.send(bytesMessage);
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

}