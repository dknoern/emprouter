package com.seattleweb.emprouter;

public class AppConfig {

	public static final int APP_TYPE_AMQP = 1;
	public static final int APP_TYPE_CLASSD = 2;
	
	private String empAddress;
	public String getEmpAddress() {
		return empAddress;
	}
	public void setEmpAddress(String empAddress) {
		this.empAddress = empAddress;
	}
	public int getAppType() {
		return appType;
	}
	public void setAppType(int appType) {
		this.appType = appType;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	private int appType;
	private int port;
	/**
	 * 
	 */
	private String queueName;
	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
}
