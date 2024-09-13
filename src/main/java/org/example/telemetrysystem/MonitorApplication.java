package org.example.telemetrysystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MonitorApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Cargar el archivo FXML
        Parent root = FXMLLoader.load(getClass().getResource("MonitorView.fxml"));
        stage.setTitle("SNMP Monitor");
        stage.setScene(new Scene(root, 1280, 600)); // Ajustar tamaño de la ventana
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}