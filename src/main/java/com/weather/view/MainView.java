package com.weather.view;

import com.weather.controller.WeatherController;
import com.weather.model.WeatherData;
import com.weather.model.WeatherResponse;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * MainView is responsible for building and managing the user interface.
 * 
 * Concurrency & Threading Model (JavaFX):
 * - UI components and event handlers run on the "JavaFX Application Thread".
 * - Network operations (such as HTTP calls to Open-Meteo) are executed asynchronously
 *   inside a JavaFX {@link javafx.concurrent.Task} on a background worker thread.
 * - This prevents the UI from freezing or lagging while waiting for network responses.
 * - The Task's lifecycle hooks (`setOnSucceeded` and `setOnFailed`) automatically run
 *   on the JavaFX Application Thread, making it completely thread-safe to update UI labels.
 */
public class MainView {

    // Controller reference to delegate business logic
    private final WeatherController controller;

    // UI Input controls
    private TextField cityInputField;
    private Button searchButton;

    // UI Output labels for weather display
    private Label cityNameHeaderLabel;
    private Label temperatureLabel;
    private Label conditionLabel;
    private Label humidityLabel;
    private Label windSpeedLabel;

    // UI Status / Error label
    private Label statusMessageLabel;

    /**
     * Constructor initializing the View with its corresponding Controller.
     */
    public MainView() {
        this.controller = new WeatherController();
    }

    /**
     * Builds and returns the root JavaFX Parent node for the scene.
     *
     * @return Parent container holding the UI components
     */
    public Parent getView() {
        // 1. App Title
        Label titleLabel = new Label("Weather Forecasting & Visualization System");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label subtitleLabel = new Label("MVC Architecture • Asynchronous Background Tasks • Live API");
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        // 2. City Input & Search Button
        cityInputField = new TextField();
        cityInputField.setPromptText("Enter city name (e.g. Chandigarh, London, Tokyo)...");
        cityInputField.setPrefWidth(320);
        cityInputField.setStyle("-fx-font-size: 13px; -fx-padding: 8px 12px;");

        searchButton = new Button("Search Weather");
        searchButton.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-background-color: #2563eb; -fx-text-fill: white; -fx-padding: 8px 16px; -fx-cursor: hand;");

        // Trigger search on button click or pressing Enter inside the TextField
        searchButton.setOnAction(event -> performSearch());
        cityInputField.setOnAction(event -> performSearch());

        HBox searchBox = new HBox(10, cityInputField, searchButton);
        searchBox.setAlignment(Pos.CENTER);

        // 3. Status / Error Message Label
        statusMessageLabel = new Label("Enter a city name above and click 'Search Weather'.");
        statusMessageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");
        statusMessageLabel.setWrapText(true);

        // 4. Weather Display Labels
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

        // Container card for weather metrics
        VBox weatherCard = new VBox(10,
                cityNameHeaderLabel,
                temperatureLabel,
                conditionLabel,
                humidityLabel,
                windSpeedLabel
        );
        weatherCard.setAlignment(Pos.CENTER_LEFT);
        weatherCard.setPadding(new Insets(16, 24, 16, 24));
        weatherCard.setMaxWidth(460);
        weatherCard.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-border-width: 1px;");

        // 5. Root Layout
        VBox rootLayout = new VBox(18,
                titleLabel,
                subtitleLabel,
                searchBox,
                statusMessageLabel,
                weatherCard
        );
        rootLayout.setAlignment(Pos.TOP_CENTER);
        rootLayout.setPadding(new Insets(30, 20, 30, 20));
        rootLayout.setStyle("-fx-background-color: #f8fafc;");

        return rootLayout;
    }

    /**
     * Executes when the user initiates a search.
     * 
     * Uses JavaFX Task to perform the HTTP network operations on a background thread,
     * maintaining high UI responsiveness and 60 FPS animation/interaction.
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

        // 2. Provide Immediate UI Feedback & Disable Controls to prevent duplicate requests
        searchButton.setDisable(true);
        cityInputField.setDisable(true);
        statusMessageLabel.setText("⏳ Loading live weather data for \"" + cityName + "\"...");
        statusMessageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2563eb; -fx-font-weight: bold;");

        // 3. Create a JavaFX Task to execute the network search in the background
        Task<WeatherResponse> weatherSearchTask = new Task<>() {
            @Override
            protected WeatherResponse call() throws Exception {
                // IMPORTANT: This call() method runs on a BACKGROUND WORKER THREAD!
                // Never update or touch JavaFX UI controls directly inside this method.
                return controller.searchWeather(cityName);
            }
        };

        // 4. On Task Success (JavaFX automatically calls this on the JavaFX Application Thread!)
        weatherSearchTask.setOnSucceeded(event -> {
            // Re-enable input controls
            searchButton.setDisable(false);
            cityInputField.setDisable(false);

            WeatherResponse response = weatherSearchTask.getValue();

            if (response == null || response.getCurrentWeather() == null) {
                // City was not found or API returned an error
                statusMessageLabel.setText("❌ Could not retrieve weather for \"" + cityName + "\". Please verify the city name or check your internet connection.");
                statusMessageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");

                // Reset labels
                resetWeatherLabels();
            } else {
                // Populate UI with LIVE weather data
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
            }
        });

        // 5. On Task Failure (JavaFX automatically calls this on the JavaFX Application Thread!)
        weatherSearchTask.setOnFailed(event -> {
            // Re-enable input controls
            searchButton.setDisable(false);
            cityInputField.setDisable(false);

            Throwable exception = weatherSearchTask.getException();
            String errorMessage = (exception != null && exception.getMessage() != null)
                    ? exception.getMessage() : "Unknown network error";

            statusMessageLabel.setText("❌ Network request failed: " + errorMessage);
            statusMessageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626; -fx-font-weight: bold;");

            resetWeatherLabels();
        });

        // 6. Launch the Task on a dedicated background Thread
        Thread backgroundThread = new Thread(weatherSearchTask);
        // Setting daemon to true ensures this thread will not keep the JVM alive if the user closes the app
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    /**
     * Resets the weather metric labels to placeholder values.
     */
    private void resetWeatherLabels() {
        cityNameHeaderLabel.setText("City: —");
        temperatureLabel.setText("Temperature: — °C");
        conditionLabel.setText("Condition: —");
        humidityLabel.setText("Humidity: — %");
        windSpeedLabel.setText("Wind Speed: — km/h");
    }
}
