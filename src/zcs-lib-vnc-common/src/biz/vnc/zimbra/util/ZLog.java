package biz.vnc.zimbra.util;

public class ZLog {
	public static void err(String facility, String message) {
		System.err.println("ERROR ["+facility+"] "+message);
	}

	public static void err(String facility, String message, Exception e) {
		System.err.println("ERROR ["+facility+"] "+message+" "+e.toString());
	}

	public static void warn(String facility, String message) {
		System.err.println("WARNING ["+facility+"] "+message);
	}

	public static void info(String facility, String message) {
		System.err.println("INFO ["+facility+"] "+message);
	}

	public static void debug(String facility, String message) {
		System.err.println("DEBUG ["+facility+"] "+message);
	}
}
