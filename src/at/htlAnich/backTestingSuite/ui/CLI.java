package at.htlAnich.backTestingSuite.ui;

import java.time.LocalDate;
import java.util.Scanner;

public class CLI extends Input{
	protected Scanner mScanner;
	protected String nextLine(){
		var s = mScanner.nextLine();
		mScanner.reset();
		return s;
	}
	protected String next(){
		var s = mScanner.next();
		mScanner.reset();
		return s;
	}

	public CLI(){
		mScanner = new Scanner(System.in);
	}

	@Override
	protected void println(String s){
		System.out.println(s);
	}

	@Override
	protected void print(String s){
		System.out.print(s);
	}

	@Override
	public LocalDate getStartDate() {
		print("Please enter your starting date [yyyy mm dd]: ");
		var in = "a";
		in = nextLine();
		while(!containsChars(in, Symbols.LETTER_UPPERCASE, Symbols.LETTERS_LOWERCASE, Symbols.SEPCIAL_GERMAN, Symbols.SPECIAL_SYMBOLS)){
			print("Please try again [yyyy mm dd]: ");
			in = nextLine();
		}

		var year = Integer.parseInt(in.substring(0, 4));
		var gapIndex = in.indexOf(' ');
		var month = Integer.parseInt(in.substring(gapIndex+1, gapIndex+3));
		gapIndex = in.indexOf(' ', gapIndex+1);
		var day = Integer.parseInt(in.substring(gapIndex+1));
		var date = LocalDate.of(year, month, day);

		println("");
		return date;
	}

	@Override
	public LocalDate getEndDate() {
		print("Please enter your ending date [yyyy mm dd]: ");
		var in = "a";
		in = nextLine();
		while(!containsChars(in, Symbols.LETTER_UPPERCASE, Symbols.LETTERS_LOWERCASE, Symbols.SEPCIAL_GERMAN, Symbols.SPECIAL_SYMBOLS, Symbols.SPECIAL_COMMAS)){
			print("Please try again [yyyy mm dd]: ");
			in = nextLine();
		}

		var year = Integer.parseInt(in.substring(0, 4));
		var gapIndex = in.indexOf(' ');
		var month = Integer.parseInt(in.substring(gapIndex+1, gapIndex+3));
		gapIndex = in.indexOf(' ', gapIndex+1);
		var day = Integer.parseInt(in.substring(gapIndex+1));
		var date = LocalDate.of(year, month, day);

		println("");
		return date;
	}

	private boolean containsYesOrNo(String s){
		if(s.length() != 1){
			return false;
		}

		var c = s.toLowerCase().toCharArray()[0];
		return c == 'y' || c == 'n';
	}

	@Override
	public boolean isUsingFileForRequest() {
		var in = "";
		do{
			print("Do you want to use a file for more stocks at once? [Y/n]? ");
			in = next().trim();
		}while(!containsYesOrNo(in));

		var c = in.toLowerCase().toCharArray()[0];
		println("");
		return c=='y';
	}

	@Override
	public float startMoney() {
		var in = "";

		print("Please enter your maximum money you can offer. It will be split up evenly on all stocks, you want to trade: ");
		in = nextLine();
		while(!containsChars(in, Symbols.LETTER_UPPERCASE, Symbols.LETTERS_LOWERCASE, Symbols.SEPCIAL_GERMAN, Symbols.SPECIAL_SYMBOLS)){
			print("Please try again");
			in = nextLine();
		}
		var money = Float.parseFloat(in);
		return money;
	}

	@Override
	public String getWantedSymbol() {
		print("Please enter your wanted stock as the official abbreviation, as on the market: ");
		return nextLine().trim();
	}
}
