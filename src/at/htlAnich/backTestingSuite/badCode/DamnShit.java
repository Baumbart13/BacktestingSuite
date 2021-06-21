package at.htlAnich.backTestingSuite.badCode;

import at.htlAnich.backTestingSuite.BackTesting;
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
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ArrayList;
import java.util.Locale;

import static at.htlAnich.tools.BaumbartLogger.*;

public class DamnShit {
	protected String mSymbol;
	protected float mTotalMoney;
	protected float mMoneyPerSymbol;
	protected boolean mInProduction;
	protected StockDatabase mStockDb;
	protected BackTestingDatabase mBacktestDb;

	public static BaumbartLoggerGUI mLogGui = new BaumbartLoggerGUI("Detailed Log");

	public DamnShit(String symbol, float totalMoney, boolean inProduction){
		mSymbol = symbol;
		mTotalMoney = totalMoney;
		mInProduction = inProduction;
		mMoneyPerSymbol = mTotalMoney;
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
		stockData = readFromStockDb(stockData.getSymbol(), start, end);
		//stockData.sort();

		// get depotData(Depot-Object) from database
		//var depotData = readFromDepotDb(stockData);
		var depotData = createNewDepots(stockData);

		// trade on depotData
		// ATTENTION:	don't forget splitCorrection, which has to
		// 		be done backwards, but trading is forwards!!
		for(var dep : depotData){
			if(dep.getStrategy().equals(Depot.Strategy.NONE)){
				continue;
			}
			dep.sort();
			Trader.trade(dep, stockData);
			loglnf("Current Strat: %s", dep.getStrategy());
			if(BackTesting.DEBUG()){
				if(!dep.getStrategy().equals(Depot.Strategy.NONE)) {
					for (var i = 0; i < dep.getData().size(); ++i) {
						logf(dep.getData().get(i).mDate.toString());
						loglnf(" money: %.2f, stocks: %d", dep.getData().get(i).mMoney, dep.getData().get(i).mStocks);
					}
					//System.exit(0);
				}
			}
		}
		System.exit(0);

		// write new values to backtesting_depot
		writeToDepotDb(depotData, stockData.getSymbol());

		// create diagram of backtesting-strategies
		drawChart(depotData, stockData.getSymbol());

		return depotData.get(0);
	}

	public StockResults readFromStockDb(String symbol, LocalDate start, LocalDate end){
		mStockDb = new StockDatabase(crapDbCred.host(), crapDbCred.user(), crapDbCred.password(), crapDbCred.database());
		var resOut = new StockResults("ERROR");
		try{
			mLogGui.loglnf("Connecting to stockresults-db");
			mStockDb.connect();
			mStockDb.createDatabase();
			mStockDb.createTable(resOut.getTableName());
			resOut = mStockDb.getValues(symbol, start, end);

			mStockDb.disconnect();
		}catch (SQLException e){
			e.printStackTrace();
		}finally{
			mStockDb = null;
		}

		return resOut;
	}

	public void updateDepotValues(Depot dep, StockResults stockRes){

		// create all points new, no matter what
		// the point with date of stockRes.OldestDay has a unique value with (initMoney/strategyAmount)
		// because a single dep-object is for 1 stock and all of its strategies

		// first date
		if(dep.getData().size() <= 0) {
			var firstDepPoint = new Depot.Point(
				stockRes.getOldestDate().toLocalDate(),
				Depot.Point.BuyFlag.UNCHANGED,
				0,
				0,
				0.0f,
				stockRes.getDataPoints().get(0).getValue(StockDataPoint.ValueType.avg200),
				stockRes.getDataPoints().get(0).getValue(StockDataPoint.ValueType.close_adjusted),
				mMoneyPerSymbol
			);
			dep.addPoint(firstDepPoint);
		}

	}

	/**
	 * All entries, that are not between <code>start</code> and <code>end</code> (both including) will be removed
	 * and then returned by this method.
	 * @param res from where the entries will be chosen.
	 * @param start the first date, that can be inside of the returned object.
	 * @param end the last date, that can be inside of the returned object.
	 * @return A <code>StockResults</code>-object only holding the wanted dates (<code>start</code> until
	 * <code>end</code>, both including).
	 */
	public StockResults deleteIrrelevantDates(StockResults res, LocalDate start, LocalDate end){
		var out = new StockResults(res.getSymbol());

		// only one date ;)
		if(start.isEqual(end)){
			for(var i = 0; i < res.getDataPoints().size(); ++i){
				if(res.getDataPoints().get(i).mDateTime.equals(start.atStartOfDay())){
					out.addDataPoint(res.getDataPoints().get(i));
					return out;
				}
			}
		}
		// if start and end are given the other way round, swap 'em
		if (start.isAfter(end)) {
			var temp = LocalDate.parse(end.toString(), DateTimeFormatter.ISO_DATE);
			end = start;
			start = temp;
		}


		for(var i = 0; i < res.getDataPoints().size(); ++i){
			var date = res.getDataPoints().get(i).mDateTime.toLocalDate();
			if(date.isAfter(start) && date.isBefore(end)){
				out.addDataPoint(res.getDataPoints().get(i));
			}
		}

		out.setNewestDate(out.getDataPoints().get(0).mDateTime);
		out.setOldestDate(out.getDataPoints().get(out.getDataPoints().size()-1).mDateTime);
		return out;
	}

	public void drawChart(ArrayList<Depot> depoData, String symbol){
		XYChart chart = new XYChartBuilder().width(Environment.getDesktopWidth_Multiple())
			.height(Environment.getDesktopHeight_Multiple()).title(symbol)
			.xAxisTitle("Dates").yAxisTitle("Money").build();
		chart.getStyler().setDatePattern("yyyy-mm-dd");
		chart.getStyler().setDecimalPattern("#0.00");
		chart.getStyler().setLocale(Locale.getDefault());

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		for(var depot : depoData){
			if(depot.getStrategy().equals(Depot.Strategy.NONE)){
				continue;
			}
			var money = new ArrayList<Float>();
			var dates = new ArrayList<Date>();
			for(var p : depot.getData()){
				// add money - yAxis-values
				money.add(p.mMoney);

				// add dates - xAxis-Values
				Date date = null;
				try {
					date = dateFormat.parse(p.mDate.format(DateTimeFormatter.ISO_DATE));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				dates.add(date);
			}
			chart.addSeries(depot.getStrategy().toString(), dates, money).setMarker(SeriesMarkers.NONE);
		}
		new SwingWrapper<XYChart>(chart).displayChart();
	}

	public void writeToDepotDb(ArrayList<Depot> depoData, String symbol){
		mBacktestDb = new BackTestingDatabase(crapDbCred.host(), crapDbCred.user(), crapDbCred.password(), crapDbCred.database());

		try{
			mLogGui.loglnf("Connecting to backtest-db");
			mBacktestDb.connect();
			mBacktestDb.createDatabase();
			mBacktestDb.createTable(BackTestingDatabase._TABLE_NAME);
			/*for(var dep : depoData){
				mBacktestDb.updateDepots(dep, symbol);
			}*/
			mBacktestDb.disconnect();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return;
	}

	public ArrayList<Depot> createNewDepots(StockResults res){
		var deps = new ArrayList<Depot>();
		var firstStockDate = res.getDataPoints().get(0);

		for(var strat : Depot.Strategy.values()){
			if(strat.equals(Depot.Strategy.NONE)){
				continue;
			}
			deps.add(new Depot(
				mSymbol,
				strat,
				new Depot.Point(
					firstStockDate.mDateTime.toLocalDate(),
					Depot.Point.BuyFlag.UNCHANGED,
					0,
					0,
					0.0f,
					firstStockDate.getValue(StockDataPoint.ValueType.avg200),
					// use close, so splitCorrection can be done
					firstStockDate.getValue(StockDataPoint.ValueType.close_adjusted),
					mMoneyPerSymbol
				)
			));
		}
		return deps;
	}

	/**
	 * Returns a collection of <code>Depot</code>s objects, which vary in the strategy, that is used.
	 * @param res a reference to load the same stock as previously used in the program.
	 * @return
	 */
	public ArrayList<Depot> readFromDepotDb(StockResults res){
		var deps = new ArrayList<Depot>();

		mBacktestDb = new BackTestingDatabase("localhost", "root", "DuArschloch4", "baumbartstocks");

		try{
			mLogGui.loglnf("Connection to backtest-db");
			mBacktestDb.connect();
			mLogGui.loglnf("creating database, if necessary");
			mBacktestDb.createDatabase();
			mLogGui.loglnf("creating table if necessary");
			mBacktestDb.createTable(res.getTableName());
			mBacktestDb.initTable(res.getSymbol(), res.getOldestDate().toLocalDate());
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
