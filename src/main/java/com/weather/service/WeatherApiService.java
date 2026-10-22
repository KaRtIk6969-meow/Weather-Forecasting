package com.weather.service;

import com.weather.model.CityCoordinates;
import com.weather.model.Forecast;
import com.weather.model.WeatherData;
import com.weather.model.WeatherResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * WeatherApiService handles fetching real meteorological observations and multi-day
 * forecast projections from the Open-Meteo Forecast REST API.
 *
 * Responsibilities:
 * 1. Formulate request URLs using latitude and longitude from CityCoordinates.
 * 2. Execute HTTP GET requests using Java's HttpClient.
 * 3. Parse JSON payloads and transform them into WeatherData and Forecast objects.
 * 4. Return an aggregated WeatherResponse object.
 */
public class WeatherApiService {

    // Base REST endpoint for Open-Meteo Forecast API
    private static final String FORECAST_API_BASE_URL = "https://api.open-meteo.com/v1/forecast";

    // Reusable HttpClient instance
    private final HttpClient httpClient;

    /**
     * Initializes the service with a configured HttpClient and connection timeout.
     */
    public WeatherApiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Fetches current weather and a 7-day forecast for the given geographic coordinates.
     *
     * @param coordinates the CityCoordinates containing latitude, longitude, and city name
     * @return WeatherResponse containing WeatherData and List<Forecast>, or null on failure
     */
    public WeatherResponse fetchWeather(CityCoordinates coordinates) {
        if (coordinates == null) {
            System.err.println("Weather API Error: CityCoordinates cannot be null.");
            return null;
        }

        try {
            // 1. Build the Open-Meteo forecast query URL using US locale for decimal dots (.)
            // Query parameters requested:
            // - current: temperature_2m, relative_humidity_2m, apparent_temperature, weather_code, surface_pressure, wind_speed_10m
            // - daily: weather_code, temperature_2m_max, temperature_2m_min, relative_humidity_2m_mean, precipitation_probability_max, wind_speed_10m_max
            // - wind_speed_unit=kmh: ensures wind speed is returned in km/h
            // - timezone=auto: aligns timestamps with the city's local timezone
            // - forecast_days=7: retrieves approximately 7 days of daily outlook
            String requestUrl = String.format(Locale.US,
                    "%s?latitude=%.5f&longitude=%.5f" +
                    "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,surface_pressure,wind_speed_10m" +
                    "&daily=weather_code,temperature_2m_max,temperature_2m_min,relative_humidity_2m_mean,precipitation_probability_max,wind_speed_10m_max" +
                    "&wind_speed_unit=kmh&timezone=auto&forecast_days=7",
                    FORECAST_API_BASE_URL,
                    coordinates.getLatitude(),
                    coordinates.getLongitude()
            );

            // 2. Build the HTTP GET request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            // 3. Send the request synchronously
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 4. Validate HTTP status code
            if (response.statusCode() != 200) {
                System.err.println("Weather API Error: HTTP " + response.statusCode() + " received from Open-Meteo.");
                return null;
            }

            // 5. Parse JSON root object
            String responseBody = response.body();
            JSONObject root = new JSONObject(responseBody);

            // 6. Extract Current Weather section
            if (!root.has("current") || !root.has("daily")) {
                System.err.println("Weather API Error: Expected 'current' and 'daily' sections in response.");
                return null;
            }

            JSONObject currentJson = root.getJSONObject("current");
            WeatherData currentWeather = parseCurrentWeather(coordinates.getCityName(), currentJson);

            // 7. Extract Daily Forecast section
            JSONObject dailyJson = root.getJSONObject("daily");
            List<Forecast> forecasts = parseDailyForecasts(dailyJson);

            // 8. Return aggregated WeatherResponse
            return new WeatherResponse(currentWeather, forecasts);

        } catch (IOException e) {
            System.err.println("Network Error: Could not connect to Open-Meteo Weather API (" + e.getMessage() + ")");
            return null;
        } catch (InterruptedException e) {
            System.err.println("Network Error: Weather API request was interrupted (" + e.getMessage() + ")");
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            System.err.println("Error parsing weather API response: " + e.getMessage());
            return null;
        }
    }

    /**
     * Parses the 'current' JSON object into a WeatherData model.
     */
    private WeatherData parseCurrentWeather(String cityName, JSONObject currentJson) {
        double temperature = currentJson.getDouble("temperature_2m");
        double feelsLike = currentJson.getDouble("apparent_temperature");
        int humidity = currentJson.getInt("relative_humidity_2m");
        double windSpeed = currentJson.getDouble("wind_speed_10m");
        int pressure = (int) Math.round(currentJson.getDouble("surface_pressure"));
        int weatherCode = currentJson.getInt("weather_code");

        String condition = mapWeatherCondition(weatherCode);
        String description = mapWeatherDescription(weatherCode);

        // Parse observation timestamp (e.g., "2026-10-22T09:30") or default to now
        LocalDateTime timestamp;
        try {
            timestamp = LocalDateTime.parse(currentJson.getString("time"));
        } catch (Exception e) {
            timestamp = LocalDateTime.now();
        }

        return new WeatherData(
                cityName,
                temperature,
                feelsLike,
                humidity,
                windSpeed,
                pressure,
                condition,
                description,
                timestamp
        );
    }

    /**
     * Parses parallel arrays from the 'daily' JSON object into a List of Forecast models.
     */
    private List<Forecast> parseDailyForecasts(JSONObject dailyJson) {
        List<Forecast> forecastList = new ArrayList<>();

        JSONArray timeArray = dailyJson.getJSONArray("time");
        JSONArray maxTempArray = dailyJson.getJSONArray("temperature_2m_max");
        JSONArray minTempArray = dailyJson.getJSONArray("temperature_2m_min");
        JSONArray humidityArray = dailyJson.optJSONArray("relative_humidity_2m_mean");
        JSONArray precipProbArray = dailyJson.getJSONArray("precipitation_probability_max");
        JSONArray windSpeedArray = dailyJson.getJSONArray("wind_speed_10m_max");
        JSONArray weatherCodeArray = dailyJson.getJSONArray("weather_code");

        int daysCount = timeArray.length();

        for (int i = 0; i < daysCount; i++) {
            LocalDate date = LocalDate.parse(timeArray.getString(i));
            double maxTemp = maxTempArray.getDouble(i);
            double minTemp = minTempArray.getDouble(i);

            // Compute average temperature rounded to 1 decimal place
            double avgTemp = Math.round(((maxTemp + minTemp) / 2.0) * 10.0) / 10.0;

            int humidity = (humidityArray != null && i < humidityArray.length())
                    ? humidityArray.getInt(i) : 50;

            double windSpeed = windSpeedArray.getDouble(i);
            int precipProb = precipProbArray.getInt(i);
            int weatherCode = weatherCodeArray.getInt(i);

            String condition = mapWeatherCondition(weatherCode);
            String description = mapWeatherDescription(weatherCode);

            Forecast forecast = new Forecast(
                    date,
                    minTemp,
                    maxTemp,
                    avgTemp,
                    humidity,
                    windSpeed,
                    precipProb,
                    condition,
                    description
            );

            forecastList.add(forecast);
        }

        return forecastList;
    }

    /**
     * Maps WMO (World Meteorological Organization) weather codes to human-readable condition categories.
     *
     * @param code WMO numeric weather code
     * @return high-level condition name (e.g., "Clear", "Rain", "Thunderstorm")
     */
    private String mapWeatherCondition(int code) {
        if (code == 0) {
            return "Clear";
        } else if (code >= 1 && code <= 3) {
            return "Clouds";
        } else if (code == 45 || code == 48) {
            return "Fog";
        } else if (code >= 51 && code <= 57) {
            return "Drizzle";
        } else if (code >= 61 && code <= 67) {
            return "Rain";
        } else if (code >= 71 && code <= 77) {
            return "Snow";
        } else if (code >= 80 && code <= 82) {
            return "Rain Showers";
        } else if (code >= 95 && code <= 99) {
            return "Thunderstorm";
        } else {
            return "Unknown";
        }
    }

    /**
     * Maps WMO weather codes to detailed textual descriptions.
     *
     * @param code WMO numeric weather code
     * @return detailed description string
     */
    private String mapWeatherDescription(int code) {
        switch (code) {
            case 0:  return "Clear sky";
            case 1:  return "Mainly clear";
            case 2:  return "Partly cloudy";
            case 3:  return "Overcast clouds";
            case 45: return "Fog";
            case 48: return "Depositing rime fog";
            case 51: return "Light drizzle";
            case 53: return "Moderate drizzle";
            case 55: return "Dense drizzle";
            case 56:
            case 57: return "Freezing drizzle";
            case 61: return "Slight rain";
            case 63: return "Moderate rain";
            case 65: return "Heavy rain";
            case 66:
            case 67: return "Freezing rain";
            case 71: return "Slight snow fall";
            case 73: return "Moderate snow fall";
            case 75: return "Heavy snow fall";
            case 77: return "Snow grains";
            case 80: return "Slight rain showers";
            case 81: return "Moderate rain showers";
            case 82: return "Violent rain showers";
            case 95: return "Thunderstorm";
            case 96:
            case 99: return "Thunderstorm with hail";
            default: return "Weather code " + code;
        }
    }

    /**
     * Demonstration method:
     * 1. Uses the coordinates resolved for Chandigarh (Lat: 30.73629, Lon: 76.7884).
     * 2. Calls WeatherApiService.fetchWeather.
     * 3. Prints the live Current Weather.
     * 4. Prints all 7 daily Forecast objects.
     */
    public static void demonstrateWeatherApi() {
        System.out.println("=== Open-Meteo Weather API Service Demonstration ===");

        // Coordinates obtained from the previous Geocoding step for Chandigarh
        CityCoordinates chandigarhCoords = new CityCoordinates(
                "Chandigarh",
                "India",
                30.73629,
                76.7884
        );

        System.out.println("Querying live weather for: " + chandigarhCoords.getCityName() +
                " (" + chandigarhCoords.getLatitude() + "° N, " + chandigarhCoords.getLongitude() + "° E)...");

        WeatherApiService apiService = new WeatherApiService();
        WeatherResponse response = apiService.fetchWeather(chandigarhCoords);

        if (response != null) {
            System.out.println("\n✓ LIVE Current Weather Received:");
            WeatherData current = response.getCurrentWeather();
            System.out.println("  • City            : " + current.getCityName());
            System.out.println("  • Temperature     : " + current.getTemperature() + " °C");
            System.out.println("  • Feels Like      : " + current.getFeelsLike() + " °C");
            System.out.println("  • Humidity        : " + current.getHumidity() + " %");
            System.out.println("  • Wind Speed      : " + current.getWindSpeed() + " km/h");
            System.out.println("  • Pressure        : " + current.getPressure() + " hPa");
            System.out.println("  • Condition       : " + current.getCondition());
            System.out.println("  • Description     : " + current.getDescription());
            System.out.println("  • Timestamp       : " + current.getTimestamp());

            System.out.println("\n✓ LIVE 7-Day Forecast Received (" + response.getForecasts().size() + " days):");
            int dayNumber = 1;
            for (Forecast forecast : response.getForecasts()) {
                System.out.printf("  Day %d [%s]: Min %5.1f °C | Max %5.1f °C | Avg %5.1f °C | Hum %2d%% | Wind %4.1f km/h | Rain Prob %2d%% | %-12s (%s)%n",
                        dayNumber++,
                        forecast.getDate(),
                        forecast.getMinTemperature(),
                        forecast.getMaxTemperature(),
                        forecast.getAverageTemperature(),
                        forecast.getHumidity(),
                        forecast.getWindSpeed(),
                        forecast.getPrecipitationProbability(),
                        forecast.getCondition(),
                        forecast.getDescription()
                );
            }
        } else {
            System.err.println("✗ Failed to retrieve live weather data.");
        }
    }

    /**
     * Standalone main method to run the demonstration directly.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        demonstrateWeatherApi();
    }
}
