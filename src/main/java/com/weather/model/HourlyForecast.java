package com.weather.model;

import java.time.LocalDateTime;

/**
 * HourlyForecast represents a single hour's meteorological observation or projection.
 *
 * Encapsulated fields:
 * - time: Timestamp of the hourly projection (local to target location)
 * - temperature: Projected ambient temperature at 2 meters (°C)
 * - apparentTemperature: Projected feels-like temperature (°C)
 * - weatherCode: WMO meteorological condition code
 */
public class HourlyForecast {

    private LocalDateTime time;
    private double temperature;
    private double apparentTemperature;
    private int weatherCode;

    /**
     * Default no-argument constructor.
     */
    public HourlyForecast() {
    }

    /**
     * Parameterized constructor.
     *
     * @param time                the local date-time of this hourly record
     * @param temperature         the 2m air temperature in Celsius
     * @param apparentTemperature the apparent (feels-like) temperature in Celsius
     * @param weatherCode         the numeric WMO weather condition code
     */
    public HourlyForecast(LocalDateTime time, double temperature, double apparentTemperature, int weatherCode) {
        this.time = time;
        this.temperature = temperature;
        this.apparentTemperature = apparentTemperature;
        this.weatherCode = weatherCode;
    }

    // Getters and Setters

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getApparentTemperature() {
        return apparentTemperature;
    }

    public void setApparentTemperature(double apparentTemperature) {
        this.apparentTemperature = apparentTemperature;
    }

    public int getWeatherCode() {
        return weatherCode;
    }

    public void setWeatherCode(int weatherCode) {
        this.weatherCode = weatherCode;
    }

    @Override
    public String toString() {
        return "HourlyForecast{" +
                "time=" + time +
                ", temperature=" + temperature +
                ", apparentTemperature=" + apparentTemperature +
                ", weatherCode=" + weatherCode +
                '}';
    }
}
