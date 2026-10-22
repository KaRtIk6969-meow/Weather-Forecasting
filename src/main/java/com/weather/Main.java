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

        // Create the scene with standard desktop window dimensions
        Scene scene = new Scene(mainView.getView(), 680, 480);

        // Configure and display the primary stage
        primaryStage.setTitle("Weather Forecasting & Visualization System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(550);
        primaryStage.setMinHeight(420);
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
