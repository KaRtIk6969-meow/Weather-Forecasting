package com.weather.view;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * MainView represents the initial view presentation for the application.
 * In accordance with MVC principles, view construction is separated from application lifecycle.
 */
public class MainView {

    /**
     * Builds and returns the root visual node for the main scene.
     *
     * @return Parent container containing the welcome interface
     */
    public Parent getView() {
        Label welcomeLabel = new Label("Welcome to Weather Forecasting & Visualization System");
        welcomeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-text-alignment: center;");
        welcomeLabel.setWrapText(true);

        Label subLabel = new Label("College Java Project • Clean Architecture Foundation");
        subLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label statusBadge = new Label("● Foundation Initialized (Model • Service • Controller • View • Util)");
        statusBadge.setStyle("-fx-font-size: 12px; -fx-text-fill: #0d9488; -fx-padding: 6px 12px; -fx-background-color: #ccfbf1; -fx-background-radius: 9999px;");

        VBox layout = new VBox(16, welcomeLabel, subLabel, statusBadge);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 40px; -fx-background-color: #f8fafc;");

        return layout;
    }
}
