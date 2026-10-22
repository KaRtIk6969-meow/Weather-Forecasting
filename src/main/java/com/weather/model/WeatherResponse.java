package com.weather.model;

import java.util.ArrayList;
import java.util.List;

/**
 * WeatherResponse serves as a consolidated response container (Data Transfer Object).
 * 
 * It bundles together:
 * 1. Current real-time weather conditions (WeatherData).
 * 2. Multi-day daily forecast projections (List<Forecast>).
 * 
 * This enables the service layer to return a single coherent object to controllers and views.
 */
public class WeatherResponse {

    // 1. Private fields (Encapsulation)

    /** Current instantaneous weather conditions */
    private WeatherData currentWeather;

    /** Multi-day forecast collection (e.g., 7-day outlook) */
    private List<Forecast> forecasts;

    /** Hourly forecast collection (e.g., 24-hour progression) */
    private List<HourlyForecast> hourlyForecasts;

    // 2. Constructors

    /**
     * Default no-argument constructor.
     * Initializes empty forecast lists.
     */
    public WeatherResponse() {
        this.forecasts = new ArrayList<>();
        this.hourlyForecasts = new ArrayList<>();
    }

    /**
     * Parameterized constructor for current weather and daily forecasts.
     *
     * @param currentWeather the current weather object
     * @param forecasts      the list of daily forecast objects
     */
    public WeatherResponse(WeatherData currentWeather, List<Forecast> forecasts) {
        this(currentWeather, forecasts, new ArrayList<>());
    }

    /**
     * Parameterized constructor for current weather, daily forecasts, and hourly forecasts.
     *
     * @param currentWeather  the current weather object
     * @param forecasts       the list of daily forecast objects
     * @param hourlyForecasts the list of hourly forecast objects
     */
    public WeatherResponse(WeatherData currentWeather, List<Forecast> forecasts, List<HourlyForecast> hourlyForecasts) {
        this.currentWeather = currentWeather;
        this.forecasts = forecasts != null ? forecasts : new ArrayList<>();
        this.hourlyForecasts = hourlyForecasts != null ? hourlyForecasts : new ArrayList<>();
    }

    // 3. Getters and Setters

    public WeatherData getCurrentWeather() {
        return currentWeather;
    }

    public void setCurrentWeather(WeatherData currentWeather) {
        this.currentWeather = currentWeather;
    }

    public List<Forecast> getForecasts() {
        return forecasts;
    }

    public void setForecasts(List<Forecast> forecasts) {
        this.forecasts = forecasts != null ? forecasts : new ArrayList<>();
    }

    public List<HourlyForecast> getHourlyForecasts() {
        return hourlyForecasts;
    }

    public void setHourlyForecasts(List<HourlyForecast> hourlyForecasts) {
        this.hourlyForecasts = hourlyForecasts != null ? hourlyForecasts : new ArrayList<>();
    }

    // 4. toString method

    @Override
    public String toString() {
        return "WeatherResponse{" +
                "currentWeather=" + currentWeather +
                ", forecastsCount=" + (forecasts != null ? forecasts.size() : 0) +
                ", hourlyForecastsCount=" + (hourlyForecasts != null ? hourlyForecasts.size() : 0) +
                ", forecasts=" + forecasts +
                ", hourlyForecasts=" + hourlyForecasts +
                '}';
    }
}
