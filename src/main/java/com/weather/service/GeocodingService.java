package com.weather.service;

import com.weather.model.CityCoordinates;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * GeocodingService handles communication with the Open-Meteo Geocoding API.
 * 
 * Its sole responsibility is to translate a textual city name (e.g., "Chandigarh", "London")
 * into geographic coordinates (latitude and longitude) wrapped inside a CityCoordinates object.
 */
public class GeocodingService {

    // Base URL for Open-Meteo Geocoding REST endpoint
    private static final String GEOCODING_API_BASE_URL = "https://geocoding-api.open-meteo.com/v1/search";

    // Modern, reusable HTTP client introduced in Java 11
    private final HttpClient httpClient;

    /**
     * Constructor initializing the HttpClient with a standard connection timeout.
     */
    public GeocodingService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Fetches geographic coordinates for a given city name from Open-Meteo Geocoding API.
     *
     * @param cityName the name of the city to look up
     * @return CityCoordinates object with latitude, longitude, name, and country,
     *         or null if not found or if an error occurs
     */
    public CityCoordinates findCoordinates(String cityName) {
        // Validation: Ensure input is not null or whitespace
        if (cityName == null || cityName.trim().isEmpty()) {
            System.err.println("Geocoding Error: City name cannot be empty.");
            return null;
        }

        try {
            // 1. URL encode the city name to handle spaces and special characters safely
            String encodedCity = URLEncoder.encode(cityName.trim(), StandardCharsets.UTF_8);

            // 2. Construct the complete endpoint URL:
            // - name: query string for city search
            // - count=1: retrieve only the top/most relevant match
            // - language=en: English names for countries/cities
            // - format=json: structured JSON response
            String endpointUrl = String.format("%s?name=%s&count=1&language=en&format=json",
                    GEOCODING_API_BASE_URL, encodedCity);

            // 3. Build HTTP GET request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpointUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            // 4. Send synchronous HTTP GET request and receive the response body as a String
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 5. Verify HTTP Status code (200 = Success)
            if (response.statusCode() != 200) {
                System.err.println("Geocoding API Error: HTTP " + response.statusCode() + " received.");
                return null;
            }

            // 6. Parse JSON using org.json
            String jsonBody = response.body();
            JSONObject root = new JSONObject(jsonBody);

            // 7. Check if results array exists and has at least one entry
            if (!root.has("results")) {
                System.out.println("No geocoding results found for city: \"" + cityName + "\"");
                return null;
            }

            JSONArray results = root.getJSONArray("results");
            if (results.isEmpty()) {
                System.out.println("No geocoding results found for city: \"" + cityName + "\"");
                return null;
            }

            // 8. Extract the first (most relevant) result
            JSONObject firstMatch = results.getJSONObject(0);
            String resolvedCity = firstMatch.getString("name");
            String country = firstMatch.optString("country", "Unknown");
            double latitude = firstMatch.getDouble("latitude");
            double longitude = firstMatch.getDouble("longitude");

            // 9. Return the populated CityCoordinates model
            return new CityCoordinates(resolvedCity, country, latitude, longitude);

        } catch (IOException e) {
            // Handles network connectivity failures, DNS resolution issues, or dropped connections
            System.err.println("Network Error: Could not reach Open-Meteo Geocoding API (" + e.getMessage() + ")");
            return null;
        } catch (InterruptedException e) {
            // Handles thread interruption during network transmission
            System.err.println("Network Error: Request was interrupted (" + e.getMessage() + ")");
            Thread.currentThread().interrupt(); // Restore interrupted status
            return null;
        } catch (Exception e) {
            // Handles unexpected JSON parsing or format errors
            System.err.println("Error processing geocoding response: " + e.getMessage());
            return null;
        }
    }

    /**
     * Demonstration method that calls the geocoding service with "Chandigarh"
     * and prints the extracted geographic coordinates.
     */
    public static void demonstrateGeocoding() {
        System.out.println("=== Open-Meteo Geocoding Service Demonstration ===");
        GeocodingService service = new GeocodingService();

        String cityToSearch = "Chandigarh";
        System.out.println("Querying API for city: \"" + cityToSearch + "\"...");

        CityCoordinates coords = service.findCoordinates(cityToSearch);

        if (coords != null) {
            System.out.println("\n✓ Geocoding Result Received:");
            System.out.println("  • City Name : " + coords.getCityName());
            System.out.println("  • Country   : " + coords.getCountry());
            System.out.println("  • Latitude  : " + coords.getLatitude() + "°");
            System.out.println("  • Longitude : " + coords.getLongitude() + "°");
            System.out.println("  • Object    : " + coords);
        } else {
            System.out.println("✗ Failed to retrieve coordinates for \"" + cityToSearch + "\"");
        }
    }

    /**
     * Standalone main method to test the service directly.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        demonstrateGeocoding();
    }
}
