package at.htlAnich.backTestingSuite.badCode;

import at.htlAnich.stockUpdater.CredentialLoader;
import at.htlAnich.stockUpdater.StockDataPoint;
import at.htlAnich.stockUpdater.StockDatabase;
import at.htlAnich.stockUpdater.StockResults;
import at.htlAnich.backTestingSuite.api.ApiParser;
import at.htlAnich.stockUpdater.api.ApiProcessor;

import java.io.IOException;
import java.sql.SQLException;

import static at.htlAnich.tools.BaumbartLogger.errlnf;

public class DamnShit {
	protected String mSymbol;
	protected boolean mInProduction;
	protected StockDatabase mStockDb;

	public DamnShit(String symbol, boolean inProduction){
		mSymbol = symbol;
		mInProduction = inProduction;
	}

	public void getValuesAndUpdateDatabase(){
		// get data from API
		var stockData = fetchFromAPI();

		// update all values
		updateStockValues(stockData);

		// write values to stock_data(DB) and keep stockData(StockResults-Object)
		writeToStocksDB(stockData);

		// get depotData(Depot-Object) from database
		//var depotData = readFromDepotDb(stockData); // TODO: Implement at.htlAnich.backTestingSuite.badCode.DamnShit.readFromDepotDb

		// update depotData with keptData/stockData
		//updateDepotValues(stockData, depotData); // TODO: implement at.htlAnich.backTestingSuite.badCode.DamnShit.updateDepotValues

		// trade on depotData
		// ATTENTION:	don't forget splitcorrection, which has to
		// 		be done backwards, but trading is forwards!!
		//depotData.trade(); // TODO: implement at.htlAnich.backTestingSuite.Depot.trade

		// write new values to backtesting_depot
		//writeToDepotDb(depotData); // TODO: implement at.htlAnich.backTestingSuite.badCode.DamnShit.writeToDepotDb
	}

	public StockResults fetchFromAPI(){
		StockResults res = null;
		try {
			var creds = CredentialLoader.loadApi(mInProduction);
			res = new ApiParser(creds).request(mSymbol, ApiParser.Function.TIME_SERIES_DAILY_ADJUSTED);
		}catch(IOException e){
			e.printStackTrace();
		}
		return res;
	}

	public void updateStockValues(StockResults res){
		res.splitCorrection();
		res.calcAverage();
	}

	public void writeToStocksDB(StockResults res){
		var writingWith = res.clone();

		mStockDb = new StockDatabase("localhost", "root", "DuArschloch4", "baumbartstocks");

		try {
			mStockDb.connect();
			mStockDb.createDatabase();
			mStockDb.createTable(writingWith.getTableName());
			mStockDb.insertOrUpdateStock(writingWith);
			mStockDb.disconnect();
		}catch(SQLException e){
			errlnf("go away, exception, i'm scared of you");
			e.printStackTrace();
		}
	}
}
