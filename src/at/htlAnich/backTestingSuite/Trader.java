package at.htlAnich.backTestingSuite;

import at.htlAnich.backTestingSuite.badCode.DamnShit;
import at.htlAnich.stockUpdater.StockDataPoint;
import at.htlAnich.stockUpdater.StockResults;

import java.time.LocalDate;
import java.util.ArrayList;

import static at.htlAnich.tools.BaumbartLogger.*;

public final class Trader {
	private Trader(){ }

	public static void createNewDepotPoint(Depot dep, Depot.Point yesterday, StockDataPoint today){
		var newDepPoint = new Depot.Point(
			today.mDateTime.toLocalDate(),
			Depot.Point.BuyFlag.UNCHANGED,
			0,
			yesterday.mStocks,
			yesterday.mWorth,
			today.getValue(StockDataPoint.ValueType.avg200),
			today.getValue(StockDataPoint.ValueType.close_adjusted),
			yesterday.mMoney
		);
		dep.addPoint(newDepPoint);
	}

	public static boolean checkDates(LocalDate today, LocalDate yesterday){
		if(today.isBefore(yesterday)){
			errlnf("Excuse me? WTF?!");
			return false;
		}
		if(today.isEqual(yesterday)){
			errlnf("Excuse me?! What the actual fuck?!");
			return false;
		}
		return true;
	}

	public static void trade(Depot dep, StockResults ref){
		switch(dep.getStrategy()){
			case avg200:
				DamnShit.mLogGui.loglnf("Trading 200-average");
				trade_avg200(dep, ref);
				break;
			case avg200_3percent:
				DamnShit.mLogGui.loglnf("Trading 200-average + 3");
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
				DamnShit.mLogGui.loglnf("no Trade at all possible");
				break;
		}
	}

	public static void trade_avg200(Depot dep, StockResults stockRes){
		// go through each date of stockRes, because it holds all needed values from
		// startDate and endDate (both including)
		// if dep does not have a day for the corresponding date, create a new Depot.Point
		// at this date and then see, whether a trade shall happen or not
		for(var i = 1; i < stockRes.getDataPoints().size(); ++i) {
			// for easier access
			var todaysStock = stockRes.getDataPoints().get(i);
			var yesterdaysDepot = dep.getData().get(i - 1);
			if(!checkDates(todaysStock.mDateTime.toLocalDate(), yesterdaysDepot.mDate)) continue;

			// create new depotPoint if there is none
			if (i >= dep.getData().size()) {
				createNewDepotPoint(dep, yesterdaysDepot, todaysStock);
			}
			var todaysDepot = dep.getData().get(i);

			// lesss go trading
			// round today.close to .00; just for comparison
			var todayClose = (float) ((int) (todaysStock.getValue(StockDataPoint.ValueType.close_adjusted) * 100.0f)) / 100.0f;
			// round today.avg200 to .00; just for comparison
			var todayAvg = (float) ((int) (todaysStock.getValue(StockDataPoint.ValueType.avg200) * 100.0f)) / 100.0f;

			int maxStocksCanBuy = (int) (todaysDepot.mMoney / todaysDepot.mClose);
			boolean canBuy = maxStocksCanBuy >= 1;
			boolean canSell = todaysDepot.mStocks >= 1;

			if (!canBuy && !canSell) {
				DamnShit.mLogGui.loglnf("Cannot buy/sell on %s", todaysStock.mDateTime.toLocalDate().toString());
			}

			final var BUY_AMOUNT = maxStocksCanBuy;
			final var SELL_AMOUNT = todaysDepot.mStocks;
			// buy BUY_AMOUNT if today.close>today.avg200
			if (canBuy && todayClose > todayAvg) {
				todaysDepot.mBuyAmount = +BUY_AMOUNT;
				todaysDepot.mFlag = Depot.Point.BuyFlag.BUY;
				todaysDepot.mMoney -= BUY_AMOUNT * todaysDepot.mClose;
				todaysDepot.mStocks += BUY_AMOUNT;
			}
			// sell BUY_AMOUNT if today.close<today.avg200
			else if (canSell && todayAvg < todayClose) {
				todaysDepot.mBuyAmount = -SELL_AMOUNT;
				todaysDepot.mFlag = Depot.Point.BuyFlag.SELL;
				todaysDepot.mMoney += SELL_AMOUNT * todaysDepot.mClose;
				todaysDepot.mStocks -= SELL_AMOUNT;
			}
			// do nothing if today.close==today.avg200
			else {
				var date = todaysStock.mDateTime.toLocalDate();
				DamnShit.mLogGui.loglnf("Not traded today. Today's date is: %d %s %d.",
					date.getYear(), date.getMonth(), date.getDayOfMonth());

				// just to be sure
				todaysDepot.mBuyAmount = 0;
				todaysDepot.mFlag = Depot.Point.BuyFlag.UNCHANGED;
				todaysDepot.mMoney -= 0;
			}
			todaysDepot.mWorth = todaysDepot.mClose * todaysDepot.mStocks + todaysDepot.mMoney;
			dep.getData().set(i, todaysDepot);
		}
	}
	public static void trade_avg200_3Percent(Depot dep, StockResults stockRes){
		// go through each date of stockRes, because it holds all needed values from
		// startDate and endDate (both including)
		// if dep does not have a day for the corresponding date, create a new Depot.Point
		// at this date and then see, whether a trade shall happen or not
		for(var i = 1; i < stockRes.getDataPoints().size(); ++i){
			// for easier access
			var todaysStock = stockRes.getDataPoints().get(i);
			var yesterdaysDepot = dep.getData().get(i-1);
			if(!checkDates(todaysStock.mDateTime.toLocalDate(), yesterdaysDepot.mDate)) continue;

			// create new depotPoint if there is none
			if(i >= dep.getData().size()){
				createNewDepotPoint(dep, yesterdaysDepot, todaysStock);
			}
			var todaysDepot = dep.getData().get(i);

			// lesss go trading
			// round today.close to .00; just for comparison
			var todayClose = (float)((int)(todaysStock.getValue(StockDataPoint.ValueType.close_adjusted) * 100.0f))/100.0f;
			// round today.avg200 to .00; just for comparison
			var todayAvg = (float)((int)(todaysStock.getValue(StockDataPoint.ValueType.avg200) * 100.0f))/100.0f;

			int maxStocksCanBuy = (int)(todaysDepot.mMoney / todaysDepot.mClose);
			boolean canBuy = maxStocksCanBuy >= 1;
			boolean canSell = todaysDepot.mStocks >= 1;

			if(!canBuy && !canSell){
				DamnShit.mLogGui.loglnf("Cannot buy/sell on %s", todaysStock.mDateTime.toLocalDate().toString());
			}

			final var BUY_AMOUNT = maxStocksCanBuy;
			final var SELL_AMOUNT = todaysDepot.mStocks;
			// buy BUY_AMOUNT if today.close>today.avg200
			if(canBuy && todayClose > todayAvg*1.03){
				todaysDepot.mBuyAmount = +BUY_AMOUNT;
				todaysDepot.mFlag = Depot.Point.BuyFlag.BUY;
				todaysDepot.mMoney -= BUY_AMOUNT*todaysDepot.mClose;
				todaysDepot.mStocks += BUY_AMOUNT;
			}
			// sell BUY_AMOUNT if today.close<today.avg200
			else if(canSell && todayAvg*0.97 < todayClose){
				todaysDepot.mBuyAmount = -SELL_AMOUNT;
				todaysDepot.mFlag = Depot.Point.BuyFlag.SELL;
				todaysDepot.mMoney += SELL_AMOUNT*todaysDepot.mClose;
				todaysDepot.mStocks -= SELL_AMOUNT;
			}
			// do nothing if today.close==today.avg200
			else{
				var date = todaysStock.mDateTime.toLocalDate();
				DamnShit.mLogGui.loglnf("Not traded today. Today's date is: %d %s %d.",
					date.getYear(), date.getMonth(), date.getDayOfMonth());

				// just to be sure
				todaysDepot.mBuyAmount = 0;
				todaysDepot.mFlag = Depot.Point.BuyFlag.UNCHANGED;
				todaysDepot.mMoney -= 0;
			}
			todaysDepot.mWorth = todaysDepot.mClose*todaysDepot.mStocks + todaysDepot.mMoney;
			dep.getData().set(i, todaysDepot);
		}
	}
	public static void trade_avg200_false(Depot dep, StockResults stockRes){
		// go through each date of stockRes, because it holds all needed values from
		// startDate and endDate (both including)
		// if dep does not have a day for the corresponding date, create a new Depot.Point
		// at this date and then see, whether a trade shall happen or not
		for(var i = 1; i < stockRes.getDataPoints().size(); ++i){
			// for easier access
			var todaysStock = stockRes.getDataPoints().get(i);
			var yesterdaysDepot = dep.getData().get(i-1);
			if(!checkDates(todaysStock.mDateTime.toLocalDate(), yesterdaysDepot.mDate)) continue;

			// create new depotPoint if there is none
			if(i >= dep.getData().size()){
				createNewDepotPoint(dep, yesterdaysDepot, todaysStock);
			}
			var todaysDepot = dep.getData().get(i);

			// lesss go trading
			// round today.close to .00; just for comparison
			var todayClose = (float)((int)(todaysStock.getValue(StockDataPoint.ValueType.close_adjusted) * 100.0f))/100.0f;
			// round today.avg200 to .00; just for comparison
			var todayAvg = (float)((int)(todaysStock.getValue(StockDataPoint.ValueType.avg200) * 100.0f))/100.0f;

			int maxStocksCanBuy = (int)(todaysDepot.mMoney / todaysDepot.mClose);
			boolean canBuy = maxStocksCanBuy >= 1;
			boolean canSell = todaysDepot.mStocks >= 1;

			if(!canBuy && !canSell){
				DamnShit.mLogGui.loglnf("Cannot buy/sell on %s", todaysStock.mDateTime.toLocalDate().toString());
			}

			final var BUY_AMOUNT = maxStocksCanBuy;
			final var SELL_AMOUNT = todaysDepot.mStocks;
			// buy BUY_AMOUNT if today.close>today.avg200
			if(canBuy && todayClose < todayAvg){
				todaysDepot.mBuyAmount = +BUY_AMOUNT;
				todaysDepot.mFlag = Depot.Point.BuyFlag.BUY;
				todaysDepot.mMoney -= BUY_AMOUNT*todaysDepot.mClose;
				todaysDepot.mStocks += BUY_AMOUNT;
			}
			// sell BUY_AMOUNT if today.close<today.avg200
			else if(canSell && todayAvg > todayClose){
				todaysDepot.mBuyAmount = -SELL_AMOUNT;
				todaysDepot.mFlag = Depot.Point.BuyFlag.SELL;
				todaysDepot.mMoney += SELL_AMOUNT*todaysDepot.mClose;
				todaysDepot.mStocks -= SELL_AMOUNT;
			}
			// do nothing if today.close==today.avg200
			else{
				var date = todaysStock.mDateTime.toLocalDate();
				DamnShit.mLogGui.loglnf("Not traded today. Today's date is: %d %s %d.",
					date.getYear(), date.getMonth(), date.getDayOfMonth());

				// just to be sure
				todaysDepot.mBuyAmount = 0;
				todaysDepot.mFlag = Depot.Point.BuyFlag.UNCHANGED;
				todaysDepot.mMoney -= 0;
			}
			todaysDepot.mWorth = todaysDepot.mClose*todaysDepot.mStocks + todaysDepot.mMoney;
			dep.getData().set(i, todaysDepot);
		}
	}
	public static void trade_buyAndHold(Depot dep, StockResults stockRes){

		// go through each date of stockRes, because it holds all needed values from
		// startDate and endDate (both including)
		// if dep does not have a day for the corresponding date, create a new Depot.Point
		// at this date and then see, whether a trade shall happen or not
		for(var i = 1; i < stockRes.getDataPoints().size(); ++i){
			// for easier access
			var todaysStock = stockRes.getDataPoints().get(i);
			var yesterdaysDepot = dep.getData().get(i-1);
			if(!checkDates(todaysStock.mDateTime.toLocalDate(), yesterdaysDepot.mDate)) continue;

			// create new depotPoint if there is none
			if(i >= dep.getData().size()){
				createNewDepotPoint(dep, yesterdaysDepot, todaysStock);
			}
			var todaysDepot = dep.getData().get(i);

			// lesss go trading
			// SELL on end
			if(i <= 1){
				final var SELL_AMOUNT = todaysDepot.mStocks;
				todaysDepot.mBuyAmount = -SELL_AMOUNT;
				todaysDepot.mFlag = Depot.Point.BuyFlag.SELL;
				todaysDepot.mMoney += SELL_AMOUNT*todaysDepot.mClose;
				todaysDepot.mStocks -= SELL_AMOUNT;
			}
			// BUY on beginning
			else if(i >= stockRes.getDataPoints().size()-1){
				final var BUY_AMOUNT = (int)(todaysDepot.mMoney / todaysDepot.mClose);
				todaysDepot.mBuyAmount = +BUY_AMOUNT;
				todaysDepot.mFlag = Depot.Point.BuyFlag.BUY;
				todaysDepot.mMoney -= BUY_AMOUNT*todaysDepot.mClose;
				todaysDepot.mStocks += BUY_AMOUNT;

			}
			// do nothing if neither first or last entry
			else{
				var date = todaysStock.mDateTime.toLocalDate();
				DamnShit.mLogGui.loglnf("Not traded today. Today's date is: %d %s %d.",
					date.getYear(), date.getMonth(), date.getDayOfMonth());

				// just to be sure
				todaysDepot.mBuyAmount = 0;
				todaysDepot.mFlag = Depot.Point.BuyFlag.UNCHANGED;
				todaysDepot.mMoney -= 0;
			}
			todaysDepot.mWorth = todaysDepot.mClose*todaysDepot.mStocks + todaysDepot.mMoney;
			dep.getData().set(i, todaysDepot);
		}
	}
}
