package at.htlAnich.backTestingSuite;

import at.htlAnich.backTestingSuite.badCode.DamnShit;
import at.htlAnich.stockUpdater.StockDataPoint;
import at.htlAnich.stockUpdater.StockResults;

import java.util.List;

import static at.htlAnich.tools.BaumbartLogger.logf;
import static at.htlAnich.tools.BaumbartLogger.loglnf;

public final class Trader {
	private Trader(){ }

	public static void trade(Depot dep, StockResults ref){


		switch(dep.getStrategy()){
			case avg200:
				DamnShit.mLogGui.loglnf("Trading 200-average");
				trade_avg200(dep, ref);
				break;
			case avg200_3percent:
				DamnShit.mLogGui.loglnf("Trading 200-average + 3%");
				trade_avg200_3Percent(dep, ref);
				break;
			case avg200_false:
				DamnShit.mLogGui.loglnf("Trading 200-average false");
				trade_avg200_false(dep, ref);
				break;
			case buyAndHold:
				DamnShit.mLogGui.loglnf("Trading buy and hold");
				trade_buyAndHold(dep, ref);
				break;
			case NONE:
			default:
				DamnShit.mLogGui.loglnf("nothing");
				break;
		}
	}

	public static void trade_avg200(Depot dep, StockResults stockRes){}
	public static void trade_avg200_3Percent(Depot dep, StockResults stockRes){}
	public static void trade_avg200_false(Depot dep, StockResults stockRes){}
	public static void trade_buyAndHold(Depot dep, StockResults stockRes){}
}
