package at.htlAnich.backTestingSuite.badCode;

import at.htlAnich.stockUpdater.CredentialLoader;
import at.htlAnich.stockUpdater.StockDataPoint;
import at.htlAnich.stockUpdater.StockResults;
import at.htlAnich.backTestingSuite.api.ApiParser;
import at.htlAnich.stockUpdater.api.ApiProcessor;

import java.io.IOException;

public class DamnShit {
	protected String mSymbol;
	protected boolean mInProduction;
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

		// update depotData with keptData

		// write new values to backtesting_depot
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

		var db = new
	}
}
