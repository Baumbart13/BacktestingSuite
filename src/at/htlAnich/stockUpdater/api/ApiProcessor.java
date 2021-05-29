package at.htlAnich.stockUpdater.api;

import at.htlAnich.stockUpdater.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.time.LocalDate;

public class ApiProcessor {

	public static StockResults processDailyAdjusted(@NotNull JSONObject json, @NotNull String symbol){
		var results = new StockResults(symbol);
		json = json.getJSONObject(json.keys().next());

		for(var key : json.keySet()){
			//yyyy-mm-dd
			var dateTime = LocalDate.parse(key).atStartOfDay();

			//1. open
			//2. high
			//3. low
			//4. close
			//5. adjusted close
			//6. volume
			//7. divided amount
			//8. split coefficient
			StockDataPoint dataPoint = new StockDataPoint(dateTime);
			var item = json.getJSONObject(key);
			dataPoint.setValue(StockDataPoint.ValueType.open, item.getFloat("1. open"));
			dataPoint.setValue(StockDataPoint.ValueType.high, item.getFloat("2. high"));
			dataPoint.setValue(StockDataPoint.ValueType.low, item.getFloat("3. low"));
			dataPoint.setValue(StockDataPoint.ValueType.close, item.getFloat("5. adjusted close"));
			dataPoint.setValue(StockDataPoint.ValueType.volume, item.getFloat("6. volume"));
			dataPoint.setValue(StockDataPoint.ValueType.avg200, 0.0f);
			dataPoint.setValue(StockDataPoint.ValueType.splitCoefficient, item.getFloat("8. split coefficient"));
			results.addDataPoint(dataPoint);
		}

		return results;
	}

	public static StockResults processDaily(@NotNull JSONObject json, @NotNull String symbol){
		var results = new StockResults(symbol);
		json = json.getJSONObject(json.keys().next());

		for(var key : json.keySet()){
			//yyyy-mm-dd
			var dateTime = LocalDate.parse(key).atStartOfDay();

			//1. open
			//2. high
			//3. low
			//4. close
			//5. volume
			StockDataPoint dataPoint = new StockDataPoint(dateTime);
			var item = json.getJSONObject(key);
			dataPoint.setValue(StockDataPoint.ValueType.open, item.getFloat("1. open"));
			dataPoint.setValue(StockDataPoint.ValueType.high, item.getFloat("2. high"));
			dataPoint.setValue(StockDataPoint.ValueType.low, item.getFloat("3. low"));
			dataPoint.setValue(StockDataPoint.ValueType.close, item.getFloat("4. close"));
			dataPoint.setValue(StockDataPoint.ValueType.volume, item.getFloat("5. volume"));
			dataPoint.setValue(StockDataPoint.ValueType.avg200, 0.0f);
			dataPoint.setValue(StockDataPoint.ValueType.splitCoefficient, item.getFloat("8. split coefficient"));
			results.addDataPoint(dataPoint);
		}

		return results;
	}
}
