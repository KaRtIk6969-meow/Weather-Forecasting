package com.weather.view;

import com.weather.controller.WeatherController;
import com.weather.model.Forecast;
import com.weather.model.WeatherData;
import com.weather.model.WeatherResponse;
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
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * MainView is responsible for building and managing the complete user interface.
 *
 * Professional UI Architecture & Polish:
 * 1. Responsive Scrollable Viewport (vertical ScrollPane prevents any viewport clipping).
 * 2. Elegant Search Header with clear hover states and keyboard trigger (ENTER).
 * 3. Modern Current Weather Card with clear visual hierarchy, metric tiles, and typography.
 * 4. Horizontal Scrollable 7-Day Forecast Cards with hover elevation and "Today" badges.
 * 5. Interactive 7-Day Temperature LineChart (Max & Min curves).
 * 6. Interactive 7-Day Precipitation BarChart (0-100% rain probability).
 * 7. Asynchronous background loading with disabled button states and comprehensive error handling.
 */
public class MainView {

    // Controller reference to coordinate backend services
    private final WeatherController controller;

    // UI Input controls
    private TextField cityInputField;
    private Button searchButton;

    // UI Status / Error notification label
    private Label statusMessageLabel;

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

    // UI Container for dynamically generated 7-Day Forecast cards
    private HBox forecastCardsContainer;

    // Visual Charts
    private LineChart<String, Number> temperatureChart;
    private BarChart<String, Number> precipitationChart;

    // Date Formatters
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

        Label appSubtitleLabel = new Label("MVC Architecture • Live Weather API • Dynamic Visual Analytics");
        appSubtitleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

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

        // 5. 7-Day Forecast Section
        VBox forecastSection = createForecastSection();

        // 6. Visual Charts Section
        VBox tempChartContainer = createTemperatureChart();
        VBox precipChartContainer = createPrecipitationChart();

        // 7. Main Vertical Content Box
        VBox rootContent = new VBox(20,
                headerBox,
                searchBar,
                statusMessageLabel,
                currentWeatherCard,
                forecastSection,
                tempChartContainer,
                precipChartContainer
        );
        rootContent.setAlignment(Pos.TOP_CENTER);
        rootContent.setPadding(new Insets(24, 28, 32, 28));
        rootContent.setMaxWidth(860);
        rootContent.setStyle("-fx-background-color: #f8fafc;");

        // Centering wrapper
        HBox centerWrapper = new HBox(rootContent);
        centerWrapper.setAlignment(Pos.TOP_CENTER);
        centerWrapper.setStyle("-fx-background-color: #f8fafc;");

        // 8. Vertical ScrollPane: Guarantees full accessibility on any laptop/screen resolution
        ScrollPane mainScrollPane = new ScrollPane(centerWrapper);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        mainScrollPane.setStyle("-fx-background: #f8fafc; -fx-background-color: #f8fafc; -fx-border-color: transparent;");

        return mainScrollPane;
    }

    /**
     * Builds a modernized Current Weather Card with clear visual hierarchy and metric tiles.
     */
    private VBox createCurrentWeatherCard() {
        // City Title & Observation Timestamp
        cityNameHeaderLabel = new Label("Current Weather: —");
        cityNameHeaderLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        observationTimeLabel = new Label("Awaiting city search...");
        observationTimeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        VBox titleArea = new VBox(2, cityNameHeaderLabel, observationTimeLabel);

        // Primary Temperature & Feels-like
        temperatureValueLabel = new Label("— °C");
        temperatureValueLabel.setStyle("-fx-font-size: 38px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        feelsLikeLabel = new Label("Feels like: — °C");
        feelsLikeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        VBox tempBlock = new VBox(2, temperatureValueLabel, feelsLikeLabel);

        // Condition & Emoji
        conditionEmojiLabel = new Label("🌤️");
        conditionEmojiLabel.setStyle("-fx-font-size: 42px;");

        conditionTextLabel = new Label("Condition: —");
        conditionTextLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #334155;");

        descriptionLabel = new Label("Description: —");
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        VBox conditionBlock = new VBox(2, conditionTextLabel, descriptionLabel);

        HBox weatherHero = new HBox(16, tempBlock, conditionEmojiLabel, conditionBlock);
        weatherHero.setAlignment(Pos.CENTER_LEFT);

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
        temperatureChart.setAnimated(false); // Disabling animation avoids internal category glitches upon updates
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
        String inputCity = cityInputField.getText();

        // 1. Validate Input
        if (inputCity == null || inputCity.trim().isEmpty()) {
            statusMessageLabel.setText("⚠️ Please enter a city name before searching.");
            statusMessageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
            return;
        }

        final String cityName = inputCity.trim();

        // 2. Set Loading State & Disable Controls
        searchButton.setDisable(true);
        cityInputField.setDisable(true);
        statusMessageLabel.setText("⏳ Loading live weather data for \"" + cityName + "\"...");
        statusMessageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2563eb; -fx-font-weight: bold;");

        // 3. Create Asynchronous Task
        Task<WeatherResponse> weatherSearchTask = new Task<>() {
            @Override
            protected WeatherResponse call() throws Exception {
                return controller.searchWeather(cityName);
            }
        };

        // 4. On Task Success
        weatherSearchTask.setOnSucceeded(event -> {
            searchButton.setDisable(false);
            cityInputField.setDisable(false);

            WeatherResponse response = weatherSearchTask.getValue();

            if (response == null || response.getCurrentWeather() == null) {
                statusMessageLabel.setText("❌ Could not retrieve weather for \"" + cityName + "\". Please verify the city name or check your internet connection.");
                statusMessageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
                resetCurrentWeatherLabels();
                resetForecastSection("No forecast data available.");
                clearCharts();
            } else {
                // Populate Current Weather
                WeatherData current = response.getCurrentWeather();
                cityNameHeaderLabel.setText("📍 Current Weather in " + current.getCityName());
                observationTimeLabel.setText("Observed: " + current.getTimestamp());

                temperatureValueLabel.setText(String.format("%.1f °C", current.getTemperature()));
                feelsLikeLabel.setText(String.format("Feels like %.1f °C", current.getFeelsLike()));

                conditionEmojiLabel.setText(getWeatherEmoji(current.getCondition()));
                conditionTextLabel.setText(current.getCondition());
                descriptionLabel.setText(current.getDescription());

                humidityTileLabel.setText(String.format("💧 Humidity\n%d %%", current.getHumidity()));
                windSpeedTileLabel.setText(String.format("💨 Wind Speed\n%.1f km/h", current.getWindSpeed()));
                pressureTileLabel.setText(String.format("⏲ Pressure\n%d hPa", current.getPressure()));

                statusMessageLabel.setText("✓ Live weather data successfully retrieved from Open-Meteo API!");
                statusMessageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #059669; -fx-font-weight: bold;");

                // Populate 7-Day Forecast Cards
                updateForecastCards(response.getForecasts());

                // Populate Visual Charts
                updateTemperatureChart(response.getForecasts());
                updatePrecipitationChart(response.getForecasts());
            }
        });

        // 5. On Task Failure
        weatherSearchTask.setOnFailed(event -> {
            searchButton.setDisable(false);
            cityInputField.setDisable(false);

            Throwable exception = weatherSearchTask.getException();
            String errorMsg = (exception != null && exception.getMessage() != null)
                    ? exception.getMessage() : "Network connection unavailable";

            statusMessageLabel.setText("❌ Network request failed: " + errorMsg);
            statusMessageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");

            resetCurrentWeatherLabels();
            resetForecastSection("Forecast could not be loaded due to an error.");
            clearCharts();
        });

        // 6. Start Daemon Background Worker Thread
        Thread backgroundThread = new Thread(weatherSearchTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
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
            VBox card = createForecastCard(forecast);
            forecastCardsContainer.getChildren().add(card);
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
        Label conditionText = new Label(forecast.getCondition());
        conditionText.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #334155;");

        Label descText = new Label(forecast.getDescription());
        descText.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b; -fx-text-alignment: center;");
        descText.setWrapText(true);
        descText.setMaxWidth(115);

        // High / Low Temperatures
        Label maxTempLabel = new Label(String.format("High: %.1f °C", forecast.getMaxTemp()));
        maxTempLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #ea580c;");

        Label minTempLabel = new Label(String.format("Low: %.1f °C", forecast.getMinTemp()));
        minTempLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #2563eb;");

        // Precipitation & Wind Speed
        Label rainLabel = new Label(String.format("☔ %d%% rain", forecast.getPrecipitationProbability()));
        rainLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #0284c7;");

        Label windLabel = new Label(String.format("💨 %.1f km/h", forecast.getWindSpeed()));
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
            String dateLabel = formatChartDate(forecast.getDate());
            maxSeries.getData().add(new XYChart.Data<>(dateLabel, forecast.getMaxTemp()));
            minSeries.getData().add(new XYChart.Data<>(dateLabel, forecast.getMinTemp()));
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
            String dateLabel = formatChartDate(forecast.getDate());
            rainSeries.getData().add(new XYChart.Data<>(dateLabel, forecast.getPrecipitationProbability()));
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
        cityNameHeaderLabel.setText("Current Weather: —");
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
     * Resets the forecast cards container and displays an informational message.
     */
    private void resetForecastSection(String message) {
        forecastCardsContainer.getChildren().clear();
        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-style: italic;");
        forecastCardsContainer.getChildren().add(msgLabel);
    }

    /**
     * Clears all chart series data when no data is available or a search fails.
     */
    private void clearCharts() {
        if (temperatureChart != null) {
            temperatureChart.getData().clear();
        }
        if (precipitationChart != null) {
            precipitationChart.getData().clear();
        }
    }
}
