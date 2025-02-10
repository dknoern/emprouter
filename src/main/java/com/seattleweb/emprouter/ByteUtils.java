package com.seattleweb.emprouter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class ByteUtils {

	public static long byteArrayToLong(byte[] b, int istart, int iend) {
		long l = 0;

		for (int i = istart; i < iend; i++) {
			l <<= 8;
			l ^= (long) b[i] & 0xFF;
		}

		return l;
	}

	/**
	 * 
	 * Classname / Method Name : CalculationHelper/byteArrayToLong()
	 * 
	 * @param b
	 * @return
	 * @Description : Method is used to convert byte array to Long
	 */
	public static long byteArrayToLong(final byte[] b) {
		long value = 0;
		for (int i = 0; i < b.length; i++) {
			value = (value << 8) + (b[i] & 0xff);
		}
		return value;
	}

	/**
	 * 
	 * Classname / Method Name : CalculationHelper/longToByteArray()
	 * 
	 * @param value
	 * @param size
	 * @return
	 * @Description : Method is used to convert long to byte array
	 */
	public static byte[] longToByteArray(final long value, final int size) {

		final byte[] b = new byte[size];
		for (int i = 0; i < size; i++) {
			final int offset = (b.length - 1 - i) * 8;
			b[i] = (byte) ((value >> offset) & 0xFF);
		}
		return b;
	}

	/**
	 * 
	 * Classname / Method Name : CalculationHelper/byteArrayToInt()
	 * 
	 * @param b
	 * @return
	 * @Description : Method is used to convert byte array to int
	 */
	public static int byteArrayToInt(final byte[] b) {
		int val = 0;
		for (int i = b.length - 1, j = 0; i >= 0; i--, j++) {
			val += (b[i] & 0xff) << (8 * j);
		}
		return val;
	}

	/**
	 * 
	 * Classname / Method Name : CalculationHelper/byteArrayToBinary()
	 * 
	 * @param b
	 * @return
	 * @Description : Method is used to convert byte array to binary
	 */
	public static String byteArrayToBinary(final byte[] b) {
		final BigInteger bi = new BigInteger(b);
		final String tempEmpMessageDisplay = bi.toString(2);
		return tempEmpMessageDisplay;
	}

	/**
	 * 
	 * Classname / Method Name : CalculationHelper/byteArrayDisplay()
	 * 
	 * @param b
	 * @return
	 * @Description : Method is used to display byte array conversion to binary
	 */
	public static String byteArrayDisplay(final byte[] b) {
		final StringBuilder strEmpMessageDisplay = new StringBuilder();
		for (int i = 0; i < b.length; i++) {
			strEmpMessageDisplay.append((char) b[i]);
		}
		return strEmpMessageDisplay.toString();
	}

	public static String getSubSequence(String inputString,
			final int offsetToSubsequence) {

		String formattedMessage = null;

		CharSequence inputStringSubSequence = null;
		CharSequence byteArraySubSequence = null;
		// Replace \r and \n in xml so that they dont get interprated in logging

		inputString = inputString.replaceAll("\r", "");
		inputString = inputString.replaceAll("\n", "");

		final String byteArrayString = byteArrayToBinary(inputString.getBytes());
		for (int index = 0; index < inputString.length();) {

			if (index + offsetToSubsequence > inputString.length()) {
				inputStringSubSequence = inputString.subSequence(index,
						inputString.length());
				if (index + offsetToSubsequence > byteArrayString.length()) {
					byteArraySubSequence = byteArrayString.subSequence(index,
							byteArrayString.length());
				}
				formattedMessage = getFormattedMessage(formattedMessage,
						inputStringSubSequence, byteArraySubSequence);
			} else {
				inputStringSubSequence = inputString.subSequence(index, index
						+ offsetToSubsequence);
				byteArraySubSequence = byteArrayString.subSequence(index, index
						+ offsetToSubsequence);
				formattedMessage = getFormattedMessage(formattedMessage,
						inputStringSubSequence, byteArraySubSequence);
			}
			index = index + offsetToSubsequence;
		}

		return formattedMessage;
	}

	/**
	 * 
	 * Classname / Method Name : CalculationHelper/getFormattedMessage()
	 * 
	 * @param formattedMessage
	 * @param inputStringSubSequence
	 * @param byteArraySubSequence
	 * @return
	 * @Description : Method is used to format the message
	 */
	private static String getFormattedMessage(String formattedMessage,
			final CharSequence inputStringSubSequence,
			final CharSequence byteArraySubSequence) {
		if (formattedMessage != null) {
			formattedMessage = "\t\t\t" + formattedMessage + "\t\t\t"
					+ byteArraySubSequence + "\n" + inputStringSubSequence;
		} else {
			formattedMessage = "\t\t\t" + formattedMessage + "\t\t\t"
					+ byteArraySubSequence + "\n" + inputStringSubSequence;
		}
		return formattedMessage;
	}

	/**
	 * 
	 * Classname / Method Name : CalculationHelper/toHexDump()
	 * 
	 * @param bytes
	 * @return
	 * @Description : Method is used to Create HEX dump of input byte array
	 */
	public static String toHexDump(byte[] bytes) {

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < bytes.length; i = i + 16) {
			String hex = Integer.toString(i, 16).toUpperCase();
			if (hex.length() < 4)
				sb.append("0");
			if (hex.length() < 3)
				sb.append("0");
			if (hex.length() < 2)
				sb.append("0");
			sb.append(hex + "\t");

			int j;
			for (j = i; j < i + 16 && j < bytes.length; j++) {

				hex = Integer.toString(bytes[j] & 0xff, 16).toUpperCase();
				if (hex.length() == 1) {
					hex = "0" + hex;
				}
				sb.append(hex + " ");

			}
			// fill out row
			if (j - i < 16) {
				for (int k = 0; k < 16 - (j - i); k++) {
					sb.append("   ");
				}
			}

			sb.append("\t");

			for (j = i; j < i + 16 && j < bytes.length; j++) {

				if (bytes[j] < 32 || bytes[j] > 126) {
					sb.append(".");
				} else {
					sb.append((char) bytes[j]);
				}
			}

			sb.append("\n");
		}

		return sb.toString();
	}

	public static String readNullTerminatedString(byte[] bytes, int i) {
		 int j = i;
		 ByteArrayOutputStream bos = new ByteArrayOutputStream();
		 while(bytes[j]!=0 && j<bytes.length){
			 j++;
		 }
		 bos.write(bytes, i, j-i);
		 
		 return bos.toString();
	}

	public static byte[] getSubSequence(byte[] bytes, int off, int len) {
		
		 ByteArrayOutputStream bos = new ByteArrayOutputStream();
		 bos.write(bytes, off, len);
		 return bos.toByteArray();
	}
	
	public static String getString(byte[] bytes, int off, int len) {
		
		 ByteArrayOutputStream bos = new ByteArrayOutputStream();
		 bos.write(bytes, off, len);
		 return bos.toString();
	}
		
	public static byte[] getBytes (byte value) throws IOException {
		byte[] byteArray = new byte[1];
		byteArray[0] = value;
		return byteArray;
	}
		
	public static byte[] getBytes(int value, int length) throws IOException {
		return getBytes((long)value,length);
	}

	public static byte[] getBytes(long value, int length) throws IOException {

		byte[] b = new byte[length];
		for (int i = 0; i < length; i++) {
			int offset = (b.length - 1 - i) * 8;
			b[i] = (byte) ((value >> offset) & 0xFF);
		}
		return b;
	}
}
