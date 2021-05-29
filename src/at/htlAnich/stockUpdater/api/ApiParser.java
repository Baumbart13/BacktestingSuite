package at.htlAnich.stockUpdater.api;

import at.htlAnich.stockUpdater.StockResults;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ApiParser {
	private static final String ApiUrl = "https://www.alphavantage.co/query?";
	private String mApiKey = "demo";

	public ApiParser(ApiParser other){
		this.mApiKey = other.mApiKey.toString();
	}

	public ApiParser(String apiKey){
		if(apiKey.length() < 1){
			apiKey = "FATAL ERROR: Wrong API-Key!";
		}
		mApiKey = apiKey;
	}

	public enum Function{
		/**
		 * This API returns a list of active or delisted US stocks and ETFs, either as of the latest trading day or at a
		 * specific time in history. The endpoint is positioned to facilitate equity research on asset lifecycle and
		 * survivorship.
		 */
		LISTING_STATUS(2),
		/**
		 * This API returns raw (as-traded) daily open/high/low/close/volume values, daily adjusted close values, and
		 * historical split/dividend events of the global equity specified, covering 20+ years of historical data.
		 */
		TIME_SERIES_DAILY_ADJUSTED(1|2),
		/**
		 This API returns raw (as-traded) daily time series (date, daily open, daily high, daily low, daily close, daily
		 volume) of the global equity specified, covering 20+ years of historical data. If you are also interested in
		 split/dividend-adjusted historical data, please use the <code>Daily Adjusted API</code>, which covers adjusted
		 close values and historical split and dividend events.
		 */
		TIME_SERIES_DAILY(1|2);

		// 1 - 0b0000_0001 - suppportsJSON
		// 2 - 0b0000_0010 - supportsCSV
		private int supportsThings = 0;
		private Function(int supportsThings){
			this.supportsThings = supportsThings;
		}
		public boolean supportsJSON(){
			return (0b0000_0001 & supportsThings) == 0b0000_0001;
		}
		public boolean supportsCSV(){
			return (0b0000_0010 & supportsThings) == 0b0000_0010;
		}
	}

	public enum Outputsize{
		full, compact
	}

	public enum DataType{
		json, csv
	}

	private String makeUrl(Function fun, String symbol, Outputsize outputSize, DataType dataType){
		switch(fun){
			case LISTING_STATUS:
				return String.format("%sfunction=%s&apikey=%s",
					ApiUrl, fun.name(), mApiKey);
			case TIME_SERIES_DAILY:
			case TIME_SERIES_DAILY_ADJUSTED:
				return String.format("%sfunction=%s&symbol=%s&outputsize=%s&datatype=%s&apikey=%s",
					ApiUrl, fun.name(), symbol, outputSize, dataType, mApiKey);
			default:
				return String.format("%sfunction=%s&symbol=%s&datatype=%s&apikey=%s",
					ApiUrl, fun.name(), symbol, dataType, mApiKey);
		}
	}

	private String makeUrl(Function fun, String symbol){
		return makeUrl(fun, symbol, Outputsize.full, DataType.json);
	}

	/**
	 * Calls Functions of the www.alphavantage.co API and returns the request as a StockResults object. Uses
	 * <code>OutputSize.full</code>. Uses <code>DataType.json</code>.
	 * @param symbol The symbol of the stock.
	 * @param fun The API-function that will be called.
	 * @return the StockResults of the API request.
	 * @throws IOException if an I/O Exception occurs
	 * @see StockResults
	 * @see Function
	 */
	public StockResults request(@NotNull String symbol, @NotNull Function fun) throws IOException{
		return request(symbol, fun, Outputsize.full, DataType.json);
	}

	/**
	 * Calls Functions of the <code>www.alphavantage.co</code> API and returns the request as a StockResults object. Uses full
	 * <code>OutputSize</code>. Uses json <code>DataType</code>.
	 * @param symbol The symbol of the stock.
	 * @param fun The API-function that will be called.
	 * @param size The number of entries.
	 * @param type The outputType of the function.
	 * @return the StockResults of the API request.
	 * @throws IOException if an I/O Exception occurs
	 * @see StockResults
	 * @see Function
	 */
	public StockResults request(@NotNull String symbol, @NotNull Function fun, Outputsize size, DataType type)
		throws IOException{

		var url = makeUrl(fun, symbol, size, type);
		var out = new StockResults("");

		var json = new JSONObject(IOUtils.toString(new URL(url), StandardCharsets.UTF_8));
		out = switch (fun) {
			case TIME_SERIES_DAILY_ADJUSTED -> ApiProcessor.processDailyAdjusted(json, symbol);
			case TIME_SERIES_DAILY -> ApiProcessor.processDaily(json, symbol);
			default -> out;
		};


		return out;
	}
}
