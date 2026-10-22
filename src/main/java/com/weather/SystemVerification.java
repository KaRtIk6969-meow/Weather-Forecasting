package com.weather;

import com.weather.controller.WeatherController;
import com.weather.model.Forecast;
import com.weather.model.HourlyForecast;
import com.weather.model.WeatherData;
import com.weather.model.WeatherResponse;

import java.util.List;

/**
 * SystemVerification performs an automated end-to-end verification of the backend services,
 * controller orchestration, live Open-Meteo geocoding and forecast retrieval (including
 * current weather, 7-day forecast, and 24-hour hourly forecast).
 */
public class SystemVerification {

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("   WEATHER FORECASTING SYSTEM - AUTOMATED VERIFICATION SUITE   ");
        System.out.println("===============================================================\n");

        WeatherController controller = new WeatherController();
        boolean allPassed = true;

        // Test 1: Chandigarh
        System.out.println("--> [TEST 1] Verifying search for 'Chandigarh'...");
        WeatherResponse chandigarhResp = controller.searchWeather("Chandigarh");
        if (chandigarhResp != null && chandigarhResp.getCurrentWeather() != null) {
            WeatherData current = chandigarhResp.getCurrentWeather();
            List<Forecast> daily = chandigarhResp.getForecasts();
            List<HourlyForecast> hourly = chandigarhResp.getHourlyForecasts();

            System.out.println("    ✓ Current Weather: " + current.getCityName() + " | Temp: " + current.getTemperature() + " °C | Condition: " + current.getCondition());
            System.out.println("    ✓ Daily Forecast Count: " + daily.size() + " days");
            System.out.println("    ✓ Hourly Forecast Count: " + hourly.size() + " hours");
            if (daily.size() >= 7 && hourly.size() >= 24) {
                System.out.println("    >>> PASS: Chandigarh retrieved all weather streams successfully.");
            } else {
                System.err.println("    >>> FAIL: Expected at least 7 daily and 24 hourly records.");
                allPassed = false;
            }
        } else {
            System.err.println("    >>> FAIL: Chandigarh weather response was null.");
            allPassed = false;
        }

        // Test 2: London
        System.out.println("\n--> [TEST 2] Verifying search for 'London'...");
        WeatherResponse londonResp = controller.searchWeather("London");
        if (londonResp != null && londonResp.getCurrentWeather() != null) {
            System.out.println("    ✓ Current Weather: " + londonResp.getCurrentWeather().getCityName() + " | Temp: " + londonResp.getCurrentWeather().getTemperature() + " °C");
            System.out.println("    ✓ Daily Forecast Count: " + londonResp.getForecasts().size() + " days");
            System.out.println("    ✓ Hourly Forecast Count: " + londonResp.getHourlyForecasts().size() + " hours");
            System.out.println("    >>> PASS: London retrieved successfully.");
        } else {
            System.err.println("    >>> FAIL: London weather response was null.");
            allPassed = false;
        }

        // Test 3: Tokyo
        System.out.println("\n--> [TEST 3] Verifying search for 'Tokyo'...");
        WeatherResponse tokyoResp = controller.searchWeather("Tokyo");
        if (tokyoResp != null && tokyoResp.getCurrentWeather() != null) {
            System.out.println("    ✓ Current Weather: " + tokyoResp.getCurrentWeather().getCityName() + " | Temp: " + tokyoResp.getCurrentWeather().getTemperature() + " °C");
            System.out.println("    ✓ Daily Forecast Count: " + tokyoResp.getForecasts().size() + " days");
            System.out.println("    ✓ Hourly Forecast Count: " + tokyoResp.getHourlyForecasts().size() + " hours");
            System.out.println("    >>> PASS: Tokyo retrieved successfully.");
        } else {
            System.err.println("    >>> FAIL: Tokyo weather response was null.");
            allPassed = false;
        }

        // Test 4: Invalid City Name
        System.out.println("\n--> [TEST 4] Verifying invalid city 'InvalidCityXYZ98765'...");
        WeatherResponse invalidResp = controller.searchWeather("InvalidCityXYZ98765");
        if (invalidResp == null) {
            System.out.println("    ✓ Correctly returned null for non-existent city.");
            System.out.println("    >>> PASS: Invalid city handled gracefully.");
        } else {
            System.err.println("    >>> FAIL: Expected null for invalid city.");
            allPassed = false;
        }

        // Test 5: Empty Input and Whitespace
        System.out.println("\n--> [TEST 5] Verifying empty and whitespace searches...");
        WeatherResponse emptyResp = controller.searchWeather("");
        WeatherResponse whitespaceResp = controller.searchWeather("   ");
        if (emptyResp == null && whitespaceResp == null) {
            System.out.println("    ✓ Correctly rejected empty and whitespace-only queries.");
            System.out.println("    >>> PASS: Empty input validation passed.");
        } else {
            System.err.println("    >>> FAIL: Empty input did not return null.");
            allPassed = false;
        }

        System.out.println("\n===============================================================");
        if (allPassed) {
            System.out.println("   ALL 5 INTEGRATION TESTS PASSED SUCCESSFULLY! (100% HEALTH)  ");
        } else {
            System.out.println("   SOME TESTS FAILED! CHECK LOGS ABOVE.                        ");
        }
        System.out.println("===============================================================");
    }
}
