package at.htlAnich.tools.database;

import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class MySQL extends Database{

	public MySQL(String hostname, String user, String password, String database) {
		super(hostname, user, password, database);
	}

	@Override
	public void connect() throws SQLException {
		if(mConnection != null){
			if(!mConnection.isClosed()){
				System.out.println("Connection already opened.");
				return;
			}
			return;
		}

		mConnection = DriverManager.getConnection(String.format(
			"jdbc:mysql://%s/%s", mHostname, mDatabase),
			mUser,
			mPassword
		);
		return;
	}

	@Override
	public void disconnect() throws SQLException {
		if(mConnection == null || mConnection.isClosed()){
			return;
		}

		mConnection.close();
		mConnection = null;
		return;
	}
}
