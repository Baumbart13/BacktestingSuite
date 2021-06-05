package at.htlAnich.backTestingSuite.ui;

import java.time.LocalDate;

public abstract class Input {
	public static enum Symbols{
		NUMBERS("0123456789"),
		LETTERS_LOWERCASE("abcdefghijklmnopqrstuvwxyz"),
		LETTER_UPPERCASE("ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
		SPECIAL_SYMBOLS("^°´\"§$%&/()=?\\´`²³{[]}@€µ|<>+*#'-_:;~"),
		SEPCIAL_GERMAN("äÄöÖüÜß"),
		SPECIAL_COMMAS(",.");

		private String content;
		private Symbols(String c){
			this.content = c;
		}
		public String content(){
			return this.content;
		}
	}

	public abstract LocalDate getStartDate();
	public abstract LocalDate getEndDate();
	public abstract boolean isUsingFileForRequest();
	public abstract float startMoney();
	public abstract String getWantedSymbol();
	protected abstract void print(String s);
	protected abstract void println(String s);

	public boolean containsChars(String s, Symbols... containing){
		var containString = new StringBuilder();
		for(var f : containing){
			containString.append(f.content);
		}

		for(int i = 0; i < s.length(); ++i){
			for(int j = 0; j < containString.length(); ++j){
				if(s.charAt(i) == containString.charAt(j)){
					return true;
				}
			}
		}
		return false;
	}
}
