package io.pivotal.cf.tester.util;

import java.util.Properties;

import org.joda.time.format.DateTimeFormatter;

import io.pivotal.cf.tester.service.AmqpTestMessageConsumer;

public final class Util {
	
	private Util() {}

	public static final DateTimeFormatter DTF = 
	         org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"); 

	
	public static String getAppProperty(Properties props, String propName, String defaultValue) {
		
		String propVal = System.getProperty(propName);
		if( propVal == null ) {
			propVal = props.getProperty(propName, defaultValue);
		}
		
		return propVal;
	}
	
	public static int getAppPropertyInt(Properties props, String propName, String defaultValue) {
		String propVal = getAppProperty(props, propName, defaultValue);
		return Integer.parseInt(propVal);
	}
	
	public static boolean getAppPropertyBool(Properties props, String propName, String defaultValue) {
		String propVal = getAppProperty(props, propName, defaultValue);
		return Boolean.parseBoolean(propVal);
	}

	public static void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} 
		catch (InterruptedException e) {
			AmqpTestMessageConsumer.log.warn("Interrupted", e);
			throw new RuntimeException(e);
		}
	}
	
}
