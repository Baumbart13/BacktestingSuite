package at.htlAnich.backTestingSuite;

import at.htlAnich.stockUpdater.StockDatabase;
import at.htlAnich.tools.database.Database;
import jdk.jshell.spi.ExecutionControl;

import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedList;

import static at.htlAnich.tools.BaumbartLogger.logf;
import static at.htlAnich.tools.BaumbartLogger.loglnf;

public class BackTestingDatabase extends StockDatabase {
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

	@Override
	public void createTable(String tableName) throws SQLException{
		mConnection.prepareStatement(String.format(
			"DROP TABLE IF EXISTS %s;",
			_TABLE_NAME
		)).execute();

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
			_TABLE_NAME,
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
	}

	public void initTable(String symbol, LocalDate startDate) throws SQLException{
		// if there is less than or exactly 1 entry,
		var stmnt = mConnection.prepareStatement(String.format(
			"SELECT COUNT(*) AS 'my_entries' FROM %s;",
			_TABLE_NAME
		));

		var rs = stmnt.executeQuery();
		rs.next();
		var needToInit = rs.getInt("my_entries") <= 1;
		rs.close();

		if(!needToInit){
			return;
		}


		// date, symbol and strat are the primary keys and thus need a value to be set or likely
		// TODO: rework BackTestingDatabase.initTable()
		for(var strat : Depot.Strategy.values()) {

			stmnt = this.mConnection.prepareStatement(String.format(
				"INSERT into %s (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				_TABLE_NAME,
				DatabaseNames_Backtesting.date,
				DatabaseNames_Backtesting.symbol,
				DatabaseNames_Backtesting.strat,
				DatabaseNames_Backtesting.money,
				DatabaseNames_Backtesting.buyFlag,
				DatabaseNames_Backtesting.delta,
				DatabaseNames_Backtesting.stocks,
				DatabaseNames_Backtesting.worth,
				DatabaseNames_Backtesting.close,
				DatabaseNames_Backtesting.avg
			));
			stmnt.setDate(1, Date.valueOf(startDate));
			stmnt.setString(2, symbol);
			stmnt.setInt(3, strat.ordinal());
			stmnt.setFloat(4, 100_000);
			stmnt.setInt(5, Depot.Point.BuyFlag.UNCHANGED.ordinal());
			stmnt.setInt(6, 0);
			stmnt.setInt(7, 0);
			stmnt.setFloat(8, 0.0f);
			stmnt.setFloat(9, 0.0f);
			stmnt.setFloat(10, 0.0f);
			stmnt.execute();
		}
	}

	//TODO: rewrite getValues

	/**
	 * Requests all values from the backtesting_depot and returns it as an <code>Depot</code>-object.
	 * @param symbol The data will be requested from this stock-symbol.
	 * @param strategy The strategy that gets requested.
	 * @return a <code>Depot</code> containing all requested information lying on the database.
	 * @throws SQLException
	 */
	public Depot getValues(String symbol, Depot.Strategy strategy) throws SQLException{
		createTable(_TABLE_NAME);

		var stmnt = this.mConnection.prepareStatement(String.format(
			"select %s, %s, %s, %s, %s, %s, %s, %s from %s where " +
			"%s=? and %s=?;",
			DatabaseNames_Backtesting.date.toString(),
			DatabaseNames_Backtesting.buyFlag.toString(),
			DatabaseNames_Backtesting.delta.toString(),
			DatabaseNames_Backtesting.stocks.toString(),
			DatabaseNames_Backtesting.worth.toString(),
			DatabaseNames_Backtesting.avg.toString(),
			DatabaseNames_Backtesting.close.toString(),
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

		// Process db-stuff into Depot

		var dep = new Depot(symbol, strategy);
		while(rs.next()){
			LocalDate date;
			Depot.Point.BuyFlag flag;
			int buyAmount;
			int totalStocks;
			float totalWorth;
			float avg200;
			float close;
			float money;

			var point = new Depot.Point();
		}

		return dep;
	}
	/*public Depot getValues(String symbol, Depot.Strategy strategy) throws SQLException{
		createTable(_TABLE_NAME);
		Depot dep = new Depot(symbol, strategy);

		var stmntText = String.format(
			"select %s, %s, %s, %s, %s, %s, %s, %s, %s from %s where " +
			"%s=? and %s=?;",
			DatabaseNames_Backtesting.date.toString(),
			DatabaseNames_Backtesting.symbol.toString(),
			DatabaseNames_Backtesting.buyFlag.toString(),
			DatabaseNames_Backtesting.delta.toString(),
			DatabaseNames_Backtesting.stocks.toString(),
			DatabaseNames_Backtesting.worth.toString(),
			DatabaseNames_Backtesting.avg.toString(),
			DatabaseNames_Backtesting.close.toString(),
			DatabaseNames_Backtesting.money.toString(),
			// from
			_TABLE_NAME,
			// where
			DatabaseNames_Backtesting.symbol.toString(),
			DatabaseNames_Backtesting.strat.toString()
		);

		var stmnt = mConnection.prepareStatement(stmntText);

		stmnt.setString(1, symbol);
		stmnt.setInt(2, strategy.ordinal());

		var rs = stmnt.executeQuery();

		// Process from db into program

		while (rs.next()) {
			var date = rs.getDate(DatabaseNames_Backtesting.date.toString()).toLocalDate();
			var buyFlag = Depot.Point.BuyFlag.valueOf(rs.getInt(DatabaseNames_Backtesting.buyFlag.toString()));
			var delta = rs.getInt(DatabaseNames_Backtesting.delta.toString());
			var stocks = rs.getInt(DatabaseNames_Backtesting.stocks.toString());
			var worth = rs.getFloat(DatabaseNames_Backtesting.worth.toString());
			var avg = rs.getFloat(DatabaseNames_Backtesting.avg.toString());
			var close = rs.getFloat(DatabaseNames_Backtesting.close.toString());
			//var strat = Depot.Strategy.valueOf(rs.getInt(DatabaseNames_Backtesting.strat.toString())); // mistake by myself
			var money = rs.getFloat(DatabaseNames_Backtesting.money.toString());

			var depPoint = new Depot.Point(date, buyFlag, delta, stocks, worth, avg, close, money);

			dep.getData().add(depPoint);
		}

		return dep;
	}*/

	public BackTestingDatabase(){
		this("","","","");
	}

	public BackTestingDatabase(BackTestingDatabase db){
		this(db.mHostname, db.mUser, db.mPassword, db.mDatabase);
	}

	public BackTestingDatabase(String hostname, String user, String password, String database) {
		super(hostname, user, password, database);
	}

	/*@Override
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
	}*/

	public void updateDepots(Depot dep, String symbol) throws SQLException{
		try{
			throw new ExecutionControl.NotImplementedException("LEL");
		}catch (ExecutionControl.NotImplementedException e){
			e.printStackTrace();
		}
	}

	// TODO: rewrite updateDepots
	/*public void updateDepots(Depot dep, String symbol) throws SQLException{
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
				"INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?;",
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
				DatabaseNames_Backtesting.strat.toString(),
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
			stmnt.setInt(i++, dep.mStrategy.ordinal());	// backtesting_strat
			// VALUES...ON DUPLICATE KEY UPDATE
			stmnt.setInt(i++, point.getFlag().ordinal());	// backtesting_buyFlag
			stmnt.setFloat(i++, point.getBuyAmount());		// backtesting_delta
			stmnt.setInt(i++, point.getStocks());				// backtesting_stocks
			stmnt.setFloat(i++, point.getWorth());			// backtesting_worth
			stmnt.setFloat(i++, point.getClose());			// backtesting_close
			stmnt.setFloat(i++, point.getAvg200());			// backtesting_avg
			stmnt.setFloat(i++, point.getMoney());			// backtesting_money

			loglnf(stmnt.toString());
			stmnt.executeUpdate();
		}
		return;
	}*/

	/*@Override
	public void disconnect() throws SQLException {
		if(mConnection == null || mConnection.isClosed()){
			logf("Connection already closed.");
			return;
		}

		mConnection.close();
		mConnection = null;
		return;
	}*/

	/*@Override
	public void createDatabase(String database) throws SQLException {
		var stmnt = mConnection.prepareStatement(String.format(
			"CREATE DATABASE IF NOT EXISTS %s",
			database.trim()
		));
		stmnt.execute();
		return;
	}*/

	@Override
	public Database clone() {
		return new BackTestingDatabase(this);
	}
}
