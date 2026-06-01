package com.neobank.controller;

import com.neobank.ApplicationContextProvider;
import com.neobank.model.CuentaBancaria;
import com.neobank.model.Usuario;
import com.neobank.service.CuentaService;
import com.neobank.service.MovimientoService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public class TransferenciasController {

    @FXML private Label lblUsuarioMenu;
    @FXML private ComboBox<CuentaBancaria> cmbCuentaOrigen;
    @FXML private TextField txtCuentaDestino;
    @FXML private TextField txtMonto;
    @FXML private TextField txtDescripcion;
    @FXML private Label lblMensaje;
    @FXML private Label lblSaldoDisponible;

    private Usuario usuarioActual;
    private CuentaService cuentaService;
    private MovimientoService movimientoService;

    @FXML
    public void initialize() {
        cuentaService = ApplicationContextProvider.getBean(CuentaService.class);
        movimientoService = ApplicationContextProvider.getBean(MovimientoService.class);
        cargarCuentas();
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        if (lblUsuarioMenu != null && usuario != null) {
            lblUsuarioMenu.setText(usuario.getNombre() + " | " + usuario.getRol());
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
        CuentaBancaria origen = cmbCuentaOrigen.getValue();
        String destino = txtCuentaDestino.getText().trim();
        String montoStr = txtMonto.getText().trim();
        String desc = txtDescripcion.getText().trim();

        // 1. Validaciones básicas de campos vacíos
        if (origen == null || destino.isEmpty() || montoStr.isEmpty()) {
            mostrarMensaje("⚠ Por favor, completa todos los datos.", false);
            return;
        }

        // 2. Validación: Verificar si la cuenta destino existe
        boolean cuentaDestinoExiste = cuentaService.listarTodas().stream()
                .anyMatch(c -> c.getNumeroCuenta().equals(destino));

        if (!cuentaDestinoExiste) {
            mostrarMensaje("⚠ La cuenta destino '" + destino + "' no existe.", false);
            return;
        }

        // 3. Validación: No transferir a la misma cuenta
        if (origen.getNumeroCuenta().equals(destino)) {
            mostrarMensaje("⚠ No puedes transferir a tu misma cuenta.", false);
            return;
        }

        try {
            BigDecimal monto = new BigDecimal(montoStr);

            // 4. Validación: El monto debe ser mayor a 0
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarMensaje("⚠ El monto debe ser mayor a 0.", false);
                return;
            }

            // 5. Validación: Saldo insuficiente
            if (origen.getSaldo().compareTo(monto) < 0) {
                mostrarMensaje("⚠ Saldo insuficiente.", false);
                return;
            }

            movimientoService.transferir(origen.getId(), destino, monto, desc);
            mostrarMensaje("✔ Transferencia exitosa.", true);

            txtCuentaDestino.clear();
            txtMonto.clear();
            txtDescripcion.clear();
            cargarCuentas();
            cmbCuentaOrigen.getSelectionModel().clearSelection();
            lblSaldoDisponible.setText("Saldo Disponible: S/ 0.00");

        } catch (NumberFormatException e) {
            mostrarMensaje("⚠ Monto inválido. Ingresa solo números.", false);
        } catch (IllegalArgumentException e) {
            mostrarMensaje("⚠ " + e.getMessage(), false);
        }
    }

    private void mostrarMensaje(String msg, boolean exito) {
        lblMensaje.setStyle(exito ? "-fx-text-fill: #66bb6a;" : "-fx-text-fill: #ef5350;");
        lblMensaje.setText(msg);
    }

    private void navegar(String fxml, int w, int h) {
        try {
            String ruta = System.getProperty("user.dir") + "/src/main/resources/fxml/" + fxml;
            FXMLLoader loader = new FXMLLoader(new File(ruta).toURI().toURL());
            Parent root = loader.load();
            Object ctrl = loader.getController();

            if (ctrl instanceof DashboardController dc) dc.setUsuario(usuarioActual);
            else if (ctrl instanceof ClientesController cc) cc.setUsuario(usuarioActual);
            else if (ctrl instanceof CuentasController cu) cu.setUsuario(usuarioActual);
            else if (ctrl instanceof MovimientosController mc) mc.setUsuario(usuarioActual);
            else if (ctrl instanceof TransferenciasController tc) tc.setUsuario(usuarioActual);
            else if (ctrl instanceof TarjetasController tarc) tarc.setUsuario(usuarioActual);

            Stage stage = (Stage) lblUsuarioMenu.getScene().getWindow();
            stage.setScene(new Scene(root, w, h));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void irDashboard() { navegar("Dashboard.fxml", 1280, 720); }
    @FXML private void irClientes() { navegar("Clientes.fxml", 1280, 720); }
    @FXML private void irCuentas() { navegar("Cuentas.fxml", 1280, 720); }
    @FXML private void irMovimientos() { navegar("Movimientos.fxml", 1280, 720); }
    @FXML private void irTransferencias() { }
    @FXML private void irTarjetas() { navegar("Tarjetas.fxml", 1280, 720); }
    @FXML private void cerrarSesion() { navegar("Login.fxml", 900, 600); }
}