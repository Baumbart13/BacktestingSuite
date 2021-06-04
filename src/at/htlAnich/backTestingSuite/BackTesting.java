package at.htlAnich.backTestingSuite;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import at.htlAnich.backTestingSuite.badCode.DamnShit;
import at.htlAnich.tools.BaumbartLogger;

import static at.htlAnich.tools.BaumbartLogger.*;


public class BackTesting {
	public static Scanner cliInput = new Scanner(System.in);
	public static at.htlAnich.backTestingSuite.badCode.DamnShit damn;
	public static GUI inputScreen;
	private static boolean inProd = false;
	public static boolean inProduction(){return inProd;}

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
				case DEBUG:
					logf("DEBUG suite entered%n");
					break;
			}
		}
	}

	public static void main(String[] args) {
		try {
			argumentHandling(Arrays.asList(args));
			inputScreen = new GUI();
			logf("Please enter your wanted stock: ");
			var symbol = cliInput.nextLine();

			//damn = new DamnShit(symbol, inProd);
			//damn.getValuesAndUpdateDatabase();
			System.out.println("DamnShit ended");

			System.out.println("Waiting to eneter something to exit program");
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
}
