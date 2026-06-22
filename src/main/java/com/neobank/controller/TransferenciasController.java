package com.neobank.controller;

import com.neobank.ApplicationContextProvider;
import com.neobank.model.CuentaBancaria;
import com.neobank.service.CuentaService;
import com.neobank.service.MovimientoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.math.BigDecimal;
import java.util.List;

public class TransferenciasController extends BaseController {

    @FXML private Label lblUsuarioMenu;
    @FXML private ComboBox<CuentaBancaria> cmbCuentaOrigen;
    @FXML private TextField txtCuentaDestino;
    @FXML private TextField txtMonto;
    @FXML private TextField txtDescripcion;
    @FXML private Label lblMensaje;
    @FXML private Label lblSaldoDisponible;

    @FXML private Button btnMenuDashboard;

    private CuentaService cuentaService;
    private MovimientoService movimientoService;

    // Constantes de estilo de caja completa unificadas
    private final String ESTILO_NORMAL = "-fx-background-color: #FFFFFF; -fx-border-color: #CCCCCC; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8; -fx-font-size: 14px; -fx-text-fill: #1A1A1A;";
    private final String ESTILO_ERROR = "-fx-background-color: #FFFFFF; -fx-border-color: #E53935; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8; -fx-font-size: 14px; -fx-text-fill: #1A1A1A;";
    private final String ESTILO_CMB_NORMAL = "-fx-background-color: #FFFFFF; -fx-border-color: #CCCCCC; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6;";
    private final String ESTILO_CMB_ERROR = "-fx-background-color: #FFFFFF; -fx-border-color: #E53935; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6;";

    @FXML
    public void initialize() {
        cuentaService = ApplicationContextProvider.getBean(CuentaService.class);
        movimientoService = ApplicationContextProvider.getBean(MovimientoService.class);
        cargarCuentas();
        configurarValidacionesDinamicas();
        configurarLimpiezaAutomatica(lblMensaje, txtDescripcion);
    }

    private void configurarValidacionesDinamicas() {
        // Regla: Cuenta Destino (Solo números y guiones)
        txtCuentaDestino.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (!newValue.matches("^[0-9\\-]*$")) {
                    txtCuentaDestino.setStyle(ESTILO_ERROR);
                } else {
                    txtCuentaDestino.setStyle(ESTILO_NORMAL);
                }
            } else {
                txtCuentaDestino.setStyle(ESTILO_NORMAL);
            }
        });

        // Regla: Monto (Solo números decimales)
        txtMonto.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (!newValue.matches("^\\d*\\.?\\d*$")) {
                    txtMonto.setStyle(ESTILO_ERROR);
                } else {
                    txtMonto.setStyle(ESTILO_NORMAL);
                }
            } else {
                txtMonto.setStyle(ESTILO_NORMAL);
            }
        });

        cmbCuentaOrigen.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) cmbCuentaOrigen.setStyle(ESTILO_CMB_NORMAL);
        });
    }

    @Override
    protected void actualizarInfoUsuario() {
        if (lblUsuarioMenu != null && usuarioActual != null) {
            lblUsuarioMenu.setText(usuarioActual.getNombre() + " | " + usuarioActual.getRol());
            if (usuarioActual.getRol() == com.neobank.model.enums.RolUsuario.CAJERO) {
                if (btnMenuDashboard != null) { btnMenuDashboard.setVisible(false); btnMenuDashboard.setManaged(false); }
            }
        }
    }

    private void cargarCuentas() {
        List<CuentaBancaria> cuentas = cuentaService.listarTodas();
        cmbCuentaOrigen.setItems(FXCollections.observableArrayList(cuentas));
        cmbCuentaOrigen.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(CuentaBancaria c) { return c != null ? c.getNumeroCuenta() : ""; }
            @Override public CuentaBancaria fromString(String string) { return null; }
        });
    }

    @FXML private void actualizarSaldo() {
        CuentaBancaria cuenta = cmbCuentaOrigen.getValue();
        if (cuenta != null) lblSaldoDisponible.setText("Saldo Disponible: S/ " + cuenta.getSaldo());
    }

    @FXML
    public void realizarTransferencia() {
        boolean hayError = false;

        if (cmbCuentaOrigen.getValue() == null) { cmbCuentaOrigen.setStyle(ESTILO_CMB_ERROR); hayError = true; }
        if (txtCuentaDestino.getText().trim().isEmpty()) { txtCuentaDestino.setStyle(ESTILO_ERROR); hayError = true; }
        if (txtMonto.getText().trim().isEmpty()) { txtMonto.setStyle(ESTILO_ERROR); hayError = true; }

        if (hayError) {
            mostrarNotificacionError("Campos Requeridos", "Por favor, complete todos los campos obligatorios del envío.");
            return;
        }

        CuentaBancaria origen = cmbCuentaOrigen.getValue();
        String destino = txtCuentaDestino.getText().trim();
        String montoStr = txtMonto.getText().trim();
        String desc = txtDescripcion.getText().trim();

        try {
            BigDecimal monto = new BigDecimal(montoStr);
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarNotificacionError("Monto Inválido", "El monto a transferir debe ser superior a S/ 0.00.");
                return;
            }

            movimientoService.transferir(origen.getId(), destino, monto, desc, usuarioActual);
            mostrarNotificacionExito("Transferencia Procesada", "Se han enviado S/ " + monto + " a la cuenta '" + destino + "' con éxito.");

            txtCuentaDestino.clear(); txtMonto.clear(); txtDescripcion.clear();
            txtCuentaDestino.setStyle(ESTILO_NORMAL); txtMonto.setStyle(ESTILO_NORMAL);
            cargarCuentas(); cmbCuentaOrigen.getSelectionModel().clearSelection();
            lblSaldoDisponible.setText("Saldo Disponible: S/ 0.00");

        } catch (NumberFormatException e) {
            txtMonto.setStyle(ESTILO_ERROR);
            mostrarNotificacionError("Formato de Monto", "El dinero ingresado no posee un formato numérico válido.");
        } catch (IllegalArgumentException e) {
            mostrarNotificacionError("Transferencia Denegada", e.getMessage());
        }
    }

    @FXML private void irDashboard() { navegar(lblUsuarioMenu, "Dashboard.fxml", 1280, 720); }
    @FXML private void irClientes() { navegar(lblUsuarioMenu, "Clientes.fxml", 1280, 720); }
    @FXML private void irCuentas() { navegar(lblUsuarioMenu, "Cuentas.fxml", 1280, 720); }
    @FXML private void irMovimientos() { navegar(lblUsuarioMenu, "Movimientos.fxml", 1280, 720); }
    @FXML private void irTransferencias() { }
    @FXML private void irTarjetas() { navegar(lblUsuarioMenu, "Tarjetas.fxml", 1280, 720); }
    @FXML private void cerrarSesion() { navegar(lblUsuarioMenu, "Login.fxml", 900, 600); }
}