package at.htlAnich.stockUpdater;

import at.htlAnich.tools.dataTypes.CanSaveCSV;
import at.htlAnich.tools.database.CanBeTable;
import jdk.jshell.spi.ExecutionControl;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is the interface between the API and Database. It supports <code>StockDataPoint</code> and
 * <code>StockSymbolPoint</code>.
 */
public class StockResults implements CanBeTable {
	public static final String _TABLE_NAME__DATA = "stock_data";

	private HashMap<StockDataPoint.ValueType, Float> mLowerBounds;
	private HashMap<StockDataPoint.ValueType, Float> mUpperBounds;
	private LocalDateTime mOldestDate;
	private LocalDateTime mNewestDate;
	private List<StockDataPoint> mDataPoints;
	private String mSymbol;
	private String mName;

	public enum DatabaseNames_Data{
		data_datetime,
		data_symbol,
		data_open,
		data_close,
		data_high,
		data_low,
		data_volume,
		data_splitCoefficient,
		data_close_adjusted,
		data_avg200;
	}

	public enum DatabaseNames_Symbol{
		symbol_symbol,	// primary key
		symbol_name,
		symbol_exchange,
		symbol_asset,
		symbol_ipoDate,
		symbol_delistingDate,
		symbol_status;

		@Override
		public String toString(){
			return this.name().replace("symbol_", "");
		}
	}

	public void splitCorrection(){
		var splitCoeff = 1.0f;

		for(var i = mDataPoints.size()-1; i >= 0; --i){
			var tempPoint = mDataPoints.get(i);

			splitCoeff *= tempPoint.getValue(StockDataPoint.ValueType.splitCoefficient);
			var tempClose = tempPoint.getValue(StockDataPoint.ValueType.close);
			var adjustedClose = tempClose / splitCoeff;

			tempPoint.setValue(StockDataPoint.ValueType.close_adjusted, adjustedClose);

			mDataPoints.set(i, tempPoint);
		}
		return;
	}

	@Override
	public StockResults clone(){
		return new StockResults(this);
	}

	/**
	 * Copy constructor
	 * @param other Copies the values from <code>other</code> to a new instance.
	 */
	public StockResults(StockResults other){
		if(other == null) return;
		this.mLowerBounds = new HashMap<StockDataPoint.ValueType, Float>(other.mLowerBounds);
		this.mUpperBounds = new HashMap<StockDataPoint.ValueType, Float>(other.mUpperBounds);
		this.mOldestDate = LocalDateTime.of(other.mOldestDate.toLocalDate(), other.mOldestDate.toLocalTime());
		this.mNewestDate = LocalDateTime.of(other.mNewestDate.toLocalDate(), other.mNewestDate.toLocalTime());
		this.mDataPoints = other.mDataPoints.subList(0, other.mDataPoints.size());
		this.mSymbol = other.mSymbol.toString();
		this.mName = other.mName;
	}

	public StockResults(String symbol, String name){
		this.mSymbol = symbol;
		this.mName = name;
		this.mLowerBounds = new HashMap<>();
		this.mUpperBounds = new HashMap<>();
		this.mDataPoints = new LinkedList<>();
		this.mOldestDate = LocalDateTime.MAX;
		this.mNewestDate = LocalDateTime.MIN;

		for(var x : StockDataPoint.ValueType.values()){
			mLowerBounds.put(x, Float.MAX_VALUE);
			mUpperBounds.put(x, -Float.MAX_VALUE);
		}
	}

	/**
	 * Simply updates the <code>avg200</code> based on <code>close_adjusted</code>.
	 */
	public void calcAverage(){
		final var daysBack = 200;

		for(var currDay = 0; currDay < this.mDataPoints.size(); ++currDay){

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
				sum += this.mDataPoints.get(dayPast).getValue(StockDataPoint.ValueType.close_adjusted);
			}
			sum = sum/daysBack;

			this.mDataPoints.get(currDay).setValue(StockDataPoint.ValueType.avg200, sum);
		}

		return;
	}

	/**
	 *
	 * @param symbol
	 */
	public StockResults(String symbol){
		this(symbol, "");
	}

	public void addDataPoint(StockDataPoint point){
		mDataPoints.add(point);

		if(point.mDateTime.isAfter(mNewestDate)){
			mNewestDate = point.mDateTime;
		}
		if(point.mDateTime.isBefore(mOldestDate)){
			mOldestDate = point.mDateTime;
		}

		for(var x : StockDataPoint.ValueType.values()){

			if (point.getValue(x) < mLowerBounds.get(x)) {
				mLowerBounds.put(x, point.getValue(x));
			}
			if (point.getValue(x) > mUpperBounds.get(x)) {
				mUpperBounds.put(x, point.getValue(x));
			}
		}
		return;
	}

	public LocalDateTime getOldestDate() {
		return mOldestDate;
	}

	public LocalDateTime getNewestDate(){
		return mNewestDate;
	}

	/**
	 * @return the lower bound of the given type, except <code>StockValueType.avgValue</code>: this will return 0.0f.
	 */
	public float getLowerBound(StockDataPoint.ValueType t){
		return mLowerBounds.get(t);
	}

	/**
	 * @return the upper bound of the given type, except <code>StockValueType.avgValue</code>: this will return 0.0f.
	 */
	public float getUpperBound(StockDataPoint.ValueType t){
		return mUpperBounds.get(t);
	}

	/**
	 * @return the range of the given type, except <code>StockValueType.avgValue</code>: this will return 0.0f.
	 */
	public float getRange(StockDataPoint.ValueType t){
		return mUpperBounds.get(t) - mLowerBounds.get(t);
	}

	/**
	 * @return the DataPoints of this object.
	 */
	public List<StockDataPoint> getDataPoints(){
		return mDataPoints;
	}

	/**
	 * The short form of the stock.
	 */
	public String getSymbol(){
		return mSymbol;
	}

	/**
	 * The full name of the stock.
	 */
	public String getName(){
		return mName;
	}

	@Override
	/**
	 * @return A string, which tells if this object handles the data or the symbols.
	 */
	public String getTableName(){
		return _TABLE_NAME__DATA;
	}
}
