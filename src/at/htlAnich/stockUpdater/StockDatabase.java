package at.htlAnich.stockUpdater;

import at.htlAnich.tools.database.CanBeTable;
import at.htlAnich.tools.database.Database;
import at.htlAnich.tools.database.MySQL;
import jdk.jshell.spi.ExecutionControl;

import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static at.htlAnich.tools.BaumbartLogger.*;

/**
 * @author Baumbart13
 */
public class StockDatabase extends MySQL implements CanBeTable {
	public static final String	_TABLE_NAME__DATA	= "stock_data";

	public StockDatabase(){
		this("", "", "", "");
	}

	public StockDatabase(String hostname, String user, String password, String database){
		super(hostname, user, password, database);
	}

	public StockDatabase(StockDatabase stockDb){
		this(stockDb.mHostname, stockDb.mUser, stockDb.mPassword, stockDb.mDatabase);
	}

	@Override
	public Database clone() {
		return new StockDatabase(this);
	}

	@Override
	public void createDatabase(String database) throws SQLException {
		var stmnt = mConnection.createStatement();
		stmnt.execute("CREATE DATABASE IF NOT EXISTS " + database.trim() + ";");
		return;
	}

	public void createDatabase() throws SQLException{
		this.createDatabase(this.mDatabase);
	}

	public void createTable(String tableName) throws SQLException{
		PreparedStatement stmnt;

		stmnt = mConnection.prepareStatement(String.format(
			"CREATE TABLE IF NOT EXISTS %s (" +
				"%s DATETIME NOT NULL," +	// data_datetime
				"%s VARCHAR(8) NOT NULL," +	// data_symbol
				"%s FLOAT," +			// data_open
				"%s FLOAT," +			// data_close
				"%s FLOAT," +			// data_high
				"%s FLOAT," +			// data_low
				"%s FLOAT," +			// data_volume
				"%s FLOAT," +			// data_splitCoefficient
				"%s FLOAT," +			// data_close_adjusted
				"%s FLOAT," +			// data_avg200
				"PRIMARY KEY(%s, %s));",	// data_datetime, data_symbol

			tableName,
			// PRIMARY KEYS
			StockResults.DatabaseNames_Data.data_datetime.toString(),
			StockResults.DatabaseNames_Data.data_symbol.toString(),
			// VALUES
			StockResults.DatabaseNames_Data.data_open.toString(),
			StockResults.DatabaseNames_Data.data_close.toString(),
			StockResults.DatabaseNames_Data.data_high.toString(),
			StockResults.DatabaseNames_Data.data_low.toString(),
			StockResults.DatabaseNames_Data.data_volume.toString(),
			//StockResults.DatabaseNames_Data.data_avg.toString(200), // removed due to different handling since v2
			StockResults.DatabaseNames_Data.data_splitCoefficient.toString(),
			StockResults.DatabaseNames_Data.data_close_adjusted.toString(),
			StockResults.DatabaseNames_Data.data_avg200.toString(),
			// PRIMARY KEY-Declaration
			StockResults.DatabaseNames_Data.data_datetime.toString(),
			StockResults.DatabaseNames_Data.data_symbol.toString()
		));

		stmnt.execute();
	}

	/**
	 * Updates the database with the provided <code>StockResults</code>.
	 * @param results The data that shall be written to the database.
	 * @throws SQLException
	 */
	public void insertOrUpdateStock(StockResults results) throws SQLException{
		createDatabase();
		createTable(results.getTableName());

		loglnf("insertOrUpdateStock_DATA(results)");
		insertOrUpdateStock_DATA(results);
		loglnf("Uploaded to %s:%s", mDatabase, _TABLE_NAME__DATA);
	}

	private void insertOrUpdateStock_DATA(StockResults results) throws SQLException {

		final var datetime = StockResults.DatabaseNames_Data.data_datetime.toString();
		final var symbol = StockResults.DatabaseNames_Data.data_symbol.toString();
		final var open = StockResults.DatabaseNames_Data.data_open.toString();
		final var close = StockResults.DatabaseNames_Data.data_close.toString();
		final var high = StockResults.DatabaseNames_Data.data_high.toString();
		final var low = StockResults.DatabaseNames_Data.data_low.toString();
		final var volume = StockResults.DatabaseNames_Data.data_volume.toString();
		final var split = StockResults.DatabaseNames_Data.data_splitCoefficient.toString();
		final var closeAdj = StockResults.DatabaseNames_Data.data_close_adjusted.toString();
		final var avg200 = StockResults.DatabaseNames_Data.data_avg200.toString();

		// do not upload all entries at once to the database
		// if connection problems happen, so all data could be corrupted
		// therefore we upload all one-by-one
		for(var dataPoint : results.getDataPoints()){

			//////////////////////////////////
			//                              //
			//         TESTING VALUES       //
			//                              //
			//////////////////////////////////

			/*dataPoint.mDateTime = LocalDateTime.of(2021, 12, 12,12,12,12,12);
			results.getSymbol();
			dataPoint.setValue(StockDataPoint.ValueType.open, 1.0f);
			loglnf("key:%s\tvalue:%10.4f", StockDataPoint.ValueType.open, dataPoint.getValue(StockDataPoint.ValueType.open));
			dataPoint.setValue(StockDataPoint.ValueType.close,2.0f);
			loglnf("key:%s\tvalue:%10.4f", StockDataPoint.ValueType.close, dataPoint.getValue(StockDataPoint.ValueType.close));
			dataPoint.setValue(StockDataPoint.ValueType.high, 3.0f);
			loglnf("key:%s\tvalue:%10.4f", StockDataPoint.ValueType.high, dataPoint.getValue(StockDataPoint.ValueType.high));
			dataPoint.setValue(StockDataPoint.ValueType.low, 4.0f);
			loglnf("key:%s\tvalue:%10.4f", StockDataPoint.ValueType.low, dataPoint.getValue(StockDataPoint.ValueType.low));
			dataPoint.setValue(StockDataPoint.ValueType.volume, 5.0f);
			loglnf("key:%s\tvalue:%10.4f", StockDataPoint.ValueType.volume, dataPoint.getValue(StockDataPoint.ValueType.volume));
			dataPoint.setValue(StockDataPoint.ValueType.splitCoefficient, 6.0f);
			loglnf("key:%s\tvalue:%10.4f", StockDataPoint.ValueType.splitCoefficient, dataPoint.getValue(StockDataPoint.ValueType.splitCoefficient));
			dataPoint.setValue(StockDataPoint.ValueType.close_adjusted, 7.0f);
			loglnf("key:%s\tvalue:%10.4f", StockDataPoint.ValueType.close_adjusted, dataPoint.getValue(StockDataPoint.ValueType.close_adjusted));
			dataPoint.setValue(StockDataPoint.ValueType.avg200, 8.0f);
			loglnf("key:%s\tvalue:%10.4f", StockDataPoint.ValueType.avg200, dataPoint.getValue(StockDataPoint.ValueType.avg200));*/


			//////////////////////////////////
			//                              //
			//          UPDATE-TEXT         //
			//                              //
			//////////////////////////////////
			var stmntTextInsertInto = new StringBuilder(String.format(
				"insert into %s (%s, %s, " +
				"%s, %s, %s, %s, %s, %s, %s, %s)" +
				"VALUES ("   +   "? ,  ?, " +
				"? , ? , ? , ? , ? , ? , ? , ?) ",
				_TABLE_NAME__DATA, datetime, symbol,
				open, close, high, low, volume, split, closeAdj, avg200
			));
			var stmntTextOnUpdate = new StringBuilder(String.format(
				"on duplicate key update %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?, %s=?;",
				open, close, high, low, volume, split, closeAdj, avg200
			));

			var stmntText = stmntTextInsertInto.append(stmntTextOnUpdate);

			//////////////////////////////////
			//                              //
			//       INSERTING VALUES       //
			//                              //
			//////////////////////////////////

			var stmnt = mConnection.prepareStatement(stmntText.toString());

			var i = 1;
			// data_datetime
			// data_symbol
			stmnt.setDate(i++, Date.valueOf(dataPoint.mDateTime.toLocalDate()));
			stmnt.setString(i++, results.getSymbol());

			// data_open
			// data_close
			// data_high
			// data_low
			// data_volume
			// data_splitCoefficient
			// data_close_adjusted
			// data_avg200
			stmnt.setFloat(i++, dataPoint.getValue(StockDataPoint.ValueType.open));
			stmnt.setFloat(i++, dataPoint.getValue(StockDataPoint.ValueType.close));
			stmnt.setFloat(i++, dataPoint.getValue(StockDataPoint.ValueType.high));
			stmnt.setFloat(i++, dataPoint.getValue(StockDataPoint.ValueType.low));
			stmnt.setFloat(i++, dataPoint.getValue(StockDataPoint.ValueType.volume));
			stmnt.setFloat(i++, dataPoint.getValue(StockDataPoint.ValueType.splitCoefficient));
			stmnt.setFloat(i++, dataPoint.getValue(StockDataPoint.ValueType.close_adjusted));
			stmnt.setFloat(i++, dataPoint.getValue(StockDataPoint.ValueType.avg200));

			// data_open
			// data_close
			// data_high
			// data_low
			// data_volume
			// data_splitCoefficient
			// data_close_adjusted
			// data_avg200
			stmnt.setFloat(i++, dataPoint.getValue(StockDataPoint.ValueType.open));
			stmnt.setFloat(i++, dataPoint.getValue(StockDataPoint.ValueType.close));
			stmnt.setFloat(i++, dataPoint.getValue(StockDataPoint.ValueType.high));
			stmnt.setFloat(i++, dataPoint.getValue(StockDataPoint.ValueType.low));
			stmnt.setFloat(i++, dataPoint.getValue(StockDataPoint.ValueType.volume));
			stmnt.setFloat(i++, dataPoint.getValue(StockDataPoint.ValueType.splitCoefficient));
			stmnt.setFloat(i++, dataPoint.getValue(StockDataPoint.ValueType.close_adjusted));
			stmnt.setFloat(i++, dataPoint.getValue(StockDataPoint.ValueType.avg200));

			stmnt.executeUpdate();
		}

		return;
	}

	/**
	 * Returns the existing average-columns on the database.
	 * @return an array with every average-column existing on the database.
	 */
	public Long[] getAvgsOnDatabase(){
		var out = new LinkedList<Long>();
		final var sql = "SHOW COLUMNS FROM " + _TABLE_NAME__DATA + ";";

		try{
			var stmnt = mConnection.prepareStatement(sql);
			var rs = stmnt.executeQuery();
			while(rs.next()){
				var line = rs.getString("Field");
				if(line.contains("avg")){
					out.add(Long.valueOf(line.replace("data_avg", "")));
				}
			}

		}catch(SQLException e){
			e.printStackTrace();
		}

		return out.toArray(new Long[out.size()]);
	}

	public LocalDateTime getNewestStockDataEntry(String symbol){
		var sql = String.format("SELECT * FROM %s" +
				"WHERE %s = ?" +
				"ORDER BY %s ASC" +
				"LIMIT 201;",
			_TABLE_NAME__DATA,
			StockResults.DatabaseNames_Data.data_symbol.toString(),
			StockResults.DatabaseNames_Data.data_datetime.toString()
		);

		var out = LocalDate.MIN.atStartOfDay();
		try{
			var stmnt = mConnection.prepareStatement(sql);
			stmnt.setString(1, symbol);
			var rs = stmnt.executeQuery();

			rs.first();
			var columnLabel = StockResults.DatabaseNames_Data.data_datetime.toString().toLowerCase();
			out = rs.getTimestamp(columnLabel).toLocalDateTime();

		}catch (SQLException e){
			e.printStackTrace();
		}
		return out;
	}

	/**
	 * <strong>DataTables structure:</strong><br>
	 * 	<code>stock_datetime	DATETIME NOT NULL,<br>
	 * 	stock_symbol	VARCHAR(8) NOT NULL,<br>
	 * 	open FLOAT,<br>
	 * 	close FLOAT,<br>
	 * 	high FLOAT,<br>
	 * 	low FLOAT,<br>
	 * 	volume FLOAT,<br>
	 * 	splitCoefficient FLOAT,<br>
	 * 	close_adjusted FLOAT,<br>
	 * 	avgX FLOAT</code><br><br>
	 * "avgX" stands for the many averages, each based on a different amount of days.<br><br><br>
	 *
	 * <strong>SymbolTables structure:</strong><br>
	 * 	<code>symbol	STRING NOT NULL,<br>
	 * 	exchange	UINT64,<br>
	 * 	asset	UINT64,<br>
	 * 	ipoDate	DATETIME NOT NULL,<br>
	 * 	delistingDate	DATETIME NOT NULL,<br>
	 * 	status	UINT32</code><br><br>
	 *
	 * @return the name of the table.
	 */
	@Override
	public String getTableName() {
		return String.format("%s",
			_TABLE_NAME__DATA);
	}
}
