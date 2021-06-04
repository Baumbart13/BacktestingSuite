package at.htlAnich.tools.database;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class Database {
	protected Connection mConnection	= null;
	protected String	mHostname	= "",
		mUser		= "",
		mPassword	= "",
		mDatabase	= "";

	protected static String createConnectionString(String hostname, String database, String user, String pass){
		return String.format("jdbc:mysql://%s/%s?user=%s&password=%s?serverTimezone=UTC?autoReconnect=true&useSSL=false",
			hostname, database, user, pass);
	}

	public Database(String hostname, String user, String password, String database){
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		}catch(ClassNotFoundException e){
			at.htlAnich.tools.BaumbartLogger.errf("JDBC is not installed. Please first install the driver and try to run this program afterwards.%n");
			System.exit(-1);
		}

		if(hostname.length() < 1 ||
			user.length() < 1 ||
			password.length() < 1 ||
			database.length() < 1){
			hostname = user = password = database = "FATAL ERROR: Wrong credentials!";
		}

		this.mHostname = hostname;
		this.mUser = user;
		this.mPassword = password;
		this.mDatabase = database;
	}

	public abstract void connect() throws SQLException;
	public abstract void disconnect() throws SQLException;
	public abstract void createDatabase(String database) throws SQLException;

	public void createDatabase() throws SQLException{
		this.createDatabase(mDatabase);
	}

	@Override
	public String toString(){
		return String.format("Hostname:%s;User:%s;Password:%s;Database:%s",
			mHostname, mUser, mPassword, mDatabase);
	}

	@Override
	public abstract Database clone();

	public String getHostname() {
		return mHostname;
	}

	public String getUser() {
		return mUser;
	}

	public String getDatabase() {
		return mDatabase;
	}
}
