package com.neobank.controller;

import com.neobank.ApplicationContextProvider;
import com.neobank.model.Usuario;
import com.neobank.service.UsuarioService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.File;
import java.util.Optional;

public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private UsuarioService usuarioService;

    @FXML
    public void initialize() {
        usuarioService = ApplicationContextProvider
                .getBean(UsuarioService.class);
    }

    @FXML
    private void handleLogin() {
        String username = txtUsuario.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            mostrarError("⚠ Completa todos los campos");
            return;
        }

        Optional<Usuario> usuario = usuarioService
                .login(username, password);

        if (usuario.isPresent()) {
            irAlDashboard(usuario.get());
        } else {
            mostrarError("✖ Usuario o contraseña incorrectos");
        }
    }

    private void irAlDashboard(Usuario usuario) {
        try {
            String ruta = System.getProperty("user.dir")
                    + "/src/main/resources/fxml/Dashboard.fxml";
            FXMLLoader loader = new FXMLLoader(
                    new File(ruta).toURI().toURL()
            );
            Parent root = loader.load();
            DashboardController controller =
                    loader.getController();
            controller.setUsuario(usuario);
            Stage stage = (Stage) txtUsuario
                    .getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 720));
            stage.setResizable(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setStyle("-fx-text-fill: #ef5350;");
        lblError.setText(mensaje);
    }
}