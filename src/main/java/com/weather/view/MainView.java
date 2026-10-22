package com.weather.view;

import com.weather.controller.WeatherController;
import com.weather.model.Forecast;
import com.weather.model.HourlyForecast;
import com.weather.model.WeatherData;
import com.weather.model.WeatherResponse;
import com.weather.service.WeatherApiService;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * MainView is responsible for building and managing the complete user interface.
 *
 * Professional UI Architecture & Polish:
 * 1. Responsive Scrollable Viewport (vertical ScrollPane prevents any viewport clipping).
 * 2. Elegant Search Header with clear hover states and keyboard trigger (ENTER).
 * 3. Modern Current Weather Card with clear visual hierarchy, metric tiles, and typography.
 * 4. 24-Hour Forecast Section with horizontal scrolling, "NOW" badge, and hourly condition cards.
 * 5. Interactive 24-Hour Temperature LineChart plotting actual hourly temperatures.
 * 6. Horizontal Scrollable 7-Day Forecast Cards with hover elevation and "Today" badges.
 * 7. Interactive 7-Day Temperature LineChart (Max & Min curves).
 * 8. Interactive 7-Day Precipitation BarChart (0-100% rain probability).
 * 9. Subtle weather-based dynamic background gradients reflecting live atmospheric conditions.
 * 10. Asynchronous background loading with disabled button states, stale-data protection, and comprehensive error handling.
 */
public class MainView {

    // Controller reference to coordinate backend services
    private final WeatherController controller;

    // UI Input controls
    private TextField cityInputField;
    private Button searchButton;
    private boolean isSearching = false;

    // UI Status / Error notification label
    private Label statusMessageLabel;

    // Top-level containers for dynamic background styling
    private ScrollPane mainScrollPane;
    private HBox centerWrapper;
    private VBox rootContent;

    // UI Output components for Current Weather
    private Label cityNameHeaderLabel;
    private Label temperatureValueLabel;
    private Label feelsLikeLabel;
    private Label conditionEmojiLabel;
    private Label conditionTextLabel;
    private Label descriptionLabel;
    private Label humidityTileLabel;
    private Label windSpeedTileLabel;
    private Label pressureTileLabel;
    private Label observationTimeLabel;

    // UI Containers for 24-Hour Hourly Forecast
    private HBox hourlyForecastCardsContainer;
    private LineChart<String, Number> hourlyTemperatureChart;

    // UI Containers for 7-Day Daily Forecast
    private HBox forecastCardsContainer;
    private LineChart<String, Number> temperatureChart;
    private BarChart<String, Number> precipitationChart;

    // Date & Time Formatters
    private static final DateTimeFormatter HOURLY_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("h a", Locale.ENGLISH);

    private static final DateTimeFormatter CARD_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH);

    private static final DateTimeFormatter CHART_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH);

    // CSS Styling Constants for Consistent Aesthetic Polish
    private static final String BTN_DEFAULT_STYLE =
            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-pref-height: 42px; " +
            "-fx-background-color: #2563eb; -fx-text-fill: white; -fx-padding: 0 24px; " +
            "-fx-background-radius: 8px; -fx-cursor: hand;";

    private static final String BTN_HOVER_STYLE =
            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-pref-height: 42px; " +
            "-fx-background-color: #1d4ed8; -fx-text-fill: white; -fx-padding: 0 24px; " +
            "-fx-background-radius: 8px; -fx-cursor: hand;";

    private static final String CARD_DEFAULT_STYLE =
            "-fx-background-color: white; -fx-background-radius: 10px; " +
            "-fx-border-color: #e2e8f0; -fx-border-radius: 10px; -fx-border-width: 1px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 6, 0, 0, 2);";

    private static final String CARD_HOVER_STYLE =
            "-fx-background-color: #f8fafc; -fx-background-radius: 10px; " +
            "-fx-border-color: #93c5fd; -fx-border-radius: 10px; -fx-border-width: 1px; " +
            "-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.15), 10, 0, 0, 4);";

    /**
     * Constructor initializing the View with its corresponding Controller.
     */
    public MainView() {
        this.controller = new WeatherController();
    }

    /**
     * Builds and returns the root JavaFX Parent node for the scene.
     *
     * @return Parent container holding the complete UI layout
     */
    public Parent getView() {
        // 1. Application Header Title & Subtitle
        Label appTitleLabel = new Label("Weather Forecasting & Visualization System");
        appTitleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label appSubtitleLabel = new Label("MVC Architecture • Live Weather API • Hourly & 7-Day Analytics");
        appSubtitleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");

        VBox headerBox = new VBox(4, appTitleLabel, appSubtitleLabel);
        headerBox.setAlignment(Pos.CENTER);

        // 2. City Input Field & Search Button
        cityInputField = new TextField();
        cityInputField.setPromptText("Enter city name (e.g. Chandigarh, London, Tokyo)...");
        cityInputField.setPrefWidth(360);
        cityInputField.setStyle(
                "-fx-font-size: 14px; -fx-pref-height: 42px; -fx-padding: 0 14px; " +
                "-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; " +
                "-fx-background-radius: 8px;"
        );

        searchButton = new Button("Search Weather");
        searchButton.setStyle(BTN_DEFAULT_STYLE);

        // Add subtle hover micro-interaction to search button
        searchButton.setOnMouseEntered(e -> {
            if (!searchButton.isDisabled()) {
                searchButton.setStyle(BTN_HOVER_STYLE);
            }
        });
        searchButton.setOnMouseExited(e -> {
            if (!searchButton.isDisabled()) {
                searchButton.setStyle(BTN_DEFAULT_STYLE);
            }
        });

        // Event handlers: Trigger search on Button click or pressing ENTER in the TextField
        searchButton.setOnAction(event -> performSearch());
        cityInputField.setOnAction(event -> performSearch());

        HBox searchBar = new HBox(12, cityInputField, searchButton);
        searchBar.setAlignment(Pos.CENTER);

        // 3. Status / Error Message Label
        statusMessageLabel = new Label("Enter a city name above and click 'Search Weather'.");
        statusMessageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");
        statusMessageLabel.setWrapText(true);

        // 4. Current Weather Card
        VBox currentWeatherCard = createCurrentWeatherCard();

        // 5. 24-Hour Forecast Section (Cards & Hourly Chart)
        VBox hourlySection = createHourlyForecastSection();
        VBox hourlyTempChartContainer = createHourlyTemperatureChart();

        // 6. 7-Day Forecast Section (Cards & Daily Charts)
        VBox forecastSection = createForecastSection();
        VBox tempChartContainer = createTemperatureChart();
        VBox precipChartContainer = createPrecipitationChart();

        // 7. Main Vertical Content Box
        rootContent = new VBox(22,
                headerBox,
                searchBar,
                statusMessageLabel,
                currentWeatherCard,
                hourlySection,
                hourlyTempChartContainer,
                forecastSection,
                tempChartContainer,
                precipChartContainer
        );
        rootContent.setAlignment(Pos.TOP_CENTER);
        rootContent.setPadding(new Insets(24, 28, 36, 28));
        rootContent.setMaxWidth(860);
        rootContent.setStyle("-fx-background-color: transparent;");

        // Centering wrapper
        centerWrapper = new HBox(rootContent);
        centerWrapper.setAlignment(Pos.TOP_CENTER);
        centerWrapper.setStyle("-fx-background-color: transparent;");

        // 8. Vertical ScrollPane: Guarantees full accessibility on any laptop/screen resolution
        mainScrollPane = new ScrollPane(centerWrapper);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Apply initial clean sky-blue gradient background
        applyWeatherBackground(null);

        return mainScrollPane;
    }

    /**
     * Builds a modernized Current Weather Card with clear visual hierarchy and metric tiles.
     */
    private VBox createCurrentWeatherCard() {
        // City Title & Observation Timestamp
        cityNameHeaderLabel = new Label("CURRENT WEATHER: —");
        cityNameHeaderLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2563eb; -fx-letter-spacing: 0.5px;");

        observationTimeLabel = new Label("Awaiting city search...");
        observationTimeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        VBox titleArea = new VBox(2, cityNameHeaderLabel, observationTimeLabel);

        // Primary Temperature & Feels-like
        temperatureValueLabel = new Label("— °C");
        temperatureValueLabel.setStyle("-fx-font-size: 44px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        feelsLikeLabel = new Label("Feels like: — °C");
        feelsLikeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        VBox tempBlock = new VBox(1, temperatureValueLabel, feelsLikeLabel);
        tempBlock.setAlignment(Pos.CENTER_LEFT);

        // Condition & Emoji
        conditionEmojiLabel = new Label("🌤️");
        conditionEmojiLabel.setStyle("-fx-font-size: 46px;");

        conditionTextLabel = new Label("Condition: —");
        conditionTextLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        descriptionLabel = new Label("Description: —");
        descriptionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        VBox conditionBlock = new VBox(2, conditionTextLabel, descriptionLabel);
        conditionBlock.setAlignment(Pos.CENTER_LEFT);

        HBox weatherHero = new HBox(22, tempBlock, conditionEmojiLabel, conditionBlock);
        weatherHero.setAlignment(Pos.CENTER_LEFT);
        weatherHero.setPadding(new Insets(4, 0, 8, 0));

        // Metric Tiles Row: Humidity, Wind Speed, Pressure
        humidityTileLabel = new Label("💧 Humidity\n— %");
        windSpeedTileLabel = new Label("💨 Wind Speed\n— km/h");
        pressureTileLabel = new Label("⏲ Pressure\n— hPa");

        styleMetricTile(humidityTileLabel);
        styleMetricTile(windSpeedTileLabel);
        styleMetricTile(pressureTileLabel);

        HBox metricsRow = new HBox(12, humidityTileLabel, windSpeedTileLabel, pressureTileLabel);
        metricsRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(humidityTileLabel, Priority.ALWAYS);
        HBox.setHgrow(windSpeedTileLabel, Priority.ALWAYS);
        HBox.setHgrow(pressureTileLabel, Priority.ALWAYS);

        VBox card = new VBox(14, titleArea, weatherHero, metricsRow);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.setMaxWidth(820);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12px; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 12px; -fx-border-width: 1px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );

        return card;
    }

    /**
     * Styles individual metric tiles inside the current weather card.
     */
    private void styleMetricTile(Label tile) {
        tile.setMaxWidth(Double.MAX_VALUE);
        tile.setAlignment(Pos.CENTER);
        tile.setStyle(
                "-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #334155; " +
                "-fx-background-color: #f8fafc; -fx-padding: 10px 14px; -fx-background-radius: 8px; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-border-width: 1px; " +
                "-fx-text-alignment: center;"
        );
    }

    /**
     * Builds the 24-Hour Forecast Section with horizontal scrolling.
     */
    private VBox createHourlyForecastSection() {
        Label headingLabel = new Label("24-Hour Forecast");
        headingLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label subtitleLabel = new Label("Temperature outlook for the next 24 hours");
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        VBox titleBox = new VBox(2, headingLabel, subtitleLabel);

        hourlyForecastCardsContainer = new HBox(10);
        hourlyForecastCardsContainer.setAlignment(Pos.CENTER_LEFT);
        hourlyForecastCardsContainer.setPadding(new Insets(6, 4, 10, 4));

        Label placeholder = new Label("Search for a city to view its 24-hour forecast.");
        placeholder.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-style: italic;");
        hourlyForecastCardsContainer.getChildren().add(placeholder);

        ScrollPane hourlyScrollPane = new ScrollPane(hourlyForecastCardsContainer);
        hourlyScrollPane.setFitToHeight(true);
        hourlyScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        hourlyScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        hourlyScrollPane.setMaxWidth(820);
        hourlyScrollPane.setPrefHeight(230);
        hourlyScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

        VBox section = new VBox(8, titleBox, hourlyScrollPane);
        section.setAlignment(Pos.CENTER_LEFT);
        section.setMaxWidth(820);

        return section;
    }

    /**
     * Creates and configures the 24-hour temperature trend line chart.
     */
    private VBox createHourlyTemperatureChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Hour");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Temperature (°C)");

        hourlyTemperatureChart = new LineChart<>(xAxis, yAxis);
        hourlyTemperatureChart.setTitle("24-Hour Temperature Trend");
        hourlyTemperatureChart.setAnimated(false);
        hourlyTemperatureChart.setPrefHeight(280);
        hourlyTemperatureChart.setMinHeight(230);

        VBox card = new VBox(hourlyTemperatureChart);
        card.setMaxWidth(820);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12px; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 12px; -fx-border-width: 1px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );

        return card;
    }

    /**
     * Builds the 7-Day Forecast Section with horizontal scrolling.
     */
    private VBox createForecastSection() {
        Label forecastHeaderLabel = new Label("7-Day Weather Forecast");
        forecastHeaderLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        forecastCardsContainer = new HBox(12);
        forecastCardsContainer.setAlignment(Pos.CENTER_LEFT);
        forecastCardsContainer.setPadding(new Insets(8, 4, 8, 4));

        Label forecastPlaceholder = new Label("Search for a city to view its 7-day forecast.");
        forecastPlaceholder.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-style: italic;");
        forecastCardsContainer.getChildren().add(forecastPlaceholder);

        ScrollPane forecastScrollPane = new ScrollPane(forecastCardsContainer);
        forecastScrollPane.setFitToHeight(true);
        forecastScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        forecastScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        forecastScrollPane.setMaxWidth(820);
        forecastScrollPane.setPrefHeight(235);
        forecastScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

        VBox section = new VBox(8, forecastHeaderLabel, forecastScrollPane);
        section.setAlignment(Pos.CENTER_LEFT);
        section.setMaxWidth(820);

        return section;
    }

    /**
     * Creates and configures the 7-day temperature trend line chart.
     */
    private VBox createTemperatureChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Temperature (°C)");

        temperatureChart = new LineChart<>(xAxis, yAxis);
        temperatureChart.setTitle("7-Day Temperature Trend");
        temperatureChart.setAnimated(false);
        temperatureChart.setPrefHeight(290);
        temperatureChart.setMinHeight(240);

        VBox card = new VBox(temperatureChart);
        card.setMaxWidth(820);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12px; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 12px; -fx-border-width: 1px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );

        return card;
    }

    /**
     * Creates and configures the 7-day precipitation probability bar chart.
     */
    private VBox createPrecipitationChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");

        NumberAxis yAxis = new NumberAxis(0, 100, 20);
        yAxis.setLabel("Probability (%)");
        yAxis.setAutoRanging(false); // Fixes the Y-axis strictly between 0% and 100%

        precipitationChart = new BarChart<>(xAxis, yAxis);
        precipitationChart.setTitle("7-Day Precipitation Probability");
        precipitationChart.setAnimated(false);
        precipitationChart.setPrefHeight(270);
        precipitationChart.setMinHeight(220);

        VBox card = new VBox(precipitationChart);
        card.setMaxWidth(820);
        card.setPadding(new Insets(10));
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12px; " +
                "-fx-border-color: #e2e8f0; -fx-border-radius: 12px; -fx-border-width: 1px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );

        return card;
    }

    /**
     * Executes when the user initiates a search.
     * Uses JavaFX Task to ensure the UI thread never freezes.
     */
    private void performSearch() {
        // Prevent concurrent searches while a request is in-flight
        if (isSearching) {
            return;
        }

        String inputCity = cityInputField.getText();

        // 1. Validate Input (Reject empty or whitespace-only input)
        if (inputCity == null || inputCity.trim().isEmpty()) {
            statusMessageLabel.setText("❌ Please enter a city name.");
            statusMessageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
            return;
        }

        final String cityName = inputCity.trim();

        // 2. Set Loading State & Lock Controls
        isSearching = true;
        searchButton.setDisable(true);
        cityInputField.setDisable(true);
        statusMessageLabel.setText("⏳ Loading live weather data for \"" + cityName + "\"...");
        statusMessageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2563eb; -fx-font-weight: bold;");

        // Stale Data Protection: clear old data immediately so it is not confused with new city
        resetCurrentWeatherLabels();
        resetHourlySection("Fetching 24-hour forecast for \"" + cityName + "\"...");
        resetForecastSection("Fetching forecast for \"" + cityName + "\"...");
        clearCharts();
        clearHourlyTemperatureChart();

        // 3. Create Asynchronous Task
        Task<WeatherResponse> weatherSearchTask = new Task<>() {
            @Override
            protected WeatherResponse call() throws Exception {
                return controller.searchWeather(cityName);
            }
        };

        // 4. On Task Success
        weatherSearchTask.setOnSucceeded(event -> {
            isSearching = false;
            searchButton.setDisable(false);
            cityInputField.setDisable(false);

            WeatherResponse response = weatherSearchTask.getValue();

            if (response == null || response.getCurrentWeather() == null) {
                statusMessageLabel.setText("❌ Could not retrieve weather for \"" + cityName + "\". Please verify the city name or check your internet connection.");
                statusMessageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
                resetCurrentWeatherLabels();
                resetHourlySection("No hourly forecast available.");
                resetForecastSection("No forecast data available.");
                clearCharts();
                clearHourlyTemperatureChart();
                applyWeatherBackground(null);
            } else {
                // Populate Current Weather
                WeatherData current = response.getCurrentWeather();
                cityNameHeaderLabel.setText("CURRENT WEATHER IN " + current.getCityName().toUpperCase(Locale.ENGLISH));
                observationTimeLabel.setText("Observed: " + (current.getTimestamp() != null ? current.getTimestamp() : "Just now"));

                temperatureValueLabel.setText(String.format(Locale.US, "%.1f °C", current.getTemperature()));
                feelsLikeLabel.setText(String.format(Locale.US, "Feels like %.1f °C", current.getFeelsLike()));

                conditionEmojiLabel.setText(getWeatherEmoji(current.getCondition()));
                conditionTextLabel.setText(current.getCondition() != null ? current.getCondition() : "N/A");
                descriptionLabel.setText(current.getDescription() != null ? current.getDescription() : "");

                humidityTileLabel.setText(String.format(Locale.US, "💧 Humidity\n%d %%", current.getHumidity()));
                windSpeedTileLabel.setText(String.format(Locale.US, "💨 Wind Speed\n%.1f km/h", current.getWindSpeed()));
                pressureTileLabel.setText(String.format(Locale.US, "⏲ Pressure\n%d hPa", current.getPressure()));

                statusMessageLabel.setText("✓ Live weather data successfully retrieved from Open-Meteo API!");
                statusMessageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #059669; -fx-font-weight: bold;");

                // Dynamic Weather-themed Background
                applyWeatherBackground(current.getCondition());

                // Select Next 24 Hours starting at current local observation hour
                List<HourlyForecast> next24Hours = selectNext24Hours(response.getHourlyForecasts(), current.getTimestamp());

                // Populate 24-Hour Forecast Cards & Chart
                updateHourlyForecastCards(next24Hours);
                updateHourlyTemperatureChart(next24Hours);

                // Populate 7-Day Forecast Cards & Charts
                updateForecastCards(response.getForecasts());
                updateTemperatureChart(response.getForecasts());
                updatePrecipitationChart(response.getForecasts());
            }
        });

        // 5. On Task Failure
        weatherSearchTask.setOnFailed(event -> {
            isSearching = false;
            searchButton.setDisable(false);
            cityInputField.setDisable(false);

            statusMessageLabel.setText("❌ Could not retrieve weather for \"" + cityName + "\". Please verify the city name or check your internet connection.");
            statusMessageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");

            resetCurrentWeatherLabels();
            resetHourlySection("Hourly forecast could not be loaded due to an error.");
            resetForecastSection("Forecast could not be loaded due to an error.");
            clearCharts();
            clearHourlyTemperatureChart();
            applyWeatherBackground(null);
        });

        // 6. On Task Cancelled
        weatherSearchTask.setOnCancelled(event -> {
            isSearching = false;
            searchButton.setDisable(false);
            cityInputField.setDisable(false);

            statusMessageLabel.setText("⚠️ Weather search cancelled.");
            statusMessageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #d97706;");

            resetCurrentWeatherLabels();
            resetHourlySection("Search cancelled.");
            resetForecastSection("Search cancelled.");
            clearCharts();
            clearHourlyTemperatureChart();
            applyWeatherBackground(null);
        });

        // 7. Start Daemon Background Worker Thread
        Thread backgroundThread = new Thread(weatherSearchTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    /**
     * Slices the next 24 consecutive hourly forecasts starting at the current local observation hour.
     */
    private List<HourlyForecast> selectNext24Hours(List<HourlyForecast> allHourly, LocalDateTime currentObsTime) {
        if (allHourly == null || allHourly.isEmpty()) {
            return new ArrayList<>();
        }

        int startIndex = 0;
        if (currentObsTime != null) {
            for (int i = 0; i < allHourly.size(); i++) {
                HourlyForecast hf = allHourly.get(i);
                if (hf.getTime() != null) {
                    if (hf.getTime().toLocalDate().equals(currentObsTime.toLocalDate()) &&
                        hf.getTime().getHour() == currentObsTime.getHour()) {
                        startIndex = i;
                        break;
                    } else if (!hf.getTime().isBefore(currentObsTime)) {
                        startIndex = i;
                        break;
                    }
                }
            }
        }

        int endIndex = Math.min(startIndex + 24, allHourly.size());
        List<HourlyForecast> next24 = new ArrayList<>();
        for (int i = startIndex; i < endIndex; i++) {
            next24.add(allHourly.get(i));
        }
        return next24;
    }

    /**
     * Dynamically populates the 24-hour forecast cards container.
     */
    private void updateHourlyForecastCards(List<HourlyForecast> next24Hours) {
        hourlyForecastCardsContainer.getChildren().clear();

        if (next24Hours == null || next24Hours.isEmpty()) {
            resetHourlySection("No hourly forecast data available for this location.");
            return;
        }

        for (int i = 0; i < next24Hours.size(); i++) {
            HourlyForecast forecast = next24Hours.get(i);
            if (forecast != null) {
                boolean isNow = (i == 0);
                VBox card = createHourlyForecastCard(forecast, isNow);
                hourlyForecastCardsContainer.getChildren().add(card);
            }
        }
    }

    /**
     * Builds an individual JavaFX card for a single hour's forecast.
     */
    public VBox createHourlyForecastCard(HourlyForecast forecast) {
        return createHourlyForecastCard(forecast, false);
    }

    /**
     * Builds an individual JavaFX card for a single hour's forecast with optional "NOW" badge.
     */
    public VBox createHourlyForecastCard(HourlyForecast forecast, boolean isNow) {
        // "NOW" Badge or uniform spacing placeholder
        Label badgeLabel = new Label();
        if (isNow) {
            badgeLabel.setText("NOW");
            badgeLabel.setStyle(
                    "-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #1d4ed8; " +
                    "-fx-background-color: #dbeafe; -fx-padding: 2px 8px; -fx-background-radius: 999px;"
            );
        } else {
            badgeLabel.setText(" ");
            badgeLabel.setStyle("-fx-font-size: 10px; -fx-padding: 2px 8px;");
        }

        // Time Label
        String timeStr = (forecast.getTime() != null)
                ? forecast.getTime().format(HOURLY_TIME_FORMATTER)
                : "—";
        Label timeLabel = new Label(timeStr);
        timeLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // Weather Icon / Emoji
        String condition = WeatherApiService.mapWeatherCondition(forecast.getWeatherCode());
        String description = WeatherApiService.mapWeatherDescription(forecast.getWeatherCode());
        Label emojiLabel = new Label(getWeatherEmoji(condition));
        emojiLabel.setStyle("-fx-font-size: 26px;");

        // Temperature Label
        Label tempLabel = new Label(String.format(Locale.US, "%.1f °C", forecast.getTemperature()));
        tempLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        // Feels Like Label
        Label feelsLabel = new Label(String.format(Locale.US, "Feels %.1f °C", forecast.getApparentTemperature()));
        feelsLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");

        // Condition Description
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #475569; -fx-text-alignment: center;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(105);

        VBox card = new VBox(5,
                badgeLabel,
                timeLabel,
                emojiLabel,
                tempLabel,
                feelsLabel,
                descLabel
        );
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(10, 8, 12, 8));
        card.setPrefWidth(112);
        card.setMinWidth(108);
        card.setMaxWidth(118);
        card.setPrefHeight(185);
        card.setMinHeight(180);

        if (isNow) {
            card.setStyle(
                    "-fx-background-color: #eff6ff; -fx-background-radius: 10px; " +
                    "-fx-border-color: #3b82f6; -fx-border-radius: 10px; -fx-border-width: 1.5px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.18), 8, 0, 0, 2);"
            );
            card.setOnMouseEntered(e -> card.setStyle(
                    "-fx-background-color: #dbeafe; -fx-background-radius: 10px; " +
                    "-fx-border-color: #2563eb; -fx-border-radius: 10px; -fx-border-width: 1.5px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.25), 10, 0, 0, 4);"
            ));
            card.setOnMouseExited(e -> card.setStyle(
                    "-fx-background-color: #eff6ff; -fx-background-radius: 10px; " +
                    "-fx-border-color: #3b82f6; -fx-border-radius: 10px; -fx-border-width: 1.5px; " +
                    "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.18), 8, 0, 0, 2);"
            ));
        } else {
            card.setStyle(CARD_DEFAULT_STYLE);
            card.setOnMouseEntered(e -> card.setStyle(CARD_HOVER_STYLE));
            card.setOnMouseExited(e -> card.setStyle(CARD_DEFAULT_STYLE));
        }

        return card;
    }

    /**
     * Populates the 24-Hour Temperature LineChart with actual hourly values.
     */
    public void updateHourlyTemperatureChart(List<HourlyForecast> forecasts) {
        clearHourlyTemperatureChart();

        if (forecasts == null || forecasts.isEmpty()) {
            return;
        }

        XYChart.Series<String, Number> tempSeries = new XYChart.Series<>();
        tempSeries.setName("Temperature");

        for (HourlyForecast forecast : forecasts) {
            if (forecast != null && forecast.getTime() != null) {
                String timeLabel = forecast.getTime().format(HOURLY_TIME_FORMATTER);
                tempSeries.getData().add(new XYChart.Data<>(timeLabel, forecast.getTemperature()));
            }
        }

        hourlyTemperatureChart.getData().add(tempSeries);
    }

    /**
     * Clears all series data from the 24-Hour Temperature LineChart.
     */
    public void clearHourlyTemperatureChart() {
        if (hourlyTemperatureChart != null) {
            hourlyTemperatureChart.getData().clear();
        }
    }

    /**
     * Dynamically generates forecast cards from the provided list.
     */
    private void updateForecastCards(List<Forecast> forecasts) {
        forecastCardsContainer.getChildren().clear();

        if (forecasts == null || forecasts.isEmpty()) {
            resetForecastSection("No forecast data available for this location.");
            return;
        }

        for (Forecast forecast : forecasts) {
            if (forecast != null) {
                VBox card = createForecastCard(forecast);
                forecastCardsContainer.getChildren().add(card);
            }
        }
    }

    /**
     * Builds an individual JavaFX card for a single day's forecast with micro-interactions.
     */
    private VBox createForecastCard(Forecast forecast) {
        boolean isToday = forecast.getDate() != null && forecast.getDate().equals(LocalDate.now());

        // Date Label / Badge
        Label dateLabel = new Label();
        if (isToday) {
            dateLabel.setText("TODAY");
            dateLabel.setStyle(
                    "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #1d4ed8; " +
                    "-fx-background-color: #dbeafe; -fx-padding: 2px 8px; -fx-background-radius: 999px;"
            );
        } else if (forecast.getDate() != null) {
            dateLabel.setText(forecast.getDate().format(CARD_DATE_FORMATTER));
            dateLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        } else {
            dateLabel.setText("N/A");
            dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        }

        // Weather Icon / Emoji
        String emoji = getWeatherEmoji(forecast.getCondition());
        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 28px;");

        // Condition & Description
        Label conditionText = new Label(forecast.getCondition() != null ? forecast.getCondition() : "N/A");
        conditionText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #334155;");

        Label descText = new Label(forecast.getDescription() != null ? forecast.getDescription() : "");
        descText.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b; -fx-text-alignment: center;");
        descText.setWrapText(true);
        descText.setMaxWidth(115);

        // High / Low Temperatures
        Label maxTempLabel = new Label(String.format(Locale.US, "High: %.1f °C", forecast.getMaxTemp()));
        maxTempLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #ea580c;");

        Label minTempLabel = new Label(String.format(Locale.US, "Low: %.1f °C", forecast.getMinTemp()));
        minTempLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #2563eb;");

        // Precipitation & Wind Speed
        Label rainLabel = new Label(String.format(Locale.US, "☔ %d%% rain", forecast.getPrecipitationProbability()));
        rainLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #0284c7;");

        Label windLabel = new Label(String.format(Locale.US, "💨 %.1f km/h", forecast.getWindSpeed()));
        windLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");

        VBox card = new VBox(6,
                dateLabel,
                emojiLabel,
                conditionText,
                descText,
                maxTempLabel,
                minTempLabel,
                rainLabel,
                windLabel
        );
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(12, 10, 12, 10));
        card.setPrefWidth(130);
        card.setMinWidth(125);
        card.setStyle(CARD_DEFAULT_STYLE);

        // Subtle interactive hover state
        card.setOnMouseEntered(e -> card.setStyle(CARD_HOVER_STYLE));
        card.setOnMouseExited(e -> card.setStyle(CARD_DEFAULT_STYLE));

        return card;
    }

    /**
     * Populates the Temperature LineChart dynamically with Max and Min temperature series.
     */
    public void updateTemperatureChart(List<Forecast> forecasts) {
        temperatureChart.getData().clear();

        if (forecasts == null || forecasts.isEmpty()) {
            return;
        }

        XYChart.Series<String, Number> maxSeries = new XYChart.Series<>();
        maxSeries.setName("Maximum Temperature");

        XYChart.Series<String, Number> minSeries = new XYChart.Series<>();
        minSeries.setName("Minimum Temperature");

        for (Forecast forecast : forecasts) {
            if (forecast != null && forecast.getDate() != null) {
                String dateLabel = formatChartDate(forecast.getDate());
                maxSeries.getData().add(new XYChart.Data<>(dateLabel, forecast.getMaxTemp()));
                minSeries.getData().add(new XYChart.Data<>(dateLabel, forecast.getMinTemp()));
            }
        }

        // Avoid varargs addAll to eliminate compiler unchecked warnings
        temperatureChart.getData().add(maxSeries);
        temperatureChart.getData().add(minSeries);
    }

    /**
     * Populates the Precipitation BarChart dynamically with rain probability data.
     */
    public void updatePrecipitationChart(List<Forecast> forecasts) {
        precipitationChart.getData().clear();

        if (forecasts == null || forecasts.isEmpty()) {
            return;
        }

        XYChart.Series<String, Number> rainSeries = new XYChart.Series<>();
        rainSeries.setName("Rain Probability");

        for (Forecast forecast : forecasts) {
            if (forecast != null && forecast.getDate() != null) {
                String dateLabel = formatChartDate(forecast.getDate());
                rainSeries.getData().add(new XYChart.Data<>(dateLabel, forecast.getPrecipitationProbability()));
            }
        }

        precipitationChart.getData().add(rainSeries);
    }

    /**
     * Formats a forecast date for chart category labels.
     */
    private String formatChartDate(LocalDate date) {
        if (date == null) {
            return "N/A";
        }
        return date.format(CHART_DATE_FORMATTER);
    }

    /**
     * Maps weather condition categories to recognizable emoji icons.
     */
    private String getWeatherEmoji(String condition) {
        if (condition == null) {
            return "🌤️";
        }

        switch (condition.trim()) {
            case "Clear":
                return "☀️";
            case "Clouds":
                return "☁️";
            case "Fog":
                return "🌫️";
            case "Drizzle":
                return "🌦️";
            case "Rain":
                return "🌧️";
            case "Rain Showers":
                return "🌦️";
            case "Snow":
                return "❄️";
            case "Thunderstorm":
                return "⛈️";
            default:
                return "🌤️";
        }
    }

    /**
     * Resets current weather labels back to default placeholder values.
     */
    private void resetCurrentWeatherLabels() {
        cityNameHeaderLabel.setText("CURRENT WEATHER: —");
        observationTimeLabel.setText("No active weather data");
        temperatureValueLabel.setText("— °C");
        feelsLikeLabel.setText("Feels like: — °C");
        conditionEmojiLabel.setText("🌤️");
        conditionTextLabel.setText("Condition: —");
        descriptionLabel.setText("Description: —");
        humidityTileLabel.setText("💧 Humidity\n— %");
        windSpeedTileLabel.setText("💨 Wind Speed\n— km/h");
        pressureTileLabel.setText("⏲ Pressure\n— hPa");
    }

    /**
     * Resets the hourly cards container with a placeholder message.
     */
    private void resetHourlySection(String message) {
        if (hourlyForecastCardsContainer != null) {
            hourlyForecastCardsContainer.getChildren().clear();
            Label msgLabel = new Label(message);
            msgLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-style: italic;");
            hourlyForecastCardsContainer.getChildren().add(msgLabel);
        }
    }

    /**
     * Resets the forecast cards container and displays an informational message.
     */
    private void resetForecastSection(String message) {
        if (forecastCardsContainer != null) {
            forecastCardsContainer.getChildren().clear();
            Label msgLabel = new Label(message);
            msgLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-style: italic;");
            forecastCardsContainer.getChildren().add(msgLabel);
        }
    }

    /**
     * Clears all 7-day chart series data when no data is available or a search fails.
     */
    private void clearCharts() {
        if (temperatureChart != null) {
            temperatureChart.getData().clear();
        }
        if (precipitationChart != null) {
            precipitationChart.getData().clear();
        }
    }

    /**
     * Dynamically applies a subtle, professional weather-themed background gradient
     * based on current atmospheric conditions, while preserving pristine text readability.
     */
    private void applyWeatherBackground(String condition) {
        if (mainScrollPane == null) {
            return;
        }

        String bgGradient;
        String baseColor;

        if (condition == null) {
            // Default elegant sky-blue gradient
            baseColor = "#f0f7ff";
            bgGradient = "linear-gradient(to bottom, #f0f7ff 0%, #e0f2fe 100%)";
        } else {
            switch (condition.trim()) {
                case "Clear":
                    baseColor = "#eff6ff";
                    bgGradient = "linear-gradient(to bottom, #eff6ff 0%, #dbeafe 100%)";
                    break;
                case "Clouds":
                    baseColor = "#f1f5f9";
                    bgGradient = "linear-gradient(to bottom, #f8fafc 0%, #e2e8f0 100%)";
                    break;
                case "Drizzle":
                case "Rain":
                case "Rain Showers":
                    baseColor = "#e0f2fe";
                    bgGradient = "linear-gradient(to bottom, #f0f9ff 0%, #cbd5e1 100%)";
                    break;
                case "Thunderstorm":
                    baseColor = "#e0e7ff";
                    bgGradient = "linear-gradient(to bottom, #eef2ff 0%, #c7d2fe 100%)";
                    break;
                case "Snow":
                    baseColor = "#f8fafc";
                    bgGradient = "linear-gradient(to bottom, #ffffff 0%, #e0f2fe 100%)";
                    break;
                case "Fog":
                    baseColor = "#f3f4f6";
                    bgGradient = "linear-gradient(to bottom, #f9fafb 0%, #e5e7eb 100%)";
                    break;
                default:
                    baseColor = "#f0f7ff";
                    bgGradient = "linear-gradient(to bottom, #f0f7ff 0%, #e0effe 100%)";
                    break;
            }
        }

        mainScrollPane.setStyle("-fx-background: " + baseColor + "; -fx-background-color: " + bgGradient + "; -fx-border-color: transparent;");
    }
}
