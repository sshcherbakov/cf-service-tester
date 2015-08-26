package io.pivotal.cf.tester.util;

import org.joda.time.format.DateTimeFormatter;

public final class Util {
	
	private Util() {}

	public static final DateTimeFormatter DTF = 
	         org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"); 

}
