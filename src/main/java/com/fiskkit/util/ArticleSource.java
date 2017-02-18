package com.fiskkit.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ArticleSource {

	private static String COPYRIGHT = "Copyright Fiskkit Corporation 2016 All Rights Reserved";
	public static final String FISKKIT_HOME = System.getProperty("FISKKIT_HOME", ".");

	public static final void main(String[] args) {
		String hex = setSource("hello");
		String reverse = createSource(hex);
		//System.out.println(reverse);

		String test3 = "here is a lenghty string with special characters \t \n public static final void main ";

		// move to JUnit
//		String x = createSource(setSource(test3));
//		System.out.println(x.equals(test3) ? "success" : "fail");

		System.out.println(setSource("swordfish"));
		System.out.println(setSource("swordfish"));
		
		System.out.println(createSource("22-d-13"));
		
		
		
	}

	public static void setSource(String fileName, String propertyName, String value) throws IOException {
		// do this by hand for now
	}

	public static String getSource(String propertyName) throws IOException {

		String fiskkitHome = System.getProperty("FISKKIT_HOME", ".");
		String fileName = fiskkitHome + File.separator + "fiskkit.conf";

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fileName);
			Properties props = new Properties();
			props.load(fis);

			String value = props.getProperty(propertyName, "").trim();
			return createSource(value);
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}

	public static String createSource(String input) {

		if ((input == null) || (input.length() == 0)) {
			return "";
		}

		String[] tokens = input.split("-");
		byte[] bytes = new byte[tokens.length];
		for (int i = 0; i < tokens.length; i++) {

			String token = tokens[i];
			bytes[i] = (byte) Integer.parseInt(token, 16);

			byte xor = (byte) COPYRIGHT.charAt(i % (COPYRIGHT.length()));
			bytes[i] = (byte) (bytes[i] ^ xor);
		}

		return new String(bytes);
	}

	private static String setSource(String input) {

		if ((input == null) || (input.length() == 0)) {
			return "";
		}

		byte[] bytes = input.getBytes();

		StringBuilder sb = new StringBuilder("");
		for (int i = 0; i < bytes.length; i++) {

			byte b = bytes[i];
			byte xor = (byte) COPYRIGHT.charAt(i % (COPYRIGHT.length()));

			int x = (int) (b ^ xor);
			String hex = Integer.toHexString(x);

			if (i > 0) {
				sb.append("-");
			}
			sb.append(hex);

		}
		return sb.toString();
	}
}
