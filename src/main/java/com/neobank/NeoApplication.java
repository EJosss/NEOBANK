package com.neobank;

import io.micronaut.context.ApplicationContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class NeoApplication extends Application {

    private ApplicationContext micronautContext;

    @Override
    public void init() {
        // Arranca Micronaut
        micronautContext = ApplicationContext.run();
        ApplicationContextProvider.setContext(micronautContext);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Buscamos el FXML usando el ClassLoader (la forma correcta)
            URL fxmlLocation = getClass().getResource("/fxml/Login.fxml");

            // 2. Validación de seguridad por si Maven no compiló bien el archivo
            if (fxmlLocation == null) {
                throw new IllegalStateException("¡Error! No se encontró /fxml/Login.fxml. Asegúrate de compilar con el martillo verde.");
            }

            System.out.println("Cargando FXML desde: " + fxmlLocation);

            // 3. Cargamos la vista
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            // 4. Configuramos la ventana
            Scene scene = new Scene(root, 900, 600);
            primaryStage.setTitle("NeoBank System");
            primaryStage.setScene(scene);
           // primaryStage.setMaximized(true);
            primaryStage.setResizable(true);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("Ocurrió un error al cargar la interfaz de JavaFX:");
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Apaga Micronaut al cerrar la ventana
        if (micronautContext != null && micronautContext.isRunning()) {
            micronautContext.close();
        }
    }
}