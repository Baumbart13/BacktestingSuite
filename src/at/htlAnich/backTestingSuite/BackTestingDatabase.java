package at.htlAnich.backTestingSuite;

import at.htlAnich.tools.database.Database;

import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static at.htlAnich.tools.BaumbartLogger.logf;

public class BackTestingDatabase extends Database {
	public static final String _TABLE_NAME = "backtesting_depot";

	public static enum DatabaseNames_Backtesting{
		date,
		symbol,
		buyFlag,
		delta,
		stocks,
		worth,
		close,
		avg;

		@Override
		public String toString(){
			return "backtesting_"+name();
		}
	}

	public void createTable(String tableName) throws SQLException{
		var stmnt = mConnection.prepareStatement(String.format(
			"CREATE TABLE IF NOT EXISTS %s " +
				"(%s DATETIME NOT NULL," +		// backtesting_date
				"%s VARCHAR(8) NOT NULL," +		// backtesting_symbol
				"%s INTEGER NOT NULL," +		// backtesting_buyFlag
				"%s INTEGER NOT NULL," +		// backtesting_delta
				"%s INTEGER NOT NULL," +		// backtesting_stocks
				"%s FLOAT NOT NULL," +			// backtesting_worth
				"%s FLOAT NOT NULL," +			// backtesting_close
				"%s FLOAT NOT NULL);",			// backtesting_avg
			DatabaseNames_Backtesting.date.toString(),
			DatabaseNames_Backtesting.symbol.toString(),
			DatabaseNames_Backtesting.buyFlag.toString(),
			DatabaseNames_Backtesting.delta.toString(),
			DatabaseNames_Backtesting.stocks.toString(),
			DatabaseNames_Backtesting.worth.toString(),
			DatabaseNames_Backtesting.close.toString(),
			DatabaseNames_Backtesting.avg.toString()
		));
		stmnt.execute();
	}

	public Depot getValues(String symbol, Depot.Strategy strategy){
		return new Depot();
	}

	public BackTestingDatabase(){
		this("","","","");
	}

	public BackTestingDatabase(BackTestingDatabase db){
		this(db.mHostname, db.mUser, db.mPassword, db.mDatabase);
	}

	public BackTestingDatabase(String hostname, String user, String password, String database) {
		super(hostname, user, password, database);
	}

	@Override
	public void connect() throws SQLException {

		if(mConnection != null){
			if(!mConnection.isClosed()){
				logf("Connection already opened.");
				return;
			}
		}

		mConnection = DriverManager.getConnection(createConnectionString(
			mHostname, mDatabase, mUser, mPassword
		));
		return;
	}

	public void updateDepots(Depot dep, String symbol) throws SQLException{
		createDatabase();
		createTable(dep.getTableName());

		for(var point : dep.getAll(symbol)) {
			PreparedStatement stmnt;
			// backtesting_date
			// backtesting_symbol
			// backtesting_buyFlag
			// backtesting_delta
			// backtesting_stocks
			// backtesting_worth
			// backtesting_close
			// backtesting_avg
			StringBuilder stmntText = new StringBuilder(String.format(
				"INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s) VALUES (? ? ? ? ? ? ?) ON DUPLICATE KEY UPDATE %s=?, %s=?, %s=?, %s=?;",
				dep.getTableName(),
				DatabaseNames_Backtesting.date.toString(),
				DatabaseNames_Backtesting.symbol.toString(),
				DatabaseNames_Backtesting.buyFlag.toString(),
				DatabaseNames_Backtesting.delta.toString(),
				DatabaseNames_Backtesting.stocks.toString(),
				DatabaseNames_Backtesting.worth.toString(),
				DatabaseNames_Backtesting.close.toString(),
				DatabaseNames_Backtesting.avg.toString(),
				// VALUES...ON DUPLICATE KEY UPDATE
				DatabaseNames_Backtesting.buyFlag.toString(),
				DatabaseNames_Backtesting.delta.toString(),
				DatabaseNames_Backtesting.stocks.toString(),
				DatabaseNames_Backtesting.worth.toString(),
				DatabaseNames_Backtesting.close.toString(),
				DatabaseNames_Backtesting.avg.toString()
			));

			stmnt = mConnection.prepareStatement(stmntText.toString());

			stmnt.setDate(1, Date.valueOf(point.getDate()));
			stmnt.setString(2, symbol);
			stmnt.setInt(3, point.getFlag().ordinal());

		}
	}

	@Override
	public void disconnect() throws SQLException {
		if(mConnection == null || mConnection.isClosed()){
			logf("Connection already closed.");
			return;
		}

		mConnection.close();
		mConnection = null;
		return;
	}

	@Override
	public void createDatabase(String database) throws SQLException {
		var stmnt = mConnection.prepareStatement(String.format(
			"CREATE DATABASE IF NOT EXISTS %s",
			database.trim()
		));
		stmnt.execute();
		return;
	}

	@Override
	public Database clone() {
		return new BackTestingDatabase(this);
	}
}
