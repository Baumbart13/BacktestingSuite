package at.htlAnich.backTestingSuite;

import at.htlAnich.backTestingSuite.badCode.DamnShit;
import at.htlAnich.stockUpdater.StockDataPoint;
import at.htlAnich.stockUpdater.StockResults;

import java.util.List;

import static at.htlAnich.tools.BaumbartLogger.logf;
import static at.htlAnich.tools.BaumbartLogger.loglnf;

public final class Trader {
	private Trader(){ }

	public static final int BUY_AMOUNT = 10;

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
		for(var i = 0; i < stockRes.getDataPoints().size(); ++i){
			// for easier access
			var todaysStock = stockRes.getDataPoints().get(i);
			var yesterdaysDepot = dep.getData().get(
				// if i==0, we would be fucked up, but we can use a little trick
				// by just using the first entry as yesterday, where we have no yesterday
				(i==0)?i:i-1 // i love the if's we can make via the ternary operator
			);

			// create new depotPoint if there is none
			if(i >= dep.getData().size()){
				createNewDepotPoint(dep, yesterdaysDepot, todaysStock);
			}
			var todaysDepot = dep.getData().get(i);

			// lesss go trading
			// round today.close to .00
			int todayClose = (int)(todaysStock.getValue(StockDataPoint.ValueType.close_adjusted) * 100.0f);
			// round today.avg200 to .00
			int todayAvg = (int)(todaysStock.getValue(StockDataPoint.ValueType.avg200) * 100.0f);

			int maxStocksCanBuy = (int)(todaysDepot.mMoney / todaysDepot.mClose);
			boolean canBuy = maxStocksCanBuy >= BUY_AMOUNT;
			boolean canSell = todaysDepot.mStocks >= BUY_AMOUNT;

			if(!canBuy && !canSell){
				DamnShit.mLogGui.loglnf("Cannot buy/sell on %s", todaysStock.mDateTime.toLocalDate().toString());
			}


			// buy BUY_AMOUNT if today.close>today.avg200
			if(canBuy && todayClose > todayAvg){
				todaysDepot.mBuyAmount += BUY_AMOUNT;
				todaysDepot.mFlag = Depot.Point.BuyFlag.BUY;
				todaysDepot.mMoney -= BUY_AMOUNT*todayClose;
			}
			// sell BUY_AMOUNT if today.close<today.avg200
			else if(canSell && todayAvg < todayClose){
				todaysDepot.mBuyAmount -= BUY_AMOUNT;
				todaysDepot.mFlag = Depot.Point.BuyFlag.SELL;
				todaysDepot.mMoney += BUY_AMOUNT*todayClose;
			}
			// do nothing if today.close==today.avg200
			else{
				DamnShit.mLogGui.loglnf("Not traded today. Today's date is: %s", todaysStock.mDateTime.toLocalDate().toString());

				// just to be sure
				todaysDepot.mBuyAmount += 0;
				todaysDepot.mFlag = Depot.Point.BuyFlag.UNCHANGED;
				todaysDepot.mMoney -= 0;
			}
			todaysDepot.mWorth = todayClose*todaysDepot.mStocks;
		}
	}
	public static void trade_avg200_3Percent(Depot dep, StockResults stockRes){
		// go through each date of stockRes, because it holds all needed values from
		// startDate and endDate (both including)
		// if dep does not have a day for the corresponding date, create a new Depot.Point
		// at this date and then see, whether a trade shall happen or not
		for(var i = 0; i < stockRes.getDataPoints().size(); ++i){
			// for easier access
			var todaysStock = stockRes.getDataPoints().get(i);
			var yesterdaysDepot = dep.getData().get(
				// if i==0, we would be fucked up, but we can use a little trick
				// by just using the first entry as yesterday, where we have no yesterday
				(i==0)?i:i-1 // i love the if's we can make via the ternary operator
			);

			// create new depotPoint if there is none
			if(i >= dep.getData().size()){
				createNewDepotPoint(dep, yesterdaysDepot, todaysStock);
			}
			var todaysDepot = dep.getData().get(i);

			// lesss go trading
			// round today.close to .00
			int todayClose = (int)(todaysStock.getValue(StockDataPoint.ValueType.close_adjusted) * 100.0f);
			// round today.avg200 to .00
			int todayAvg = (int)(todaysStock.getValue(StockDataPoint.ValueType.avg200) * 100.0f);

			int maxStocksCanBuy = (int)(todaysDepot.mMoney / todaysDepot.mClose);
			boolean canBuy = maxStocksCanBuy >= BUY_AMOUNT;
			boolean canSell = todaysDepot.mStocks >= BUY_AMOUNT;

			if(!canBuy && !canSell){
				DamnShit.mLogGui.loglnf("Cannot buy/sell on %s", todaysStock.mDateTime.toLocalDate().toString());
			}


			// buy BUY_AMOUNT if today.close>today.avg200
			if(canBuy && todayClose > todayAvg*1.03){
				todaysDepot.mBuyAmount += BUY_AMOUNT;
				todaysDepot.mFlag = Depot.Point.BuyFlag.BUY;
				todaysDepot.mMoney -= BUY_AMOUNT*todayClose;
			}
			// sell BUY_AMOUNT if today.close<today.avg200
			else if(canSell && todayAvg < todayClose*1.03){
				todaysDepot.mBuyAmount -= BUY_AMOUNT;
				todaysDepot.mFlag = Depot.Point.BuyFlag.SELL;
				todaysDepot.mMoney += BUY_AMOUNT*todayClose;
			}
			// do nothing if today.close==today.avg200
			else{
				DamnShit.mLogGui.loglnf("Not traded today. Today's date is: %s", todaysStock.mDateTime.toLocalDate().toString());

				// just to be sure
				todaysDepot.mBuyAmount += 0;
				todaysDepot.mFlag = Depot.Point.BuyFlag.UNCHANGED;
				todaysDepot.mMoney -= 0;
			}
			todaysDepot.mWorth = todayClose*todaysDepot.mStocks;
		}
	}
	public static void trade_avg200_false(Depot dep, StockResults stockRes){
		// go through each date of stockRes, because it holds all needed values from
		// startDate and endDate (both including)
		// if dep does not have a day for the corresponding date, create a new Depot.Point
		// at this date and then see, whether a trade shall happen or not
		for(var i = 0; i < stockRes.getDataPoints().size(); ++i){
			// for easier access
			var todaysStock = stockRes.getDataPoints().get(i);
			var yesterdaysDepot = dep.getData().get(
				// if i==0, we would be fucked up, but we can use a little trick
				// by just using the first entry as yesterday, where we have no yesterday
				(i==0)?i:i-1 // i love the if's we can make via the ternary operator
			);

			// create new depotPoint if there is none
			if(i >= dep.getData().size()){
				createNewDepotPoint(dep, yesterdaysDepot, todaysStock);
			}
			var todaysDepot = dep.getData().get(i);

			// lesss go trading
			// round today.close to .00
			int todayClose = (int)(todaysStock.getValue(StockDataPoint.ValueType.close_adjusted) * 100.0f);
			// round today.avg200 to .00
			int todayAvg = (int)(todaysStock.getValue(StockDataPoint.ValueType.avg200) * 100.0f);

			int maxStocksCanBuy = (int)(todaysDepot.mMoney / todaysDepot.mClose);
			boolean canBuy = maxStocksCanBuy >= BUY_AMOUNT;
			boolean canSell = todaysDepot.mStocks >= BUY_AMOUNT;

			if(!canBuy && !canSell){
				DamnShit.mLogGui.loglnf("Cannot buy/sell on %s", todaysStock.mDateTime.toLocalDate().toString());
			}


			// buy BUY_AMOUNT if today.close<today.avg200
			if(canBuy && todayClose < todayAvg){
				todaysDepot.mBuyAmount += BUY_AMOUNT;
				todaysDepot.mFlag = Depot.Point.BuyFlag.BUY;
				todaysDepot.mMoney -= BUY_AMOUNT*todayClose;
			}
			// sell BUY_AMOUNT if today.close>today.avg200
			else if(canSell && todayAvg > todayClose){
				todaysDepot.mBuyAmount -= BUY_AMOUNT;
				todaysDepot.mFlag = Depot.Point.BuyFlag.SELL;
				todaysDepot.mMoney += BUY_AMOUNT*todayClose;
			}
			// do nothing if today.close==today.avg200
			else{
				DamnShit.mLogGui.loglnf("Not traded today. Today's date is: %s", todaysStock.mDateTime.toLocalDate().toString());

				// just to be sure
				todaysDepot.mBuyAmount += 0;
				todaysDepot.mFlag = Depot.Point.BuyFlag.UNCHANGED;
				todaysDepot.mMoney -= 0;
			}
			todaysDepot.mWorth = todayClose*todaysDepot.mStocks;
		}
	}
	public static void trade_buyAndHold(Depot dep, StockResults stockRes){}
}
