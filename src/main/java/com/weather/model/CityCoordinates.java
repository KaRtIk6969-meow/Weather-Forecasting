package com.weather.model;

/**
 * CityCoordinates represents the geographic location of a city on Earth.
 * 
 * Geographic coordinates (latitude and longitude) uniquely identify any spot on the globe.
 * Weather forecast providers (e.g., Open-Meteo, OpenWeatherMap) use these coordinates
 * to query precise numerical meteorological models.
 */
public class CityCoordinates {

    // 1. Private fields (Encapsulation: internal state is hidden from direct external mutation)

    /** Name of the city (e.g., "Chandigarh", "London") */
    private String cityName;

    /** Country or territory name/code (e.g., "India", "IN", "United Kingdom") */
    private String country;

    /**
     * Latitude coordinate in decimal degrees.
     * Ranges from -90.0° (South Pole) to +90.0° (North Pole). Equator is 0.0°.
     */
    private double latitude;

    /**
     * Longitude coordinate in decimal degrees.
     * Ranges from -180.0° (West) to +180.0° (East). Prime Meridian (Greenwich) is 0.0°.
     */
    private double longitude;

    // 2. Constructors

    /**
     * Default no-argument constructor.
     * Initializes an empty CityCoordinates instance.
     */
    public CityCoordinates() {
    }

    /**
     * Parameterized constructor to initialize all location fields.
     *
     * @param cityName  the name of the city
     * @param country   the country where the city is located
     * @param latitude  the north-south coordinate (-90.0 to +90.0)
     * @param longitude the east-west coordinate (-180.0 to +180.0)
     */
    public CityCoordinates(String cityName, String country, double latitude, double longitude) {
        this.cityName = cityName;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // 3. Getters and Setters

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // 4. toString method (Human-readable representation)

    @Override
    public String toString() {
        return "CityCoordinates{" +
                "cityName='" + cityName + '\'' +
                ", country='" + country + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    // 5. Demonstration Method

    /**
     * Demonstrates creating and using a CityCoordinates object for Chandigarh, India.
     */
    public static void demonstrateCityCoordinates() {
        // Create an example location for Chandigarh, India
        // Geographic coordinates: Latitude 30.7333° N, Longitude 76.7794° E
        CityCoordinates chandigarh = new CityCoordinates(
                "Chandigarh",
                "India",
                30.7333,
                76.7794
        );

        System.out.println("=== City Coordinates Demonstration ===");
        System.out.println("City: " + chandigarh.getCityName());
        System.out.println("Country: " + chandigarh.getCountry());
        System.out.println("Latitude: " + chandigarh.getLatitude() + "° N");
        System.out.println("Longitude: " + chandigarh.getLongitude() + "° E");
        System.out.println("Object Representation: " + chandigarh);
    }

    /**
     * Small entry point to run the demonstration directly.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        demonstrateCityCoordinates();
    }
}
