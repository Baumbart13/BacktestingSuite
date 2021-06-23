package at.htlAnich.backTestingSuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.Buffer;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.Arrays;

import at.htlAnich.backTestingSuite.badCode.DamnShit;
import at.htlAnich.tools.BaumbartLogger;

import static at.htlAnich.tools.BaumbartLogger.*;


public class BackTesting {
	public static Queue<String> requests = new java.util.LinkedList<>();
	public static Scanner cliInput = new Scanner(System.in);
	public static at.htlAnich.backTestingSuite.badCode.DamnShit damn;
	public static GUI inputScreen;
	private static boolean DEBUG = false;
	public static boolean DEBUG(){return DEBUG;}
	private static boolean inProd = false;
	public static boolean inProduction(){return inProd;}
	private static boolean usingAdjusted = true;
	public static boolean usingAdjusted(){return usingAdjusted;}

	public static void argumentHandling(List<String> args){
		if(args == null){
			System.exit(-1);
		}

		for(int i = 0; i < args.size(); ++i){
			if(!args.get(i).startsWith(ProgramArguments.PREFIX))
				continue;

			switch(ProgramArguments.valueOf(args.get(i).substring(2))){
				case inProduction:
					logf("We are in production now%n");
					inProd = true;
					break;
				case file:
					logf("Request from file%n");
					loadSymbols();
					break;
				case DEBUG:
					logf("DEBUG suite entered%n");
					DEBUG = true;
					break;
				case notAdjusted:
					logf("Not using adjsuted close values%n");
					usingAdjusted = false;
					break;
			}
		}
	}

	public static LocalDate parseDate(String s){
		var year = Integer.parseInt(s.substring(0, s.indexOf(" ")));
		var month = Integer.parseInt(s.substring(s.indexOf(" ") + 1, s.lastIndexOf(" ")));
		var day = Integer.parseInt(s.substring(s.lastIndexOf(" ")+1));

		return LocalDate.of(year, month, day);
	}

	public static void main(String[] args) {
		try {
			argumentHandling(Arrays.asList(args));
			//inputScreen = new GUI();
			var startDate = LocalDate.now();
			var endDate = startDate;
			var totalMoney = 0.0f;
			if(!DEBUG) {
				logf("Please enter your starting date [yyyy mm dd]: ");
				startDate = parseDate(cliInput.nextLine());
				logf("Please enter your last date [yyyy mm dd]: ");
				endDate = parseDate(cliInput.nextLine());
				logf("Please enter your total money you want to spend on each stock: ");
				totalMoney = cliInput.nextFloat();
			}else{
				startDate = LocalDate.of(2010, 1, 1);
				endDate = LocalDate.of(2021,6,16);
				totalMoney = 100000.0f;
			}

			final var noStocks = requests.size();
			while(requests.size() > 0){
				var symbol = requests.poll();
				logf("Current stock: %s%n", symbol);
				damn = new DamnShit(symbol, totalMoney/noStocks, inProd);
				damn.getValuesAndUpdateDatabase(startDate, endDate);
				logf("%s ended for %s%n", DamnShit.class.toString(), symbol);

				for(var strat : Depot.Strategy.values()) {
					var firstDayTotalWorth = damn.getFirstDepotEntry(strat).mMoney;
					var lastDayTotalWorth = damn.getLastDepotEntry(strat).mMoney;
					loglnf("strategy used: %s%npercentual difference: %.4f%nabsolute value: %.2f",
						strat.toString(), lastDayTotalWorth/firstDayTotalWorth, lastDayTotalWorth);
				}
			}

			logln("Waiting to enter something to exit program");
			waitForKeyPress();
			System.exit(0);
		}catch(Exception e){
			errlnf("Some Fucking Exception, that was not handled at all or was not intended to happen in any way occurred.");
			e.printStackTrace();
			System.gc();
			System.exit(-1);
		}

		/*var vals = new Depot[Depot.Strategy.values().length];
		try {
			var backDb = new BackTestingDatabase("localhost:3306", "root", "DuArschloch4", "baumbartstocks");
			backDb.connect();
			backDb.createDatabase(backDb.getDatabase());
			backDb.createTable(BackTestingDatabase._TABLE_NAME);
			for(var strat : Depot.Strategy.values()) {
				var val = backDb.getValues(symbol, strat);

				Trader.trade(val, symbol);
				backDb.updateDepots(val, symbol);
			}
			backDb.disconnect();
		}catch (SQLException e){
			e.printStackTrace();
		}*/

	}

	public static void loadSymbols(){
		var file = "res\\backtesting_requests.backtest";

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));

			var line = "";
			while((line = reader.readLine()) != null){
				line = line.trim();
				if(line.equals("")){
					continue;
				}
				requests.add(line.toUpperCase());
			}
		}catch(IOException e){
			e.printStackTrace();
			var fs = System.getProperty("user.dir");
			errlnf("%nFileSystem: \"%s\"%n",fs);
		}finally{
			try{
				if(reader!=null)
					reader.close();
			}catch(IOException ex){
				errlnf("Well, I can't help further than this. The file \"%s\" is maybe corrupted now. ¯\\_(ツ)_/¯",
					new File(file).getAbsolutePath());
			}
		}
	}
}
