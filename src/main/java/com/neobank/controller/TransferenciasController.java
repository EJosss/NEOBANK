package com.neobank.controller;

import com.neobank.ApplicationContextProvider;
import com.neobank.model.CuentaBancaria;
import com.neobank.model.Cliente;
import com.neobank.service.ClienteService;
import com.neobank.service.CuentaService;
import com.neobank.service.MovimientoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private ClienteService clienteService;

    // 🚀 Lista en memoria para la Cuenta Origen
    private ObservableList<CuentaBancaria> listaOrigenOriginal = FXCollections.observableArrayList();

    private final String ESTILO_NORMAL = "-fx-background-color: #FFFFFF; -fx-border-color: #CCCCCC; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8; -fx-font-size: 14px; -fx-text-fill: #1A1A1A;";
    private final String ESTILO_ERROR = "-fx-background-color: #FFFFFF; -fx-border-color: #E53935; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8; -fx-font-size: 14px; -fx-text-fill: #1A1A1A;";
    private final String ESTILO_CMB_NORMAL = "-fx-background-color: #FFFFFF; -fx-border-color: #CCCCCC; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6;";
    private final String ESTILO_CMB_ERROR = "-fx-background-color: #FFFFFF; -fx-border-color: #E53935; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6;";

    @FXML
    public void initialize() {
        cuentaService = ApplicationContextProvider.getBean(CuentaService.class);
        movimientoService = ApplicationContextProvider.getBean(MovimientoService.class);
        clienteService = ApplicationContextProvider.getBean(ClienteService.class);

        cargarCuentas();
        configurarValidacionesDinamicas();
        configurarLimpiezaAutomatica(lblMensaje, txtDescripcion);
    }

    private void configurarValidacionesDinamicas() {
        txtCuentaDestino.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (!newValue.matches("^[0-9\\-]*$")) txtCuentaDestino.setStyle(ESTILO_ERROR);
                else txtCuentaDestino.setStyle(ESTILO_NORMAL);
            } else {
                txtCuentaDestino.setStyle(ESTILO_NORMAL);
            }
        });

        txtMonto.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (!newValue.matches("^\\d*\\.?\\d*$")) txtMonto.setStyle(ESTILO_ERROR);
                else txtMonto.setStyle(ESTILO_NORMAL);
            } else {
                txtMonto.setStyle(ESTILO_NORMAL);
            }
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
        listaOrigenOriginal.setAll(cuentaService.listarTodas());
        cmbCuentaOrigen.setItems(listaOrigenOriginal);

        cmbCuentaOrigen.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(CuentaBancaria c) {
                if (c == null) return "";
                String dni = "Sin DNI";
                String nombre = "";
                if (c.getIdCliente() != null) {
                    for (Cliente cli : clienteService.listarTodos()) {
                        if (cli.getId().equals(c.getIdCliente())) {
                            dni = cli.getDni();
                            nombre = cli.getNombre();
                            break;
                        }
                    }
                }
                return "DNI: " + dni + " - " + nombre + " | Cta: " + c.getNumeroCuenta();
            }

            @Override
            public CuentaBancaria fromString(String string) {
                return listaOrigenOriginal.stream()
                        .filter(c -> toString(c).equals(string)).findFirst().orElse(null);
            }
        });

        // 🚀 APLICANDO EL HACK DE AUTOCOMPLETADO PARA ORIGEN
        cmbCuentaOrigen.setEditable(true);

        cmbCuentaOrigen.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.startsWith("DNI:")) {
                return;
            }

            if (newValue.trim().isEmpty()) {
                cmbCuentaOrigen.setItems(listaOrigenOriginal);
                cmbCuentaOrigen.hide();
            } else {
                ObservableList<CuentaBancaria> filtradas = FXCollections.observableArrayList();
                for (CuentaBancaria c : listaOrigenOriginal) {
                    if (c.getIdCliente() != null) {
                        for (Cliente cli : clienteService.listarTodos()) {
                            if (cli.getId().equals(c.getIdCliente())) {
                                // 🔒 REGLA EXCLUSIVA DNI
                                if (cli.getDni() != null && cli.getDni().contains(newValue.trim())) {
                                    filtradas.add(c);
                                }
                                break;
                            }
                        }
                    }
                }

                javafx.application.Platform.runLater(() -> {
                    cmbCuentaOrigen.setItems(filtradas);
                    cmbCuentaOrigen.getEditor().setText(newValue);
                    cmbCuentaOrigen.getEditor().positionCaret(newValue.length());
                    if (!filtradas.isEmpty()) {
                        cmbCuentaOrigen.show();
                    } else {
                        cmbCuentaOrigen.hide();
                    }
                });
            }
        });

        // Forzamos actualizar saldo al seleccionar manualmente usando enter o clic
        cmbCuentaOrigen.valueProperty().addListener((obs, oldV, newV) -> actualizarSaldo());
    }

    @FXML private void actualizarSaldo() {
        CuentaBancaria cuenta = cmbCuentaOrigen.getValue();
        if (cuenta != null) {
            lblSaldoDisponible.setText("Saldo Disponible: S/ " + cuenta.getSaldo());
            cmbCuentaOrigen.setStyle(ESTILO_CMB_NORMAL);
        } else {
            lblSaldoDisponible.setText("Saldo Disponible: S/ 0.00");
        }
    }

    @FXML
    public void realizarTransferencia() {
        boolean hayError = false;

        CuentaBancaria origen = cmbCuentaOrigen.getValue();
        if (origen == null) { cmbCuentaOrigen.setStyle(ESTILO_CMB_ERROR); hayError = true; }
        if (txtCuentaDestino.getText().trim().isEmpty()) { txtCuentaDestino.setStyle(ESTILO_ERROR); hayError = true; }
        if (txtMonto.getText().trim().isEmpty()) { txtMonto.setStyle(ESTILO_ERROR); hayError = true; }

        if (hayError) {
            mostrarNotificacionError("Campos Requeridos", "Por favor, complete todos los campos obligatorios del envío.");
            return;
        }

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

            String numOperacion = "TR-" + (System.currentTimeMillis() % 10000000);
            mostrarVoucherPantalla(numOperacion, origen.getNumeroCuenta(), "TRANSFERENCIA", monto);

            txtCuentaDestino.clear(); txtMonto.clear(); txtDescripcion.clear();
            txtCuentaDestino.setStyle(ESTILO_NORMAL); txtMonto.setStyle(ESTILO_NORMAL);

            listaOrigenOriginal.setAll(cuentaService.listarTodas()); // Refrescar lista oculta
            cmbCuentaOrigen.getSelectionModel().clearSelection();
            cmbCuentaOrigen.getEditor().clear(); // Limpiar la barra de búsqueda
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