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
import org.knowm.xchart.style.XYStyler;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.knowm.xchart.style.theme.XChartTheme;

import javax.swing.*;
import java.awt.*;
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

	public Depot.Point getFirstDepotEntry(Depot.Strategy strat){
		var point = new Depot.Point();
		if(mBacktestDb == null){
			errlnf("Who eliminated the database?");
			return point;
		}

		try{
			mBacktestDb.connect();
			point = mBacktestDb.getOldestDepotPoint(strat, mSymbol);
			mBacktestDb.disconnect();
		}catch (SQLException e){
			e.printStackTrace();
		}

		return point;
	}

	public Depot.Point getLastDepotEntry(Depot.Strategy strat){
		var point = new Depot.Point();
		if(mBacktestDb == null){
			errlnf("Who eliminated the database?");
			return point;
		}

		try{
			mBacktestDb.connect();
			point = mBacktestDb.getNewestDepotPoint(strat, mSymbol);
			mBacktestDb.disconnect();
		}catch (SQLException e){
			e.printStackTrace();
		}

		return point;
	}

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
		// write values to stock_data(DB)
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
				/*if(!dep.getStrategy().equals(Depot.Strategy.NONE)) {
					for (var i = 0; i < dep.getData().size(); ++i) {
						logf(dep.getData().get(i).mDate.toString());
						loglnf(" money: %.2f, stocks: %d, close: %.2f, avg: %.2f", dep.getData().get(i).mMoney, dep.getData().get(i).mStocks, dep.getData().get(i).mClose, dep.getData().get(i).mAvg200);
					}

					// insert DEBUG-linechart of close and avg200 - done
					XYChart chart = new XYChartBuilder().height(200).width(200).title(mSymbol).xAxisTitle("Date").yAxisTitle("Value").build();
					chart.getStyler().setDatePattern("yyyy-mm-dd");
					chart.getStyler().setDecimalPattern("#0.00");
					chart.getStyler().setLocale(Locale.getDefault());

					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

					var close = new ArrayList<Float>();
					var dates = new ArrayList<Date>();
					var avg = new ArrayList<Float>();

					for(var i = 0; i < dep.getData().size(); ++i){
						close.add(dep.getData().get(i).mClose);
						avg.add(dep.getData().get(i).mAvg200);

						Date date = null;
						try{
							date = dateFormat.parse(dep.getData().get(i).mDate.format(DateTimeFormatter.ISO_DATE));
						}catch(ParseException e){
							e.printStackTrace();
						}
						dates.add(date);
					}
					chart.addSeries("close", dates, close).setMarker(SeriesMarkers.NONE);
					chart.addSeries("avg200", dates, avg).setMarker(SeriesMarkers.NONE);
					new SwingWrapper<XYChart>(chart).displayChart();
				}
				waitForKeyPress();
				System.exit(0);*/
			}
		}

		// write new values to backtesting_depot
		writeToDepotDb(depotData, stockData.getSymbol());

		// create diagram of backtesting-strategies
		drawChart(depotData, stockData.getSymbol());

		return depotData.get(0);
	}

	public StockResults readFromStockDb(String symbol, LocalDate start, LocalDate end){
		mStockDb = new StockDatabase(crapDbCred);
		var resOut = new StockResults("ERROR");
		try{
			mLogGui.loglnf("Connecting to stockresults-db");
			mStockDb.connect();
			mStockDb.createDatabase();
			mStockDb.createTable(resOut.getTableName());
			resOut = mStockDb.getValues(symbol, start, end);
			mStockDb.calcAvg(resOut);

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
			.xAxisTitle("Dates").yAxisTitle("Worth").build();
		chart.getStyler().setDatePattern("yyyy-mm-dd");
		chart.getStyler().setDecimalPattern("#0.00");
		chart.getStyler().setLocale(Locale.getDefault());
		chart.getStyler().setZoomEnabled(true);
		chart.getStyler().setZoomResetByButton(true);
		chart.getStyler().setZoomResetByDoubleClick(false);
		chart.getStyler().setZoomResetButtomPosition(XYStyler.ButtonPosition.InsideNE);

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		for(var depot : depoData){
			if(depot.getStrategy().equals(Depot.Strategy.NONE)){
				continue;
			}
			var money = new ArrayList<Float>();
			var dates = new ArrayList<Date>();
			for(var p : depot.getData()){
				// add money - yAxis-values
				money.add(p.mWorth);

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
		addButtons(new SwingWrapper<XYChart>(chart).displayChart(), chart);
	}

	public void addButtons(JFrame frame, XYChart chart){
		frame.setVisible(false);
		var menubar = new JMenuBar();
		var menu = new JMenu("Show");
		for(var k : chart.getSeriesMap().keySet()){
			var series = chart.getSeriesMap().get(k);

			var menuItem = new JMenuItem(k);
			menuItem.addActionListener(e ->{
				series.setEnabled(!series.isEnabled());
				series.setShowInLegend(!series.isShowInLegend());
				var r = series.getLineColor().getRed();
				var g = series.getLineColor().getGreen();
				var b = series.getLineColor().getBlue();
				if(series.isEnabled()){
					series.setLineColor(new Color(r, g, b, 1.0f));
				}else{
					series.setLineColor(new Color(r, g, b, 0.3f));
				}
			});
			menu.add(menuItem);
		}
		menubar.add(menu);
		frame.setJMenuBar(menubar);
		frame.setVisible(true);
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
					firstStockDate.getValue(
						(BackTesting.usingAdjusted()) ?
							StockDataPoint.ValueType.close_adjusted :
						StockDataPoint.ValueType.close),
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
		//res.calcAverage();

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

		mStockDb = new StockDatabase("localhost", "root", "DuArschloch4", "baumbartstocks");

		try {
			mLogGui.loglnf("Connection to stocks-db");
			mStockDb.connect();
			mLogGui.loglnf("creating database, if necessary");
			mStockDb.createDatabase();
			mLogGui.loglnf("creating table if necessary");
			mStockDb.createTable(res.getTableName());
			mLogGui.loglnf("inserting/Updating stock values on db");
			mStockDb.insertOrUpdateStock(res);
			mLogGui.loglnf("updating average stock values");
			mStockDb.calcAvg(res);
			mLogGui.loglnf("inserting/Updating averages on db");
			mStockDb.insertOrUpdateStock(res);
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
