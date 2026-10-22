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
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * MainView builds and manages the complete user interface.
 *
 * Layout Structure:
 * 1. Application title & description
 * 2. City search input and button
 * 3. Status and error notifications
 * 4. Current weather summary card
 * 5. 7-day forecast cards (horizontal scrolling)
 * 6. 7-day temperature trend line chart (LineChart)
 * 7. 7-day precipitation probability bar chart (BarChart)
 */
public class MainView {

    // Controller reference to delegate business logic
    private final WeatherController controller;

    // UI Input controls
    private TextField cityInputField;
    private Button searchButton;

    // UI Output labels for Current Weather display
    private Label cityNameHeaderLabel;
    private Label temperatureLabel;
    private Label conditionLabel;
    private Label humidityLabel;
    private Label windSpeedLabel;

    // UI Status / Error message label
    private Label statusMessageLabel;

    // UI Container for dynamically generated 7-day forecast cards
    private HBox forecastCardsContainer;

    // Charts for visual analytics
    private LineChart<String, Number> temperatureChart;
    private BarChart<String, Number> precipitationChart;

    // Date formatters for clean card and chart presentation
    private static final DateTimeFormatter CARD_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH);

    private static final DateTimeFormatter CHART_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH);

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
        // 1. Application Title & Subtitle
        Label titleLabel = new Label("Weather Forecasting & Visualization System");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label subtitleLabel = new Label("MVC Architecture • Live Weather API • Interactive Data Visualization");
        subtitleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        // 2. City Input Field & Search Button
        cityInputField = new TextField();
        cityInputField.setPromptText("Enter city name (e.g. Chandigarh, London, Tokyo)...");
        cityInputField.setPrefWidth(340);
        cityInputField.setStyle("-fx-font-size: 13px; -fx-padding: 8px 12px;");

        searchButton = new Button("Search Weather");
        searchButton.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-color: #2563eb; -fx-text-fill: white; -fx-padding: 8px 18px; -fx-cursor: hand;");

        // Trigger search on button click or pressing Enter inside the TextField
        searchButton.setOnAction(event -> performSearch());
        cityInputField.setOnAction(event -> performSearch());

        HBox searchBox = new HBox(12, cityInputField, searchButton);
        searchBox.setAlignment(Pos.CENTER);

        // 3. Status / Error Message Label
        statusMessageLabel = new Label("Enter a city name above and click 'Search Weather'.");
        statusMessageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");
        statusMessageLabel.setWrapText(true);

        // 4. Current Weather Card
        cityNameHeaderLabel = new Label("City: —");
        cityNameHeaderLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        temperatureLabel = new Label("Temperature: — °C");
        temperatureLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");

        conditionLabel = new Label("Condition: —");
        conditionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155;");

        humidityLabel = new Label("Humidity: — %");
        humidityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155;");

        windSpeedLabel = new Label("Wind Speed: — km/h");
        windSpeedLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155;");

        VBox currentWeatherCard = new VBox(8,
                cityNameHeaderLabel,
                temperatureLabel,
                conditionLabel,
                humidityLabel,
                windSpeedLabel
        );
        currentWeatherCard.setAlignment(Pos.CENTER_LEFT);
        currentWeatherCard.setPadding(new Insets(14, 20, 14, 20));
        currentWeatherCard.setMaxWidth(800);
        currentWeatherCard.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-border-width: 1px;");

        // 5. 7-Day Forecast Cards Section
        Label forecastHeaderLabel = new Label("7-Day Forecast");
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
        forecastScrollPane.setMaxWidth(800);
        forecastScrollPane.setPrefHeight(230);
        forecastScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");

        VBox forecastSection = new VBox(8, forecastHeaderLabel, forecastScrollPane);
        forecastSection.setAlignment(Pos.CENTER_LEFT);
        forecastSection.setMaxWidth(800);

        // 6. Visual Charts Section
        VBox tempChartContainer = createTemperatureChart();
        VBox precipChartContainer = createPrecipitationChart();

        // 7. Root Layout Assembly
        VBox rootContent = new VBox(18,
                titleLabel,
                subtitleLabel,
                searchBox,
                statusMessageLabel,
                currentWeatherCard,
                forecastSection,
                tempChartContainer,
                precipChartContainer
        );
        rootContent.setAlignment(Pos.TOP_CENTER);
        rootContent.setPadding(new Insets(24, 24, 30, 24));
        rootContent.setStyle("-fx-background-color: #f8fafc;");

        // Root vertical ScrollPane ensures all content is accessible regardless of monitor size
        ScrollPane mainScrollPane = new ScrollPane(rootContent);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setStyle("-fx-background: #f8fafc; -fx-background-color: #f8fafc; -fx-border-color: transparent;");

        return mainScrollPane;
    }

    /**
     * Creates and configures the 7-day temperature trend line chart.
     *
     * @return VBox container wrapping the LineChart
     */
    private VBox createTemperatureChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Temperature (°C)");

        temperatureChart = new LineChart<>(xAxis, yAxis);
        temperatureChart.setTitle("7-Day Temperature Trend");
        // Disable animation to prevent visual glitches during rapid series clearance/repopulation
        temperatureChart.setAnimated(false);
        temperatureChart.setPrefHeight(280);
        temperatureChart.setMinHeight(240);

        VBox card = new VBox(temperatureChart);
        card.setMaxWidth(800);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-border-width: 1px;");

        return card;
    }

    /**
     * Creates and configures the 7-day precipitation probability bar chart.
     *
     * @return VBox container wrapping the BarChart
     */
    private VBox createPrecipitationChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");

        // Y-axis spans strictly 0% to 100% with intervals of 20%
        NumberAxis yAxis = new NumberAxis(0, 100, 20);
        yAxis.setLabel("Probability (%)");
        yAxis.setAutoRanging(false);

        precipitationChart = new BarChart<>(xAxis, yAxis);
        precipitationChart.setTitle("7-Day Precipitation Probability");
        precipitationChart.setAnimated(false);
        precipitationChart.setPrefHeight(260);
        precipitationChart.setMinHeight(220);

        VBox card = new VBox(precipitationChart);
        card.setMaxWidth(800);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #cbd5e1; -fx-border-radius: 8px; -fx-border-width: 1px;");

        return card;
    }

    /**
     * Populates the Temperature LineChart dynamically with Max and Min temperature series.
     *
     * @param forecasts the list of 7 daily forecast objects
     */
    public void updateTemperatureChart(List<Forecast> forecasts) {
        temperatureChart.getData().clear();

        if (forecasts == null || forecasts.isEmpty()) {
            return;
        }

        // Series 1: Maximum Temperature
        XYChart.Series<String, Number> maxSeries = new XYChart.Series<>();
        maxSeries.setName("Maximum Temperature");

        // Series 2: Minimum Temperature
        XYChart.Series<String, Number> minSeries = new XYChart.Series<>();
        minSeries.setName("Minimum Temperature");

        // Populate both series using forecast data
        for (Forecast forecast : forecasts) {
            String dateLabel = formatChartDate(forecast.getDate());
            maxSeries.getData().add(new XYChart.Data<>(dateLabel, forecast.getMaxTemp()));
            minSeries.getData().add(new XYChart.Data<>(dateLabel, forecast.getMinTemp()));
        }

        temperatureChart.getData().add(maxSeries);
        temperatureChart.getData().add(minSeries);
    }

    /**
     * Populates the Precipitation BarChart dynamically with rain probability data.
     *
     * @param forecasts the list of 7 daily forecast objects
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
     * Helper to format a forecast date for chart category labels.
     */
    private String formatChartDate(LocalDate date) {
        if (date == null) {
            return "N/A";
        }
        return date.format(CHART_DATE_FORMATTER);
    }

    /**
     * Executes when the user initiates a search.
     * 
     * Uses a JavaFX Task to fetch live weather data asynchronously in the background.
     */
    private void performSearch() {
        String inputCity = cityInputField.getText();

        // 1. Validate Input (Runs on JavaFX Application Thread)
        if (inputCity == null || inputCity.trim().isEmpty()) {
            statusMessageLabel.setText("⚠️ Please enter a city name before searching.");
            statusMessageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
            return;
        }

        final String cityName = inputCity.trim();

        // 2. Provide immediate UI feedback & disable controls to prevent spam clicks
        searchButton.setDisable(true);
        cityInputField.setDisable(true);
        statusMessageLabel.setText("⏳ Fetching live weather, forecast, and charts for \"" + cityName + "\"...");
        statusMessageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2563eb; -fx-font-weight: bold;");

        // 3. Create a JavaFX Task to execute network queries on a background thread
        Task<WeatherResponse> weatherSearchTask = new Task<>() {
            @Override
            protected WeatherResponse call() throws Exception {
                return controller.searchWeather(cityName);
            }
        };

        // 4. On Task Success (automatically invoked on the JavaFX Application Thread)
        weatherSearchTask.setOnSucceeded(event -> {
            searchButton.setDisable(false);
            cityInputField.setDisable(false);

            WeatherResponse response = weatherSearchTask.getValue();

            if (response == null || response.getCurrentWeather() == null) {
                // City was not found or an API error occurred
                statusMessageLabel.setText("❌ Could not retrieve weather for \"" + cityName + "\". Please verify the city name or check your internet connection.");
                statusMessageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
                resetCurrentWeatherLabels();
                resetForecastSection("No forecast data available.");
                clearCharts();
            } else {
                // Populate Current Weather
                WeatherData current = response.getCurrentWeather();
                cityNameHeaderLabel.setText("📍 Current Weather in " + current.getCityName());
                temperatureLabel.setText(String.format("🌡 Temperature: %.1f °C  (Feels like: %.1f °C)",
                        current.getTemperature(), current.getFeelsLike()));
                conditionLabel.setText(String.format("☁ Condition: %s — %s",
                        current.getCondition(), current.getDescription()));
                humidityLabel.setText(String.format("💧 Humidity: %d %%",
                        current.getHumidity()));
                windSpeedLabel.setText(String.format("💨 Wind Speed: %.1f km/h  |  Pressure: %d hPa",
                        current.getWindSpeed(), current.getPressure()));

                statusMessageLabel.setText("✓ Live weather data successfully retrieved from Open-Meteo API!");
                statusMessageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #059669; -fx-font-weight: bold;");

                // Populate 7-Day Forecast Cards
                updateForecastCards(response.getForecasts());

                // Populate Visual Charts with Live Data
                updateTemperatureChart(response.getForecasts());
                updatePrecipitationChart(response.getForecasts());
            }
        });

        // 5. On Task Failure (automatically invoked on the JavaFX Application Thread)
        weatherSearchTask.setOnFailed(event -> {
            searchButton.setDisable(false);
            cityInputField.setDisable(false);

            Throwable exception = weatherSearchTask.getException();
            String errorMsg = (exception != null && exception.getMessage() != null)
                    ? exception.getMessage() : "Unknown network error";

            statusMessageLabel.setText("❌ Network request failed: " + errorMsg);
            statusMessageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");

            resetCurrentWeatherLabels();
            resetForecastSection("Forecast could not be loaded due to an error.");
            clearCharts();
        });

        // 6. Launch the Task on a daemon background thread
        Thread backgroundThread = new Thread(weatherSearchTask);
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    /**
     * Dynamically generates and displays forecast cards from the provided list.
     *
     * @param forecasts the list of daily Forecast objects from the API
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
     * Builds an individual JavaFX card for a single day's forecast.
     *
     * @param forecast the Forecast model containing daily metrics
     * @return a styled VBox representing the forecast card
     */
    private VBox createForecastCard(Forecast forecast) {
        String dateText;
        if (forecast.getDate() != null && forecast.getDate().equals(LocalDate.now())) {
            dateText = "Today";
        } else if (forecast.getDate() != null) {
            dateText = forecast.getDate().format(CARD_DATE_FORMATTER);
        } else {
            dateText = "N/A";
        }

        Label dateLabel = new Label(dateText);
        dateLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        String emoji = getWeatherEmoji(forecast.getCondition());
        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 26px;");

        Label conditionTextLabel = new Label(forecast.getCondition());
        conditionTextLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #334155;");

        Label descTextLabel = new Label(forecast.getDescription());
        descTextLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b; -fx-text-alignment: center;");
        descTextLabel.setWrapText(true);
        descTextLabel.setMaxWidth(110);

        Label maxTempLabel = new Label(String.format("High: %.1f °C", forecast.getMaxTemperature()));
        maxTempLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");

        Label minTempLabel = new Label(String.format("Low: %.1f °C", forecast.getMinTemperature()));
        minTempLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #2563eb;");

        Label rainLabel = new Label(String.format("☔ %d%% rain", forecast.getPrecipitationProbability()));
        rainLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #0284c7;");

        Label windLabel = new Label(String.format("💨 %.1f km/h", forecast.getWindSpeed()));
        windLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        VBox card = new VBox(6,
                dateLabel,
                emojiLabel,
                conditionTextLabel,
                descTextLabel,
                maxTempLabel,
                minTempLabel,
                rainLabel,
                windLabel
        );
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(12, 10, 12, 10));
        card.setPrefWidth(125);
        card.setMinWidth(120);

        card.setStyle("-fx-background-color: white; " +
                      "-fx-background-radius: 8px; " +
                      "-fx-border-color: #e2e8f0; " +
                      "-fx-border-radius: 8px; " +
                      "-fx-border-width: 1px; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 4, 0, 0, 2);");

        return card;
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
        cityNameHeaderLabel.setText("City: —");
        temperatureLabel.setText("Temperature: — °C");
        conditionLabel.setText("Condition: —");
        humidityLabel.setText("Humidity: — %");
        windSpeedLabel.setText("Wind Speed: — km/h");
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
