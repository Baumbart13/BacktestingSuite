package at.htlAnich.tools;

public final class BaumbartLogger {
	public static void logf(String s, Object ... o){
		System.out.printf(s, o);
	}
	public static void log(String s){
		logf(s);
	}
	public static void logln(String s){
		loglnf(s);
	}
	public static void loglnf(String s, Object ... o){
		logf(s.concat("%n"), o);
	}
	public static void errf(String s, Object ... o){
		System.err.printf(s, o);
	}
	public static void errlnf(String s, Object ... o){
		errf(s.concat("%n"), o);
	}
	public static void waitForKeyPress(){logf("Waiting for some input... "); var s = new java.util.Scanner(System.in); s.nextLine();}
}
