package com.weather.model;

import java.time.LocalDateTime;

/**
 * WeatherData represents a snapshot of weather conditions for a specific city.
 * 
 * This class follows the Plain Old Java Object (POJO) / JavaBean pattern:
 * - Data fields are private (Encapsulation).
 * - Accessed and modified through public getters and setters.
 * - Provides constructors for object creation.
 * - Overrides toString() for easy debugging and logging.
 */
public class WeatherData {

    // 1. Private fields (Encapsulation: protecting internal data from direct outside modification)
    
    /** The name of the city (e.g., "London", "Tokyo", "Delhi") */
    private String cityName;

    /** Current temperature in degrees Celsius (°C) */
    private double temperature;

    /** Human perceived temperature ("feels like") in degrees Celsius (°C) */
    private double feelsLike;

    /** Relative humidity percentage (0% to 100%) */
    private int humidity;

    /** Wind speed in kilometers per hour (km/h) */
    private double windSpeed;

    /** Atmospheric pressure in hectopascals (hPa) / millibars */
    private int pressure;

    /** Main weather category (e.g., "Clear", "Rain", "Clouds", "Snow") */
    private String condition;

    /** Detailed weather description (e.g., "light rain", "scattered clouds") */
    private String description;

    /** Timestamp when the weather data was measured or recorded */
    private LocalDateTime timestamp;

    // 2. Constructors

    /**
     * Default no-argument constructor.
     * Initializes an empty WeatherData object.
     */
    public WeatherData() {
    }

    /**
     * Parameterized constructor to initialize all weather data fields at once.
     *
     * @param cityName    name of the city
     * @param temperature temperature in Celsius
     * @param feelsLike   feels-like temperature in Celsius
     * @param humidity    humidity percentage (0-100)
     * @param windSpeed   wind speed in km/h
     * @param pressure    atmospheric pressure in hPa
     * @param condition   general weather condition (e.g., "Rain")
     * @param description detailed description (e.g., "light rain")
     * @param timestamp   date and time of the observation
     */
    public WeatherData(String cityName, double temperature, double feelsLike, int humidity,
                       double windSpeed, int pressure, String condition, String description,
                       LocalDateTime timestamp) {
        this.cityName = cityName;
        this.temperature = temperature;
        this.feelsLike = feelsLike;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.pressure = pressure;
        this.condition = condition;
        this.description = description;
        this.timestamp = timestamp;
    }

    // 3. Getters and Setters

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(double feelsLike) {
        this.feelsLike = feelsLike;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public int getPressure() {
        return pressure;
    }

    public void setPressure(int pressure) {
        this.pressure = pressure;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // 4. toString method (Provides human-readable representation for debugging and console logs)

    @Override
    public String toString() {
        return "WeatherData{" +
                "cityName='" + cityName + '\'' +
                ", temperature=" + temperature + " °C" +
                ", feelsLike=" + feelsLike + " °C" +
                ", humidity=" + humidity + "%" +
                ", windSpeed=" + windSpeed + " km/h" +
                ", pressure=" + pressure + " hPa" +
                ", condition='" + condition + '\'' +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
