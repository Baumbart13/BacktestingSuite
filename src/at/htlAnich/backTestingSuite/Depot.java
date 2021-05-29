package at.htlAnich.backTestingSuite;

import at.htlAnich.tools.database.CanBeTable;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A Collection of Depot-Rows. A different depot means a different strategy.
 */
public class Depot implements CanBeTable {
	protected List<Point> mPoints = null;
	protected Strategy mStrategy = null;

	public static class Point implements Comparable {

		public enum BuyFlag{
			BUY,
			SELL,
			UNCHANGED;
		}

		private LocalDate mDate	= null;
		private String mSymbol	= null;
		private BuyFlag mFlag	= null;
		private int mBuyAmount	= 0;
		private int mStocks	= 0;
		private float mWorth	= 0.0f;
		private float mAvg200	= 0.0f;
		private float mClose	= 0.0f;

		public Point(){
			this(LocalDate.now(), "", BuyFlag.UNCHANGED, 0, 0, 0.0f, 0.0f, 0.0f);
		}

		public Point(@NotNull LocalDate date, @NotNull String symbol, BuyFlag flag, int buyAmount, int totalStocks, float totalWorth, float avg200, float close){
			mDate = date;
			mSymbol = symbol;
			mFlag = (flag == null) ? BuyFlag.UNCHANGED : flag;
			mFlag = flag;
			// Let's stay positive
			mBuyAmount = Math.abs(buyAmount);
			mStocks = Math.abs(totalStocks);
			mWorth = Math.abs(totalWorth);
			mAvg200 = avg200;
			mClose = close;
		}

		@NotNull
		public LocalDate getDate() {
			return mDate;
		}

		@NotNull
		public String getSymbol() {
			return mSymbol;
		}

		@NotNull
		public BuyFlag getFlag() {
			return mFlag;
		}

		public int getBuyAmount() {
			return mBuyAmount;
		}

		public int getStocks() {
			return mStocks;
		}

		public float getWorth() {
			return mWorth;
		}

		public float getAvg200(){
			return mAvg200;
		}

		public float getClose(){
			return mClose;
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
				if(temp.mSymbol.equals(this.mSymbol)) {
					return this.mDate.compareTo(temp.mDate);
				}
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
	}

	public Depot(Strategy strat, Point... points){
		var sortedTemp = Arrays.asList(points);
		Collections.sort(sortedTemp);
		this.mPoints.addAll(mPoints);
		this.mStrategy = strat;
	}

	/**
	 * Default-constructor just initializes the points and the strategy, to preserve a null-pointer-exception.
	 */
	public Depot(){
		mPoints = new LinkedList<>();
		mStrategy = Strategy.NONE;
	}

	/**
	 * Returns all <code>DepotPoints</code> from a specific date, no matter what stock it is.
	 * @param date The specific date.
	 * @return a collection of <code>DepotPoints</code> of one day.
	 */
	public List<Point> getAll(LocalDate date){
		List<Point> out = new LinkedList<>();
		for(var point : mPoints){
			if(point.getDate().equals(date)){
				out.add(point);
			}
		}
		return out;
	}

	/**
	 * Returns all <code>DepotPoints</code> from a specific stock, no matter what date it is.
	 * @param symbol The ticker-name of the stock.
	 * @return a collection of <code>DepotPoints</code> of one stock.
	 * @see at.htlAnich.stockUpdater.api.ApiParser.Function LISTING_STATUS.
	 */
	public List<Point> getAll(String symbol){
		List<Point> out = new LinkedList<>();
		for(var point : mPoints){
			if(point.getSymbol().equals(symbol)){
				out.add(point);
			}
		}
		return out;
	}

	/**
	 * Returns a <code>DepotPoint</code> from a specific stock and a specific date.
	 * @param symbol The ticker-name of the stock.
	 * @param date The date of the <code>DepotPoint</code>.
	 * @return if there is an entry, then <code>DepotPoint</code> of given stock and date will be returned, else an empty
	 * <code>DepotPoint</code>-Object.
	 */
	public Point getPoint(String symbol, LocalDate date){
		for(var point : mPoints){
			if(point.getSymbol().equals(symbol) && point.getDate().equals(date)){
				return point;
			}
		}
		return new Point();
	}

	/**
	 * Returns the number of elements, that are inside of this <code>Depot</code>-object.
	 * @return a non-negative integer holding the amount of elements inside this object.
	 */
	public int numberOfElements(){
		return this.mPoints.size();
	}

	/**
	 * Returns a <code>DepotPoint</code> at given index.
	 * @param i the index of the element, that will be returned.
	 * @return if <code>i</code> is greater than or equals <code>numberOfElements</code> an empty <code>DepotPoint</code>
	 * will be returned, otherwise the requested one.
	 */
	public Point getPoint(int i){
		if(i >= mPoints.size()){
			return new Point();
		}
		return mPoints.get(i);
	}

	/**
	 * Returns the index of the corresponding tickerSymbol and date.
	 * @param symbol the ticker of the stock.
	 * @param date the date of the <code>DepotPoint</code>.
	 * @return a non-negative integer, if there is an element. Otherwise <code>-1</code> will be returned.
	 */
	public int getIndexOfPoint(String symbol, LocalDate date){
		for(int i = 0; i < mPoints.size(); ++i){
			if(mPoints.get(i).getSymbol().equals(symbol) && mPoints.get(i).getDate().equals(date)){
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns the index of the corresponding <code>DepotPoint</code>.
	 * @param point the <code>DepotPoint</code> which index will be looked up.
	 * @return a non-negative integer, if there is an element. Otherwise <code>-1</code> will be returned.
	 */
	public int getIndexOfPoint(Point point){
		return getIndexOfPoint(point.getSymbol(), point.getDate());
	}

	/**
	 * A <code>DepotPoint</code> will be added to this <code>Depot</code>.
	 * @param point the point, that will be added.
	 */
	public void addDepotPoint(Point point){
		int index = 0;
		for( ; index < mPoints.size(); ++index){
			if(point.getDate().isBefore(mPoints.get(index).getDate())){
				break;
			}
		}
		mPoints.add(index, point);
	}

	/**
	 * Removes the first entry of given <code>DepotPoint</code>.
	 * @param point the element, that shall be removed.
	 * @return <code>true</code> if the element existed.
	 */
	public boolean removeDepotPoint(Point point){
		return mPoints.remove(point);
	}

	/**
	 * Removes the entry at given index if
	 * @param index the index of element, that shall be removed.
	 * @return <code>true</code> if the element existed. <code>false</code> if <code>inde</code>
	 */
	public boolean removeDepotPoint(int index){
		if(index >= mPoints.size()) return false;
		var x = mPoints.remove(index);
		return (x != null);
	}

	public Point popDepotPoint(int index){
		return mPoints.remove(index);
	}

	@Override
	public String getTableName() {
		return BackTestingDatabase._TABLE_NAME;
	}

	public Strategy getStrategy(){
		return this.mStrategy;
	}
}
