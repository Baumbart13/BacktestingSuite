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
		avg,
		money,
		strat;

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
				"%s FLOAT NOT NULL," +			// backtesting_avg
				"%s FLOAT NOT NULL," +			// backtesting_money
				"%s INTEGER NOT NULL," +		// backtesting_strat
				"primary key(%s, %s, %s));",	// pk = date+symbol+strat
			DatabaseNames_Backtesting.date.toString(),
			DatabaseNames_Backtesting.symbol.toString(),
			DatabaseNames_Backtesting.buyFlag.toString(),
			DatabaseNames_Backtesting.delta.toString(),
			DatabaseNames_Backtesting.stocks.toString(),
			DatabaseNames_Backtesting.worth.toString(),
			DatabaseNames_Backtesting.close.toString(),
			DatabaseNames_Backtesting.avg.toString(),
			DatabaseNames_Backtesting.money.toString(),
			DatabaseNames_Backtesting.strat.toString(),
			// Primary key
			DatabaseNames_Backtesting.date.toString(),
			DatabaseNames_Backtesting.symbol.toString(),
			DatabaseNames_Backtesting.strat.toString()
		));
		stmnt.execute();

		initTable();
	}

	private void initTable() throws SQLException{
		// if there is less than or exactly 1 entry,
		var stmnt = mConnection.prepareStatement(String.format(
			"SELECT COUNT(*) FROM %s AS 'entries';",
			_TABLE_NAME
		));

		var rs = stmnt.executeQuery();
		var needToInit = rs.getInt("entries") <= 1;

		if(!needToInit){
			return;
		}

		rs.close();
		stmnt = mConnection.prepareStatement(String.format(
			"INSERT into %s (%s) VALUES (?)",
			_TABLE_NAME, DatabaseNames_Backtesting.money
		));
		stmnt.setInt(1, 100_000);
		stmnt.executeUpdate();
	}

	public Depot getValues(String symbol, Depot.Strategy strategy) throws SQLException{
		Depot dep = new Depot(strategy);

		var stmnt = mConnection.prepareStatement(String.format(
			"select %s, %s, %s, %s, %s, %s, %s, %s, %s from %s where" +
			"%s=? and %s=?;",
			DatabaseNames_Backtesting.date.toString(),
			DatabaseNames_Backtesting.symbol.toString(),
			DatabaseNames_Backtesting.buyFlag.toString(),
			DatabaseNames_Backtesting.delta.toString(),
			DatabaseNames_Backtesting.stocks.toString(),
			DatabaseNames_Backtesting.worth.toString(),
			DatabaseNames_Backtesting.avg.toString(),
			DatabaseNames_Backtesting.close.toString(),
			DatabaseNames_Backtesting.strat.toString(),
			DatabaseNames_Backtesting.money.toString(),
			// from
			_TABLE_NAME,
			// where
			DatabaseNames_Backtesting.symbol.toString(),
			DatabaseNames_Backtesting.strat.toString()
		));

		stmnt.setString(1, symbol);
		stmnt.setInt(2, strategy.ordinal());

		var rs = stmnt.executeQuery();

		// Process from db into program

		while(rs.next()) {
			var date = rs.getDate(DatabaseNames_Backtesting.date.toString()).toLocalDate();
			var buyFlag = Depot.Point.BuyFlag.valueOf(rs.getInt(DatabaseNames_Backtesting.buyFlag.toString()));
			var delta = rs.getInt(DatabaseNames_Backtesting.delta.toString());
			var stocks = rs.getInt(DatabaseNames_Backtesting.stocks.toString());
			var worth = rs.getFloat(DatabaseNames_Backtesting.worth.toString());
			var avg = rs.getFloat(DatabaseNames_Backtesting.avg.toString());
			var close = rs.getFloat(DatabaseNames_Backtesting.close.toString());
			var strat = Depot.Strategy.valueOf(rs.getInt(DatabaseNames_Backtesting.strat.toString()));
			var money = rs.getFloat(DatabaseNames_Backtesting.money.toString());

			var depPoint = new Depot.Point(date, symbol, buyFlag, delta, stocks, worth, avg, close, money);

			dep.addDepotPoint(depPoint);
		}

		return dep;
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
			// backtesting_money
			StringBuilder stmntText = new StringBuilder(String.format(
				"INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?;",
				dep.getTableName(),
				DatabaseNames_Backtesting.date.toString(),
				DatabaseNames_Backtesting.symbol.toString(),
				DatabaseNames_Backtesting.buyFlag.toString(),
				DatabaseNames_Backtesting.delta.toString(),
				DatabaseNames_Backtesting.stocks.toString(),
				DatabaseNames_Backtesting.worth.toString(),
				DatabaseNames_Backtesting.close.toString(),
				DatabaseNames_Backtesting.avg.toString(),
				DatabaseNames_Backtesting.money.toString(),
				// VALUES...ON DUPLICATE KEY UPDATE
				DatabaseNames_Backtesting.buyFlag.toString(),
				DatabaseNames_Backtesting.delta.toString(),
				DatabaseNames_Backtesting.stocks.toString(),
				DatabaseNames_Backtesting.worth.toString(),
				DatabaseNames_Backtesting.close.toString(),
				DatabaseNames_Backtesting.avg.toString(),
				DatabaseNames_Backtesting.money.toString()
			));

			stmnt = mConnection.prepareStatement(stmntText.toString());

			var i = 1;
			stmnt.setDate(i++, Date.valueOf(point.getDate()));
			stmnt.setString(i++, symbol);
			stmnt.setInt(i++, point.getFlag().ordinal());	// backtesting_buyFlag
			stmnt.setFloat(i++, point.getBuyAmount());		// backtesting_delta
			stmnt.setInt(i++, point.getStocks());				// backtesting_stocks
			stmnt.setFloat(i++, point.getWorth());			// backtesting_worth
			stmnt.setFloat(i++, point.getClose());			// backtesting_close
			stmnt.setFloat(i++, point.getAvg200());			// backtesting_avg
			stmnt.setFloat(i++, point.getMoney());		// backtesting_money
			// VALUES...ON DUPLICATE KEY UPDATE
			stmnt.setInt(i++, point.getFlag().ordinal());	// backtesting_buyFlag
			stmnt.setFloat(i++, point.getBuyAmount());		// backtesting_delta
			stmnt.setInt(i++, point.getStocks());				// backtesting_stocks
			stmnt.setFloat(i++, point.getWorth());			// backtesting_worth
			stmnt.setFloat(i++, point.getClose());			// backtesting_close
			stmnt.setFloat(i++, point.getAvg200());			// backtesting_avg
			stmnt.setFloat(i++, point.getMoney());			// backtesting_money

			stmnt.executeUpdate();
		}
		return;
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
