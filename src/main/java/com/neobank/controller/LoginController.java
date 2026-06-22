package com.neobank.controller;

import com.neobank.ApplicationContextProvider;
import com.neobank.model.Usuario;
import com.neobank.service.UsuarioService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Optional;

public class LoginController extends BaseController {

    @FXML private VBox panelLogin;
    @FXML private VBox panelCarga;
    @FXML private Label lblCargaMensaje;

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private UsuarioService usuarioService;

    @FXML
    public void initialize() {
        usuarioService = ApplicationContextProvider.getBean(UsuarioService.class);
        configurarLimpiezaAutomatica(lblError, txtUsuario, txtPassword);
    }

    @Override
    protected void actualizarInfoUsuario() {}

    @FXML
    private void handleLogin() {
        String username = txtUsuario.getText().trim();
        String password = txtPassword.getText().trim();

        limpiarBordes(txtUsuario, txtPassword);
        boolean hayError = false;

        if (username.isEmpty()) { marcarCampoErroneo(txtUsuario); hayError = true; }
        if (password.isEmpty()) { marcarCampoErroneo(txtPassword); hayError = true; }

        if (hayError) {
            mostrarMensajeUniversal(lblError, "⚠ Completa los campos marcados en rojo", false);
            return;
        }

        Optional<Usuario> usuario = usuarioService.login(username, password);

        if (usuario.isPresent()) {
            Usuario userOpt = usuario.get();
            this.setUsuario(userOpt);

            // 🚀 INICIO DEL FEEDBACK DE CARGA ANIMADO (2 FASES)

            // Ocultar login y mostrar panel de carga
            panelLogin.setVisible(false);
            panelLogin.setManaged(false);
            panelCarga.setVisible(true);
            panelCarga.setManaged(true);

            // FASE 1: Mensaje de verificación simulada
            lblCargaMensaje.setText("Validando seguridad del sistema...");
            lblCargaMensaje.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

            // Temporizador 1 (Dura 1.2 segundos)
            PauseTransition fase1 = new PauseTransition(Duration.seconds(1.2));
            fase1.setOnFinished(e -> {

                // FASE 2: Mensaje de Bienvenida (Cambiamos el texto a verde y más grande)
                lblCargaMensaje.setText("✔ ¡Bienvenido, " + userOpt.getNombre() + "!");
                lblCargaMensaje.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #43A047;");

                // Temporizador 2 (Dura 1.5 segundos para que alcances a leer la bienvenida)
                PauseTransition fase2 = new PauseTransition(Duration.seconds(1.5));
                fase2.setOnFinished(e2 -> {
                    // FASE 3: Redirección al sistema según tu rol
                    if (userOpt.getRol() == com.neobank.model.enums.RolUsuario.CAJERO) {
                        navegar(lblError, "Clientes.fxml", 1280, 720);
                    } else {
                        navegar(lblError, "Dashboard.fxml", 1280, 720);
                    }
                });
                fase2.play(); // Inicia el segundo temporizador
            });
            fase1.play(); // Inicia el primer temporizador

        } else {
            mostrarMensajeUniversal(lblError, "✖ Usuario o contraseña incorrectos", false);
            marcarCampoErroneo(txtUsuario);
            marcarCampoErroneo(txtPassword);
        }
    }
}