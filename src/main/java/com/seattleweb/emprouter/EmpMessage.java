package com.seattleweb.emprouter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;


public class EmpMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6440485997177638571L;
	private int protocolVersion = 4; // 1 byte
	private int messageTypeID; // 2 bytes
	private int messageVersion = 1; // 1 byte

	private boolean timestampAbsolute = true;
	private boolean bodyEncrypted = false;
	private boolean bodyCompressed = false;
	private int dataIntegrityType = DATA_INTEGRITY_NONE;

	public static final int DATA_INTEGRITY_NONE = 0;
	public static final int DATA_INTEGRITY_CRC = 1;
	public static final int DATA_INTEGRITY_APP_SPECIFIC = 2;
	public static final int DATA_INTEGRITY_RESERVED = 3;

	// private int dataLength; // 3 bytes
	private int messageNumber; // 4 bytes
	private Date messageTime = new Date(); // 4 bytes
	// private int variableHeaderSize; // 1 byte size (in bits) of next 4 fields
	// combined
	private int timeToLive; // 2 bytes
	private int routingQOS; // 2 bytes
	private String sourceAddress; // 64 bytes max
	private String destAddress; // 64 bytes max
	private byte[] messageBody;

	// private byte[] dataIntegrity; // 4 bytes

	public EmpMessage() {
	}

	public EmpMessage(byte[] bytes) {

		int protocolVersion = (int) ByteUtils.byteArrayToLong(bytes, 0, 1);
		int messageTypeID = (int) ByteUtils.byteArrayToLong(bytes, 1, 3);
		int messageVersion = (int) ByteUtils.byteArrayToLong(bytes, 3, 4);
		byte flags = bytes[4];
		long messageBodyLength = ByteUtils.byteArrayToLong(bytes, 5, 8);
		long messageTime = ByteUtils.byteArrayToLong(bytes, 12, 16);
		int variableHeaderSize = (int) ByteUtils.byteArrayToLong(bytes, 15, 17);
		String sourceAddress = ByteUtils.readNullTerminatedString(bytes, 21);
		int i = 21 + sourceAddress.length() + 1;
		String destAddress = ByteUtils.readNullTerminatedString(bytes, i);
		i = i + destAddress.length() + 1;
		byte[] messageBody = ByteUtils.getSubSequence(bytes, i,
				(int) messageBodyLength);

		setProtocolVersion(protocolVersion);
		setMessageTypeID(messageTypeID);
		setMessageNumber((int) ByteUtils.byteArrayToLong(bytes, 8, 12));
		setMessageTime(new Date(messageTime * 1000));
		setTimeToLive((int) ByteUtils.byteArrayToLong(bytes, 17, 19));
		setRoutingQOS((int) ByteUtils.byteArrayToLong(bytes, 19, 21));
		setDestAddress(destAddress);
		setSourceAddress(sourceAddress);
		setMessageBody(messageBody);

	}

	public byte[] toByteArray() throws IOException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		bos.write(ByteUtils.getBytes(getProtocolVersion(), 1));
		bos.write(ByteUtils.getBytes(getMessageTypeID(), 2));
		bos.write(ByteUtils.getBytes(getMessageVersion(), 1));
		bos.write(ByteUtils.getBytes(getFlags(), 1));
		bos.write(ByteUtils.getBytes(getMessageBody().length, 3));
		bos.write(ByteUtils.getBytes(getMessageNumber(), 4));

		bos.write(ByteUtils.getBytes(getMessageTime().getTime() / 1000, 4));

		// int variableHeaderSize = (6 + message.getSourceAddress().length() +
		// message
		// .getDestAddress().length()) * 8;
		int variableHeaderSize = 6 + getSourceAddress().length()
				+ getDestAddress().length();

		// 2 bytes timeToLive + 2 bytes routingQOS + 1 byte each to
		// terminate addresses
		bos.write(ByteUtils.getBytes(variableHeaderSize, 1)); // 1 byte, size in
		// bits of next 4
		// fields

		bos.write(ByteUtils.getBytes(getTimeToLive(), 2));
		bos.write(ByteUtils.getBytes(getRoutingQOS(), 2));
		bos.write(getSourceAddress().getBytes());
		byte nullByte = 0;
		bos.write((nullByte));
		bos.write(getDestAddress().getBytes());
		bos.write(nullByte);

		// body
		bos.write(messageBody);

		if (dataIntegrityType == DATA_INTEGRITY_CRC) {
			byte[] headerAndBody = bos.toByteArray();

			java.util.zip.CRC32 x = new java.util.zip.CRC32();
			x.update(headerAndBody);
			long crc = x.getValue();
			bos.write(ByteUtils.getBytes(crc, 4));
		} else {
			bos.write(ByteUtils.getBytes(0L, 4));

		}

		return bos.toByteArray();
	}

	public void setProtocolVersion(int protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	public void setMessageTypeID(int messageTypeID) {
		this.messageTypeID = messageTypeID;
	}

	public void setMessageVersion(int messageVersion) {
		this.messageVersion = messageVersion;
	}

	public void setMessageNumber(int messageNumber) {
		this.messageNumber = messageNumber;
	}

	public void setMessageTime(Date messageTime) {
		this.messageTime = messageTime;
	}

	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}

	public void setRoutingQOS(int routingQOS) {
		this.routingQOS = routingQOS;
	}

	public void setSourceAddress(String sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

	public void setDestAddress(String destAddress) {
		this.destAddress = destAddress;
	}

	public void setMessageBody(byte[] messageBody) {
		this.messageBody = messageBody;
	}

	public int getProtocolVersion() {
		return protocolVersion;
	}

	public int getMessageTypeID() {
		return messageTypeID;
	}

	public int getMessageVersion() {
		return messageVersion;
	}

	public int getMessageNumber() {
		return messageNumber;
	}

	public Date getMessageTime() {
		return messageTime;
	}

	public int getTimeToLive() {
		return timeToLive;
	}

	public int getRoutingQOS() {
		return routingQOS;
	}

	public String getSourceAddress() {
		return sourceAddress;
	}

	public String getDestAddress() {
		return destAddress;
	}

	public byte[] getMessageBody() {
		return messageBody;
	}

	/**
	 * 
	 * 
	 * @param isGMTTime
	 * @param dataIntegrityEnum
	 * @param isEncrypted
	 * @param isDataCompressed
	 * @Description : Set all flag bits.
	 * 
	 *              1. Bit 0: Timestamp format a. 0 = relative time (elapsed
	 *              time since last message creation) b. 1 = absolute time (UTC
	 *              time according to specifications in section 3.2.2.2 Message
	 *              Timestamp) 2. Bit 1: Encryption a. 0 = body is not encrypted
	 *              b. 1 = body is encrypted 3. Bit 2: Compression a. 0 = body
	 *              is not compressed b. 1 = body is compressed 4. Bits 3-4:
	 *              Data integrity a. 0 = no data integrity support b. 1 = CRC
	 *              calculated per specifications in section 3.6. c. 2 =
	 *              Application specific data integrity value used d. 3 =
	 *              reserved 5. Bits 5-7: Reserved
	 */
	public byte getFlags() {
		short flagValue = 0;
		if (timestampAbsolute) {
			flagValue++;
		}

		if (bodyEncrypted) {
			flagValue += 2;
		}
		if (bodyCompressed) {
			flagValue += 4;
		}

		flagValue += (dataIntegrityType == DATA_INTEGRITY_NONE ? 0 : 0);
		flagValue += (dataIntegrityType == DATA_INTEGRITY_CRC ? 8 : 0);
		flagValue += (dataIntegrityType == DATA_INTEGRITY_APP_SPECIFIC ? 16 : 0);
		flagValue += (dataIntegrityType == DATA_INTEGRITY_RESERVED ? 24 : 0);

		return (byte) flagValue;
	}

}

// 7 6 5 4 3 2 1 0
// 0 0 0 0 0 0 0 0 0
// 0 0 0 0 0 0 0 1 1
// 0 0 0 0 0 0 1 0 2
// 0 0 0 0 0 0 1 1 3
// 0 0 0 0 0 1 0 0 4
// 0 0 0 0 0 1 0 1 5
// 0 0 0 0 0 1 1 0 6
// 0 0 0 0 0 1 1 1 7
// 0 0 0 0 1 0 0 0 8
// 0 0 0 0 1 0 0 0 8
// 0 0 0 1 0 0 0 0 16

//
//

