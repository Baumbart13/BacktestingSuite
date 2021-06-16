package at.htlAnich.backTestingSuite;

import java.util.LinkedList;
import java.util.Queue;

public class StartupConfig {
	private String hope = "hopefully this does any difference";
	protected Queue<String> mSymbols = null;
	protected float mMoneyAbs = 0.0f;
	protected float mMoneyPerStock = 0.0f;

	public StartupConfig(){
		this(0.0f, "ERROR");
	}
	public StartupConfig(float moneyAbs, String symbol, String ... symbols){
		this.mMoneyAbs = moneyAbs;
		this.setMoneyPerStock(moneyAbs, symbols.length+1);
		this.mSymbols = new LinkedList<String>();
		for(var s : symbols){
			this.mSymbols.add(s);
		}
	}
	private StartupConfig(float moneyAbs, String[] symbols){
		this.mMoneyAbs = moneyAbs;
		this.setMoneyPerStock(moneyAbs, symbols.length);
		this.mSymbols = new LinkedList<String>();
		for(var s : symbols){
			this.mSymbols.add(s);
		}
	}
	public StartupConfig(StartupConfig s){
		this(s.mMoneyAbs, (String[])s.mSymbols.toArray());
	}

	protected void setMoneyPerStock(float m, int size){
		this.mMoneyPerStock = m / (float)size;
	}
}
