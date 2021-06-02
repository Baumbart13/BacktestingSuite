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

	public static void calcAverage(List<Depot.Point> points, int max){
		final var daysBack = 200;

		for(var currDay = 0; currDay < max; ++currDay){

			// sum up all values from currPoint.Date.minusDays(200)
			var sum = 0.0f;
			for(var dayPast = currDay-1; !(dayPast<0 || dayPast<currDay-daysBack); --dayPast){
				if(dayPast < 0 || dayPast < currDay-daysBack) {
					try{
						throw new Exception("Should not have come to this");
					}catch(Exception e){
						e.printStackTrace();
					}
					break;
				}
				sum += points.get(dayPast).getClose();
			}
			sum = sum/daysBack;

			points.get(currDay).setAvg200(sum);
		}

		return;
	}

	private static void splitCorrection(List<Depot.Point> points, StockResults ref, int upperDayBound){
		var splitCoeff = ref.getDataPoints().get(upperDayBound).getValue(StockDataPoint.ValueType.splitCoefficient);
		// do splitcorrection if there was a split
		if(splitCoeff != 1.0f){

			for(var splitDayIndex = upperDayBound; splitDayIndex >= 0; --splitDayIndex){
				var tempDepotPoint = points.get(splitDayIndex).clone();
				final var tempStockPoint = ref.getDataPoints().get(splitDayIndex).clone();

				splitCoeff *= ref.getDataPoints().get(splitDayIndex).getValue(StockDataPoint.ValueType.splitCoefficient);
				var splitTempClose = tempStockPoint.getValue(StockDataPoint.ValueType.close);
				var splitTempStocks = tempDepotPoint.getStocks();

				var splitAdjustedClose = splitTempClose / splitCoeff;
				var splitAdjustedStocks = splitTempStocks * splitCoeff;

				// revert tempDepotPoint into temp
				tempDepotPoint = new Depot.Point(tempDepotPoint.getDate(), tempDepotPoint.getSymbol(),
					tempDepotPoint.getFlag(), tempDepotPoint.getBuyAmount(), splitTempStocks,
					splitAdjustedClose*splitAdjustedStocks, tempDepotPoint.getAvg200(),
					splitAdjustedClose, tempDepotPoint.getMoney());
				points.set(splitDayIndex, tempDepotPoint);
			}
			// need to recalculate avg200 after splitCorrection, due to different values now
			calcAverage(points, upperDayBound);
		}
	}

	private static void trade_avg200(Depot dep, StockResults ref){
		var temp = dep.getAll(ref.getSymbol());
		temp.sort(null);

		// this loop is for trading
		for(var tradingDayIndex = 0; tradingDayIndex < ref.getDataPoints().size()-1; ++tradingDayIndex){
			splitCorrection(temp, ref, tradingDayIndex);

			//
			// trade

			//create new depotPoint for tomorrow, to trade
			final var tradingDay = ref.getDataPoints().get(tradingDayIndex);
			final var tomorrowDay = ref.getDataPoints().get(tradingDayIndex+1);
			final var symbol = ref.getSymbol();

			var todaysDepotP = temp.get(tradingDayIndex);
			var tomorrowsDepotP = new Depot.Point(tomorrowDay.mDateTime.toLocalDate(), symbol,
				Depot.Point.BuyFlag.UNCHANGED, 0, 0, 0.0f,
				tomorrowDay.getValue(StockDataPoint.ValueType.avg200),
				tradingDay.getValue(StockDataPoint.ValueType.close), 0.0f
			);

			// get data from today and clone it to tomorrow
			tomorrowsDepotP = todaysDepotP.clone();
			// refresh tomorrows data with tomorrowDay
			tomorrowsDepotP.setDate(tomorrowDay.mDateTime.toLocalDate());
			tomorrowsDepotP.setAvg200(tomorrowDay.getValue(StockDataPoint.ValueType.avg200));
			tomorrowsDepotP.setClose(tomorrowDay.getValue(StockDataPoint.ValueType.close));
			// then trade tomorrow
			var tomorrowClose = tomorrowsDepotP.getClose();
			var tomorrowAvg = todaysDepotP.getAvg200();

			// buy
			if(tomorrowClose < tomorrowAvg){
				tomorrowsDepotP.setBuyFlag(Depot.Point.BuyFlag.BUY);

				// buy a maximum of 10 on one day
				var buyableStocks = (int)(tomorrowsDepotP.getMoney() / tomorrowClose);
				if(buyableStocks > 10){
					buyableStocks = 10;
				}

				todaysDepotP.setMoney(todaysDepotP.getMoney()-tomorrowClose*buyableStocks);
				todaysDepotP.setStocks(todaysDepotP.getStocks()+buyableStocks);

			}else if(tomorrowClose > tomorrowAvg) {// sell
				tomorrowsDepotP.setBuyFlag(Depot.Point.BuyFlag.SELL);

				// sell a maximum of 10 on one day
				var sellableStocks = tomorrowsDepotP.getStocks();
				if(sellableStocks > 10){
					sellableStocks = 10;
				}

				tomorrowsDepotP.setMoney(tomorrowsDepotP.getMoney()+tomorrowClose*sellableStocks);
				tomorrowsDepotP.setStocks(tomorrowsDepotP.getStocks()-sellableStocks);
			}

			// end - trade
			//
			temp.add(tomorrowsDepotP);
		}
		return;
	}

	private static void trade_avg200_3Percent(Depot dep, StockResults ref){
		var temp = dep.getAll(ref.getSymbol());
		temp.sort(null);

		// this loop is for trading
		for(var tradingDayIndex = 0; tradingDayIndex < temp.size(); ++tradingDayIndex){

			//
			// check for split and eventually correct the entries
			//
			splitCorrection(temp, ref, tradingDayIndex);
			//
			// end - splitCorrection
			//


			//
			// trade
			//

			// if bought or sold already on this day, just skip it to increase performance a bit
			var todaysBuyflag = temp.get(tradingDayIndex).getFlag();
			if(!todaysBuyflag.equals(Depot.Point.BuyFlag.UNCHANGED)){
				continue;
			}

			var today = temp.get(tradingDayIndex);
			var todaysClose = today.getClose();
			var todaysAvg200 = today.getAvg200();
			// if nothing bought and close<avg200 -> buy
			if(todaysClose < todaysAvg200-todaysAvg200*0.03){
				todaysBuyflag = Depot.Point.BuyFlag.BUY;

				// buy a maximum of 10 on one day
				var buyableStocks = (int)(today.getMoney() / todaysClose);
				if(buyableStocks > 10){
					buyableStocks = 10;
				}

				today.setMoney(today.getMoney()-todaysClose*buyableStocks);
				today.setStocks(today.getStocks()+buyableStocks);

			}else
				// else if nothing bought and close>avg200 -> sell
				if(todaysClose > todaysAvg200+todaysAvg200*0.03){
					todaysBuyflag = Depot.Point.BuyFlag.SELL;

					// sell a maximum of 10 on one day
					var sellableStocks = today.getStocks();
					if(sellableStocks > 10){
						sellableStocks = 10;
					}

					today.setMoney(today.getMoney()+todaysClose*sellableStocks);
					today.setStocks(today.getStocks()-sellableStocks);
				}

			//
			// end - trade
			//
		}
	}

	private static void trade_avg200_false(Depot dep, StockResults ref){
		var temp = dep.getAll(ref.getSymbol());
		temp.sort(null);

		// this loop is for trading
		for(var tradingDayIndex = 0; tradingDayIndex < temp.size(); ++tradingDayIndex){

			//
			// check for split and eventually correct the entries
			//
			splitCorrection(temp, ref, tradingDayIndex);
			//
			// end - splitCorrection
			//


			//
			// trade
			//

			// if bought or sold already on this day, just skip it to increase performance a bit
			var todaysBuyflag = temp.get(tradingDayIndex).getFlag();
			if(!todaysBuyflag.equals(Depot.Point.BuyFlag.UNCHANGED)){
				continue;
			}

			var today = temp.get(tradingDayIndex);
			var todaysClose = today.getClose();
			var todaysAvg200 = today.getAvg200();
			// if nothing bought and close<avg200 -> buy
			if(todaysClose > todaysAvg200){
				todaysBuyflag = Depot.Point.BuyFlag.BUY;

				// buy a maximum of 10 on one day
				var buyableStocks = (int)(today.getMoney() / todaysClose);
				if(buyableStocks > 10){
					buyableStocks = 10;
				}

				today.setMoney(today.getMoney()-todaysClose*buyableStocks);
				today.setStocks(today.getStocks()+buyableStocks);

			}else
				// else if nothing bought and close>avg200 -> sell
				if(todaysClose < todaysAvg200){
					todaysBuyflag = Depot.Point.BuyFlag.SELL;

					// sell a maximum of 10 on one day
					var sellableStocks = today.getStocks();
					if(sellableStocks > 10){
						sellableStocks = 10;
					}

					today.setMoney(today.getMoney()+todaysClose*sellableStocks);
					today.setStocks(today.getStocks()-sellableStocks);
				}

			//
			// end - trade
			//
		}
	}

	private static void trade_buyAndHold(Depot dep, StockResults ref){
		var temp = dep.getAll(ref.getSymbol());
		temp.sort(null);

		final int waitingForDays = 200;

		// this loop is for trading
		for(var tradingDayIndex = 0; tradingDayIndex < temp.size(); ++tradingDayIndex){

			//
			// check for split and eventually correct the entries
			//
			splitCorrection(temp, ref, tradingDayIndex);
			//
			// end - splitCorrection
			//


			//
			// trade
			//

			// if bought or sold already on this day, just skip it to increase performance a bit
			var todaysBuyflag = temp.get(tradingDayIndex).getFlag();
			if(!todaysBuyflag.equals(Depot.Point.BuyFlag.UNCHANGED)){
				continue;
			}

			var today = temp.get(tradingDayIndex);

			var lastChange = new Depot.Point();
			for(var i = tradingDayIndex-1; i >= 0; --i){
				var tradeTempDay = temp.get(i);
				if(tradeTempDay.getFlag().equals(Depot.Point.BuyFlag.BUY) || tradeTempDay.getFlag().equals(Depot.Point.BuyFlag.SELL)){
					lastChange = tradeTempDay;
				}
			}

			var rangeLower = today.getDate().minusDays(waitingForDays+2);
			var rangeUpper = today.getDate().plusDays(waitingForDays+2);

			// is in range for change.. let's sell and buy something
			if(lastChange.getDate().isAfter(rangeLower) && lastChange.getDate().isBefore(rangeUpper)){
				//
				// first sell a max-amount of 10
				//
				todaysBuyflag = Depot.Point.BuyFlag.SELL;
				var sellableStocks = today.getStocks();
				if(sellableStocks > 10){
					sellableStocks = 10;
				}
				today.setMoney(today.getMoney()+today.getClose()*sellableStocks);
				today.setStocks(today.getStocks()-sellableStocks);

				//
				// then buy a max-amount of 10
				//
				todaysBuyflag = Depot.Point.BuyFlag.BUY;
				var buyableStocks = (int)(today.getMoney() / today.getClose());
				if(buyableStocks > 10){
					buyableStocks = 10;
				}
				today.setMoney(today.getMoney()-today.getClose()*buyableStocks);
				today.setStocks(today.getStocks()+buyableStocks);
			}

			//
			// end - trade
			//
		}
	}
}
