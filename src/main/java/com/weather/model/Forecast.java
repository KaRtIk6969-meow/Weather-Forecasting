package com.weather.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Forecast represents a single day's projected weather conditions.
 *
 * Unlike WeatherData (which captures real-time instantaneous observations),
 * Forecast holds aggregated metrics over an entire day, such as daily minimum,
 * maximum, and average temperatures, along with precipitation probability.
 */
public class Forecast {

    // 1. Private fields (Encapsulation)

    /** The date of the forecast (e.g., 2026-10-23) */
    private LocalDate date;

    /** Expected minimum temperature in Celsius (°C) */
    private double minTemperature;

    /** Expected maximum temperature in Celsius (°C) */
    private double maxTemperature;

    /** Estimated average temperature in Celsius (°C) */
    private double averageTemperature;

    /** Expected average relative humidity percentage (0% to 100%) */
    private int humidity;

    /** Expected wind speed in kilometers per hour (km/h) */
    private double windSpeed;

    /** Probability of precipitation (rain/snow) as a percentage (0% to 100%) */
    private int precipitationProbability;

    /** General expected weather condition (e.g., "Clear", "Rain", "Clouds") */
    private String condition;

    /** Detailed weather description (e.g., "scattered showers", "sunny intervals") */
    private String description;

    // 2. Constructors

    /**
     * Default no-argument constructor.
     * Initializes an empty Forecast instance.
     */
    public Forecast() {
    }

    /**
     * Parameterized constructor to initialize all forecast fields.
     *
     * @param date                     the forecast date
     * @param minTemperature           minimum temperature in Celsius
     * @param maxTemperature           maximum temperature in Celsius
     * @param averageTemperature       average temperature in Celsius
     * @param humidity                 humidity percentage (0-100)
     * @param windSpeed                wind speed in km/h
     * @param precipitationProbability probability of precipitation (0-100%)
     * @param condition                general weather condition
     * @param description              detailed description
     */
    public Forecast(LocalDate date, double minTemperature, double maxTemperature,
                    double averageTemperature, int humidity, double windSpeed,
                    int precipitationProbability, String condition, String description) {
        this.date = date;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.averageTemperature = averageTemperature;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.precipitationProbability = precipitationProbability;
        this.condition = condition;
        this.description = description;
    }

    // 3. Getters and Setters

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    /** Alias for getMinTemperature() */
    public double getMinTemp() {
        return minTemperature;
    }

    public void setMinTemperature(double minTemperature) {
        this.minTemperature = minTemperature;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    /** Alias for getMaxTemperature() */
    public double getMaxTemp() {
        return maxTemperature;
    }

    public void setMaxTemperature(double maxTemperature) {
        this.maxTemperature = maxTemperature;
    }

    public double getAverageTemperature() {
        return averageTemperature;
    }

    public void setAverageTemperature(double averageTemperature) {
        this.averageTemperature = averageTemperature;
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

    public int getPrecipitationProbability() {
        return precipitationProbability;
    }

    public void setPrecipitationProbability(int precipitationProbability) {
        this.precipitationProbability = precipitationProbability;
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

    // 4. toString method

    @Override
    public String toString() {
        return "Forecast{" +
                "date=" + date +
                ", minTemp=" + minTemperature + " °C" +
                ", maxTemp=" + maxTemperature + " °C" +
                ", avgTemp=" + averageTemperature + " °C" +
                ", humidity=" + humidity + "%" +
                ", windSpeed=" + windSpeed + " km/h" +
                ", precipitation=" + precipitationProbability + "%" +
                ", condition='" + condition + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    // 5. Demonstration Method

    /**
     * Demonstrates:
     * 1. Creating multiple Forecast objects.
     * 2. Storing them inside a generic List<Forecast>.
     * 3. Iterating through the list using an enhanced for-loop.
     * 4. Printing each forecast to the console.
     */
    public static void demonstrateForecastList() {
        // Step 1: Create a List to store Forecast objects (using ArrayList implementation)
        List<Forecast> forecastList = new ArrayList<>();

        // Step 2: Create at least 3 Forecast objects for upcoming days
        LocalDate today = LocalDate.now();

        Forecast day1 = new Forecast(
                today.plusDays(1),
                16.0, 25.0, 20.5,
                50, 14.2, 10,
                "Clear", "Sunny with light breeze"
        );

        Forecast day2 = new Forecast(
                today.plusDays(2),
                18.5, 27.0, 22.8,
                72, 21.0, 65,
                "Rain", "Scattered thunderstorms expected"
        );

        Forecast day3 = new Forecast(
                today.plusDays(3),
                14.0, 21.5, 17.7,
                60, 16.5, 20,
                "Clouds", "Partly cloudy with mild conditions"
        );

        // Step 3: Add the Forecast objects into the list
        forecastList.add(day1);
        forecastList.add(day2);
        forecastList.add(day3);

        // Step 4: Loop through the List and print each Forecast
        System.out.println("=== 3-Day Weather Forecast Demonstration ===");
        for (Forecast forecast : forecastList) {
            System.out.println(forecast);
        }
    }

    /**
     * Small entry point to run the demonstration independently if needed.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        demonstrateForecastList();
    }
}
