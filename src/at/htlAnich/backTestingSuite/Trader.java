package at.htlAnich.backTestingSuite;

import static at.htlAnich.tools.BaumbartLogger.logf;

public final class Trader {
	private Trader(){ }

	public static void trade(Depot dep, String symbol){

		switch(dep.getStrategy()){
			case avg200:
				trade_avg200(dep, symbol);
				break;
			case avg200_3percent:
				trade_avg200_3Percent(dep, symbol);
				break;
			case avg200_false:
				trade_avg200_false(dep, symbol);
				break;
			case buyAndHold:
				trade_buyAndHold(dep, symbol);
				break;
			case NONE:
			default:
				logf("No trade done%n");
				break;
		}
	}

	private static void trade_avg200(Depot dep, String symbol){
		var temp = dep.getAll(symbol);
		temp.sort(null);

		logf("Date at first index: %s%nDate at last index: %s%n",
			temp.get(0).getDate().toString(),
			temp.get(temp.size()).toString());
		System.exit(0);

		var point = new Depot.Point();
	}

	private static void trade_avg200_3Percent(Depot dep, String symbol){
		for(var point : dep.getAll(symbol)){

		}
	}

	private static void trade_avg200_false(Depot dep, String symbol){
		for(var point : dep.getAll(symbol)){

		}
	}

	private static void trade_buyAndHold(Depot dep, String symbol){
		for(var point : dep.getAll(symbol)){

		}
	}
}
