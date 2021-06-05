package at.htlAnich.backTestingSuite;

import at.htlAnich.stockUpdater.StockResults;
import at.htlAnich.tools.database.CanBeTable;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static at.htlAnich.tools.BaumbartLogger.loglnf;

/**
 * A Collection of Depot-Rows. A different depot means a different strategy.
 */
public class Depot implements CanBeTable {
	protected List<Point> mPoints = null;
	protected Strategy mStrategy = null;
	protected String mSymbol = null;

	public static class Point implements Comparable {

		public static enum BuyFlag{
			BUY,
			SELL,
			UNCHANGED;

			public static BuyFlag valueOf(int i){
				return values()[i];
			}
		}

		public LocalDate mDate	= null;
		public BuyFlag mFlag	= null;
		public int mBuyAmount	= 0;
		public int mStocks	= 0;
		public float mWorth	= 0.0f;
		public float mAvg200	= 0.0f;
		public float mClose	= 0.0f;
		public float mMoney	= 0.0f;

		public Point(){
			this(LocalDate.now(), BuyFlag.UNCHANGED, 0, 0, 0.0f, 0.0f, 0.0f, 0.0f);
		}

		public Point(Point other){
			this(other.mDate, other.mFlag, other.mBuyAmount, other.mStocks, other.mWorth, other.mAvg200, other.mClose, other.mMoney);
		}

		public Point(@NotNull LocalDate date, BuyFlag flag, int buyAmount, int totalStocks, float totalWorth, float avg200, float close, float money){
			mDate = date;
			mFlag = (flag == null) ? BuyFlag.UNCHANGED : flag;
			mFlag = flag;
			// Let's stay positive
			mBuyAmount = Math.abs(buyAmount);
			mStocks = Math.abs(totalStocks);
			mWorth = Math.abs(totalWorth);
			mAvg200 = avg200;
			mClose = close;
			mMoney = money;
		}

		public Point clone(){
			return new Point(this);
		}

		/**
		 * <p>In the foregoing description, the notation
		 * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
		 * <i>signum</i> function, which is defined to return one of {@code -1},
		 * {@code 0}, or {@code 1} according to whether the value of
		 * <i>expression</i> is negative, zero, or positive, respectively.
		 *
		 * @param o the object to be compared.
		 * @return a negative integer, zero, or a positive integer as this object
		 * is less than, equal to, or greater than the specified object.
		 * @throws NullPointerException if the specified object is null
		 * @throws ClassCastException   if the specified object's type prevents it
		 *                              from being compared to this object.
		 */
		@Override
		public int compareTo(@NotNull Object o) {
			if(o.getClass().equals(this.getClass())) {
				var temp = (Point)o;
				return this.mDate.compareTo(temp.mDate);
			}
			return this.toString().compareTo(o.toString());
		}
	}

	/**
	 * The strategies that can be used on <code>Depot</code>.
	 */
	public static enum Strategy{
		NONE,
		avg200,
		avg200_3percent,
		buyAndHold(7),
		avg200_false;

		private int delayBetweenBuys = -1;
		Strategy(){}
		Strategy(int delay){ delayBetweenBuys = delay; }
		public Integer delayBetweenDays(){return delayBetweenBuys<1 ? null : delayBetweenBuys;}
		public static Strategy valueOf(int i){
			return values()[i];
		}
	}

	public Depot(String symbol, Strategy strat, Point... points){
		var sortedTemp = new LinkedList<Point>();

		for(var p : points){
			sortedTemp.add(p);
		}

		Collections.sort(sortedTemp);
		this.mPoints = sortedTemp;
		//this.mPoints.addAll(sortedTemp);
		this.mStrategy = strat;
		this.mSymbol = symbol;
	}

	/**
	 * Default-constructor just initializes the points and the strategy, to preserve a null-pointer-exception.
	 */
	public Depot(){
		mPoints = new LinkedList<>();
		mStrategy = Strategy.NONE;
		mSymbol = "";
	}

	/**
	 * Returns all <code>DepotPoint</code>s.
	 * @return the same collection of <code>DepotPoint</code>s as in this object.
	 */
	public List<Point> getData(){
		return mPoints;
	}
	
	public void addPoint(Depot.Point point){
		var pointDate = point.mDate;
		var i = 0;
		
		var currDay = this.mPoints.get(i).mDate;
		while(currDay.isBefore(pointDate)){
			currDay = this.mPoints.get(++i).mDate;
		}
		
		mPoints.add(i, point);
	}
	
	public String getSymbol(){
		return mSymbol;
	}

	@Override
	public String getTableName() {
		return BackTestingDatabase._TABLE_NAME;
	}

	public Strategy getStrategy(){
		return this.mStrategy;
	}
}
