package com.datum.mundialfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws IOException {

        stage = primaryStage;

        scene = new Scene(loadFXML("Login"));

        stage.setScene(scene);

        //titulo que se ve en la barra de arriba y en la barra de tareas
        stage.setTitle("MundialFX");

        //icono que se ve en el header de la ventana y en la barra de tareas de windows
        //el try es por si no encuentra el archivo no truene toda la app
        try {
            stage.getIcons().add(
                    new Image(App.class.getResourceAsStream("/Images/logo.jpg"))
                    
            );
        } catch (Exception e) {
            //si falla solo lo imprime y sigue, no es critico
            System.err.println("No se pudo cargar el icono: " + e.getMessage());
        }

        stage.show();
    }

    static void setRoot(String fxml) throws IOException {

        Parent root = loadFXML(fxml);

        scene.setRoot(root);

        stage.sizeToScene();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}
