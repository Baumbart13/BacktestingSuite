package at.htlAnich.tools;

public final class BaumbartLogger {
	public static void logf(String s, Object ... o){
		System.out.printf(s, o);
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
	public static void waitForKeyPress(){var s = new java.util.Scanner(System.in); s.nextLine();}
	public static void log(String s){
		logf(s);
	}
	public static void log(Object s){
		log(s.toString());
	}
	public static void log(byte s){
		log(s);
	}
	public static void log(char s){
		log(s);
	}
	public static void log(short s){
		log(s);
	}
	public static void log(int s){
		log(s);
	}
	public static void log(long s){
		log(s);
	}
	public static void log(float s){
		log(s);
	}
	public static void log(double s){
		log(s);
	}
	public static void log(boolean s){
		log(s);
	}
	public static void logln(String s){
		log(s+System.lineSeparator());
	}
	public static void logln(Object s){
		logln(s.toString());
	}
	public static void logln(byte s){
		logln(s);
	}
	public static void logln(char s){
		logln(s);
	}
	public static void logln(short s){
		logln(s);
	}
	public static void logln(int s){
		logln(s);
	}
	public static void logln(long s){
		logln(s);
	}
	public static void logln(float s){
		logln(s);
	}
	public static void logln(double s){
		logln(s);
	}
	public static void logln(boolean s){
		logln(s);
	}
}
