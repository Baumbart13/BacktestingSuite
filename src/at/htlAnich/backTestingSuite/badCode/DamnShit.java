package at.htlAnich.backTestingSuite.badCode;

import at.htlAnich.backTestingSuite.BackTestingDatabase;
import at.htlAnich.backTestingSuite.Depot;
import at.htlAnich.backTestingSuite.Trader;
import at.htlAnich.stockUpdater.CredentialLoader;
import at.htlAnich.stockUpdater.StockDataPoint;
import at.htlAnich.stockUpdater.StockDatabase;
import at.htlAnich.stockUpdater.StockResults;
import at.htlAnich.backTestingSuite.api.ApiParser;
import at.htlAnich.tools.BaumbartLoggerGUI;
import at.htlAnich.tools.Environment;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static at.htlAnich.tools.BaumbartLogger.errlnf;
import static at.htlAnich.tools.BaumbartLogger.loglnf;

public class DamnShit {
	protected String mSymbol;
	protected float mTotalMoney;
	protected boolean mInProduction;
	protected StockDatabase mStockDb;
	protected BackTestingDatabase mBacktestDb;

	public static BaumbartLoggerGUI mLogGui = new BaumbartLoggerGUI("Detailed Log");

	public DamnShit(String symbol, float totalMoney, boolean inProduction){
		mSymbol = symbol;
		mTotalMoney = totalMoney;
		mInProduction = inProduction;
	}

	private static final CredentialLoader.ApiCredentials crapApiCred =
		new CredentialLoader.ApiCredentials("VBAX1XGSP5QC85SL");
	private static final CredentialLoader.DatabaseCredentials crapDbCred =
		new CredentialLoader.DatabaseCredentials("localhost","root","DuArschloch4","baumbartstocks");

	/**
	 * @return the Depot with the traded stocks
	 */
	public Depot getValuesAndUpdateDatabase(LocalDate start, LocalDate end){
		mLogGui.loglnf("Fetching data from API.");
		// get data from API
		var stockData = fetchFromAPI();
		mLogGui.loglnf("Successfully fetched %d entries from stock \"%s\".",
			stockData.getDataPoints().size(), mSymbol);

		mLogGui.loglnf("Updating stock values");
		// update all values
		updateStockValues(stockData);

		mLogGui.loglnf("Writing stocks to Stock-database");
		// write values to stock_data(DB) and keep stockData(StockResults-Object)
		writeToStocksDB(stockData);

		// delete all dates, that are not between (including) start and (excluding) end
		deleteIrrelevantDates(stockData, start, end);

		// get depotData(Depot-Object) from database
		var depotData = readFromDepotDb(stockData);

		// update depotData
		for(var i = 0; i<depotData.size(); ++i) {
			updateDepotValues(depotData.get(i), stockData);
		}

		// trade on depotData
		// ATTENTION:	don't forget splitCorrection, which has to
		// 		be done backwards, but trading is forwards!!
		for(var dep : depotData){
			if(dep.getStrategy().equals(Depot.Strategy.NONE)){
				continue;
			}
			Trader.trade(dep, stockData);
		}

		// write new values to backtesting_depot
		writeToDepotDb(depotData, stockData.getSymbol());

		// create diagram of backtesting-strategies
		drawChart(depotData, stockData.getSymbol());

		return depotData.get(0);
	}

	public void updateDepotValues(Depot dep, StockResults stockRes){
		for(int i = 0; i < stockRes.getDataPoints().size(); ++i){
			var currDepPoint = (i < dep.getData().size()) ? dep.getData().get(i) : null;
			var currStockPoint = stockRes.getDataPoints().get(i);

			// definitely easier to read, instead of using the ternary operator instead of setting it first
			// null, then check for null and then set new DepotPoint
			if(currDepPoint != null){
				if(currDepPoint.mDate.isEqual(currStockPoint.mDateTime.toLocalDate())){
					continue;
				}
				currDepPoint.mDate = currStockPoint.mDateTime.toLocalDate();
			}


			currDepPoint = new Depot.Point(
				currStockPoint.mDateTime.toLocalDate(),
				Depot.Point.BuyFlag.UNCHANGED,
				0,
				dep.getData().get(i-1).mStocks,
				dep.getData().get(i-1).mWorth,
				currStockPoint.getValue(StockDataPoint.ValueType.avg200),
				currStockPoint.getValue(StockDataPoint.ValueType.close_adjusted),
				dep.getData().get(i-1).mMoney);
			dep.getData().add(currDepPoint);
		}
	}

	public void deleteIrrelevantDates(StockResults res, LocalDate start, LocalDate end){
		if(start.isEqual(end)) return;
		if (start.isAfter(end)) {
			var temp = LocalDate.parse(end.toString(), DateTimeFormatter.ISO_DATE);
			end = start;
			start = temp;
		}

		for(var i = 0; i < res.getDataPoints().size(); ++i){
			var date = res.getDataPoints().get(i).mDateTime.toLocalDate();
			if((date.isBefore(start) && date.isBefore(end)) ||
				(date.isAfter(start) && date.isAfter(end))){
				res.getDataPoints().remove(i--);
			}
		}
	}

	public void drawChart(List<Depot> depoData, String symbol){
		XYChart chart = new XYChart(Environment.getDesktopWidth_Multiple(), Environment.getDesktopHeight_Multiple());
		chart.setTitle("Stock strategies");

		XYSeries series;
		var xAxis = new LinkedList<LinkedList<LocalDate>>();
		var yAxis = new LinkedList<LinkedList<Float>>();

		int currStrat = 0;
		for(var depo : depoData){
			xAxis.add(new LinkedList<>());
			yAxis.add(new LinkedList<>());

			for(var i = 0; i < depo.getData().size(); ++i){
				// TODO: continue implementing xChart
			}
		}
	}

	public void writeToDepotDb(List<Depot> depoData, String symbol){
		mBacktestDb = new BackTestingDatabase("localhost:3306", "root", "DuArschloch4", "baumbartstocks");

		try{
			mLogGui.loglnf("Connecting to backtest-db");
			mBacktestDb.connect();
			mBacktestDb.createDatabase();
			mBacktestDb.createTable(BackTestingDatabase._TABLE_NAME);
			for(var dep : depoData){
				mBacktestDb.updateDepots(dep, symbol);
			}
			mBacktestDb.disconnect();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return;
	}

	/**
	 * Returns a collection of <code>Depot</code>s objects, which vary in the strategy, that is used.
	 * @param res a reference to load the same stock as previously used in the program.
	 * @return
	 */
	public List<Depot> readFromDepotDb(StockResults res){
		var deps = new LinkedList<Depot>();

		mBacktestDb = new BackTestingDatabase("localhost", "root", "DuArschloch4", "baumbartstocks");

		try{
			mLogGui.loglnf("Connection to backtest-db");
			mBacktestDb.connect();
			mLogGui.loglnf("creating database, if necessary");
			mBacktestDb.createDatabase();
			mLogGui.loglnf("creating table if necessary");
			mBacktestDb.createTable(res.getTableName());
			mBacktestDb.initTable(res.getSymbol());
			mLogGui.loglnf("reading values from db");
			for(var strat : Depot.Strategy.values()) {
				try {
					var temp = mBacktestDb.getValues(res.getSymbol(), strat);
					deps.add(temp);
				}catch(Exception e){
					errlnf("mine");
					e.printStackTrace();
					System.exit(0);
				}
			}
			mLogGui.loglnf("disconnection from backtest-db");
			mBacktestDb.disconnect();
		}catch (SQLException e){
			e.printStackTrace();
		}finally{
			mBacktestDb = null;
		}

		return deps;
	}

	public StockResults fetchFromAPI(){
		StockResults res = new StockResults("ERROR");
		try {
			mLogGui.loglnf("Fetching from API");
			//var creds = CredentialLoader.loadApi(mInProduction);
			var creds = crapApiCred;

			mLogGui.loglnf("Processing fetched data");
			res = new ApiParser(creds).request(mSymbol, ApiParser.Function.TIME_SERIES_DAILY_ADJUSTED);
		}catch(IOException e){
			e.printStackTrace();
		}

		res.splitCorrection();
		res.calcAverage();

		return res;
	}

	public void updateStockValues(StockResults res){
		mLogGui.loglnf("Doing splitCorrection");
		res.splitCorrection();
		mLogGui.loglnf("Calculating average based on close_adjusted");
		res.calcAverage();
		return;
	}

	public void writeToStocksDB(StockResults res){
		var writingWith = res.clone();

		mStockDb = new StockDatabase("localhost", "root", "DuArschloch4", "baumbartstocks");

		try {
			mLogGui.loglnf("Connection to stocks-db");
			mStockDb.connect();
			mLogGui.loglnf("creating database, if necessary");
			mStockDb.createDatabase();
			mLogGui.loglnf("creating table if necessary");
			mStockDb.createTable(writingWith.getTableName());
			mLogGui.loglnf("inserting/Updating stock values on db");
			mStockDb.insertOrUpdateStock(writingWith);
			mLogGui.loglnf("disconnection from stocks-db");
			mStockDb.disconnect();
		}catch(SQLException e){
			errlnf("go away, exception, i'm scared of you");
			e.printStackTrace();
		}finally{
			mStockDb = null;
		}
	}

}
