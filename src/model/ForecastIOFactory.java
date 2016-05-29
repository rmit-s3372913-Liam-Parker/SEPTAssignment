package model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ForecastIOFactory implements ForecastFactory {

	public static Logger logger = Logger.getLogger("Forecast IO Factory");
	
	String lat, lon;

	public ForecastIOFactory(String lat, String lon) {
		this.lat = lat;
		this.lon = lon;
	}

	@Override
	public HashMap<Date, ForecastDataPoint> GetWeatherForecast()
	{
		String json = "";
		final String forecastAPIString = "https://api.forecast.io/forecast/e75c50c1bc3a622832329f007ff1ab4b/";
		HashMap<Date, ForecastDataPoint> data = new HashMap<>();

		String finalUrl = forecastAPIString + lat + "," + lon;
		try {
			// Scrapes from the JSON file URL that has been assigned to
			// WeatherStation
			Scanner sc = new Scanner(new URL(finalUrl).openStream());
			while (sc.hasNext()) {
				json += sc.nextLine();
			}

			sc.close();
		} catch (IOException e) {
			logger.log(Level.WARNING, "Couldn't establish a connection with weather server at " + forecastAPIString);
			return data;
		}

		JSONObject root = new JSONObject(json);
		JSONObject observations = root.getJSONObject("daily");
		JSONArray entriesArray = observations.getJSONArray("data");

		Iterator<Object> entriesItr = entriesArray.iterator();

		// Loop for all recording entries the data block contains.
        // Each element represents aggregate weather data for a single day.
        // https://developer.forecast.io/docs/v2
		while (entriesItr.hasNext()) {
			JSONObject entry = (JSONObject) entriesItr.next();
		
			//Summary
			long unixTime = entry.optLong("time");
			Date date = new Date(unixTime*1000L);
			String summary = entry.optString("summary");

			// Temperatures and humidity
			float temp = (float)entry.optDouble("temp");
			int relHum = (int)(entry.optDouble("humidity")*100);

			// Wind & gust
			float windDir = (float)entry.optDouble("windBearing");
			float windSpeedKmh = (float)(entry.optDouble("windSpeed")* MPH_2_KMH);

			// Pressure
			float pressure = (float)entry.optDouble("pressure");
			
			ForecastDataPoint dataPoint = new ForecastDataPoint(date, summary, temp, relHum, windDir, 
					windSpeedKmh, pressure);

			// Add the entry to this station's hashmap
			data.put(date, dataPoint);
		}

		return data;
	}
}
