package com.weather.controller;

import com.weather.model.CityCoordinates;
import com.weather.model.WeatherResponse;
import com.weather.service.GeocodingService;
import com.weather.service.WeatherApiService;

/**
 * WeatherController coordinates user actions from the JavaFX View
 * with the underlying business services.
 * 
 * In accordance with MVC principles:
 * - It contains NO HTTP or network code (delegated to services).
 * - It contains NO JavaFX UI layout code (delegated to view).
 * - It receives user intent (city name), orchestrates services, and returns
 *   structured model data (WeatherResponse) to the view layer.
 */
public class WeatherController {

    private final GeocodingService geocodingService;
    private final WeatherApiService weatherApiService;

    /**
     * Default constructor initializing the required service components.
     */
    public WeatherController() {
        this.geocodingService = new GeocodingService();
        this.weatherApiService = new WeatherApiService();
    }

    /**
     * Parameterized constructor allowing service injection (useful for unit testing).
     *
     * @param geocodingService  the service used to convert city names to coordinates
     * @param weatherApiService the service used to fetch weather metrics
     */
    public WeatherController(GeocodingService geocodingService, WeatherApiService weatherApiService) {
        this.geocodingService = geocodingService;
        this.weatherApiService = weatherApiService;
    }

    /**
     * Searches live weather for the specified city name.
     *
     * Workflow:
     * 1. Validates the city input.
     * 2. Calls GeocodingService to obtain coordinates (latitude & longitude).
     * 3. Calls WeatherApiService using the resolved coordinates.
     * 4. Returns the complete WeatherResponse model.
     *
     * @param cityName the name of the city entered by the user
     * @return WeatherResponse containing live current weather and forecast,
     *         or null if the city could not be found or an error occurred
     */
    public WeatherResponse searchWeather(String cityName) {
        // Step 1: Input Validation
        if (cityName == null || cityName.trim().isEmpty()) {
            System.err.println("Controller Validation: City name cannot be empty.");
            return null;
        }

        String sanitizedCity = cityName.trim();

        // Step 2: Geocoding (Convert city name -> coordinates)
        CityCoordinates coordinates = geocodingService.findCoordinates(sanitizedCity);
        if (coordinates == null) {
            System.err.println("Controller: Coordinates not found for city: \"" + sanitizedCity + "\"");
            return null;
        }

        // Step 3: Fetch Weather (Coordinates -> live weather data)
        WeatherResponse weatherResponse = weatherApiService.fetchWeather(coordinates);
        if (weatherResponse == null) {
            System.err.println("Controller: Weather data could not be retrieved for: \"" + sanitizedCity + "\"");
            return null;
        }

        // Step 4: Return populated model to the caller (View)
        return weatherResponse;
    }
}
