package com.weather;

import com.weather.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point for the Weather Forecasting & Visualization System.
 * 
 * Extends {@link javafx.application.Application} to manage the JavaFX application lifecycle.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Instantiate the primary view component
        MainView mainView = new MainView();

        // Create the scene with spacious dimensions to accommodate current weather, cards, and charts
        Scene scene = new Scene(mainView.getView(), 880, 720);

        // Configure and display the primary stage
        primaryStage.setTitle("Weather Forecasting & Visualization System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(740);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    /**
     * Standard Java main method.
     * Launches the JavaFX runtime environment.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
