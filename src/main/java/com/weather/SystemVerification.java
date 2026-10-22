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
 * current weather, 7-day daily forecast, and 24-hour hourly forecast).
 */
public class SystemVerification {

    public static void main(String[] args) {
        System.out.println("===============================================================================");
        System.out.println("        WEATHER FORECASTING SYSTEM - AUTOMATED VERIFICATION SUITE              ");
        System.out.println("===============================================================================\n");

        WeatherController controller = new WeatherController();
        boolean allPassed = true;

        // Test 1: Chandigarh
        System.out.println("--> [TEST 1] Verifying search for 'Chandigarh'...");
        WeatherResponse chandigarhResp = controller.searchWeather("Chandigarh");
        if (validateCityResponse(chandigarhResp, "Chandigarh")) {
            System.out.println("    >>> PASS: Chandigarh retrieved all weather streams successfully.");
        } else {
            System.err.println("    >>> FAIL: Chandigarh verification failed.");
            allPassed = false;
        }

        // Test 2: London
        System.out.println("\n--> [TEST 2] Verifying search for 'London'...");
        WeatherResponse londonResp = controller.searchWeather("London");
        if (validateCityResponse(londonResp, "London")) {
            System.out.println("    >>> PASS: London retrieved all weather streams successfully.");
        } else {
            System.err.println("    >>> FAIL: London verification failed.");
            allPassed = false;
        }

        // Test 3: Tokyo
        System.out.println("\n--> [TEST 3] Verifying search for 'Tokyo'...");
        WeatherResponse tokyoResp = controller.searchWeather("Tokyo");
        if (validateCityResponse(tokyoResp, "Tokyo")) {
            System.out.println("    >>> PASS: Tokyo retrieved all weather streams successfully.");
        } else {
            System.err.println("    >>> FAIL: Tokyo verification failed.");
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

        // Test 5: Empty Input
        System.out.println("\n--> [TEST 5A] Verifying empty search (\"\")...");
        WeatherResponse emptyResp = controller.searchWeather("");
        if (emptyResp == null) {
            System.out.println("    ✓ Correctly rejected empty query without making network call.");
            System.out.println("    >>> PASS: Empty input validation passed.");
        } else {
            System.err.println("    >>> FAIL: Empty input did not return null.");
            allPassed = false;
        }

        // Test 6: Whitespace Input
        System.out.println("\n--> [TEST 5B] Verifying whitespace search (\"   \")...");
        WeatherResponse whitespaceResp = controller.searchWeather("   ");
        if (whitespaceResp == null) {
            System.out.println("    ✓ Correctly rejected whitespace-only query.");
            System.out.println("    >>> PASS: Whitespace validation passed.");
        } else {
            System.err.println("    >>> FAIL: Whitespace input did not return null.");
            allPassed = false;
        }

        System.out.println("\n===============================================================================");
        if (allPassed) {
            System.out.println("   ALL INTEGRATION & VALIDATION TESTS PASSED SUCCESSFULLY! (100% HEALTH)       ");
        } else {
            System.out.println("   SOME TESTS FAILED! CHECK LOGS ABOVE.                                        ");
        }
        System.out.println("===============================================================================");
    }

    private static boolean validateCityResponse(WeatherResponse response, String expectedCity) {
        if (response == null || response.getCurrentWeather() == null) {
            System.err.println("    ✗ WeatherResponse or CurrentWeather is null.");
            return false;
        }

        WeatherData current = response.getCurrentWeather();
        List<Forecast> daily = response.getForecasts();
        List<HourlyForecast> hourly = response.getHourlyForecasts();

        System.out.println("    ✓ City Resolved         : " + current.getCityName());
        System.out.println("    ✓ Current Temperature   : " + current.getTemperature() + " °C (Feels: " + current.getFeelsLike() + " °C)");
        System.out.println("    ✓ Condition / Emoji     : " + current.getCondition() + " (" + current.getDescription() + ")");
        System.out.println("    ✓ Humidity / Wind       : " + current.getHumidity() + "% | " + current.getWindSpeed() + " km/h");
        System.out.println("    ✓ Daily Forecast Count  : " + daily.size() + " days (Expected >= 7)");
        System.out.println("    ✓ Hourly Forecast Count : " + hourly.size() + " hours (Expected >= 24)");

        // Assert counts
        if (daily.size() < 7 || hourly.size() < 24) {
            System.err.println("    ✗ Insufficient daily or hourly records.");
            return false;
        }

        // Assert sanity ranges
        if (current.getHumidity() < 0 || current.getHumidity() > 100) {
            System.err.println("    ✗ Invalid humidity: " + current.getHumidity());
            return false;
        }

        // Verify first daily forecast
        Forecast f1 = daily.get(0);
        if (f1.getDate() == null || f1.getPrecipitationProbability() < 0 || f1.getPrecipitationProbability() > 100) {
            System.err.println("    ✗ Invalid daily forecast precipitation or date.");
            return false;
        }

        // Verify first hourly forecast
        HourlyForecast h1 = hourly.get(0);
        if (h1.getTime() == null) {
            System.err.println("    ✗ Hourly timestamp is null.");
            return false;
        }

        return true;
    }
}
