package com.neobank.controller;

import com.neobank.ApplicationContextProvider;
import com.neobank.model.*;
import com.neobank.model.enums.TipoMovimiento;
import com.neobank.service.CuentaService;
import com.neobank.service.MovimientoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.File;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class MovimientosController {

    @FXML private Label lblUsuarioMenu;
    @FXML private ComboBox<CuentaBancaria> cmbCuentasBusqueda;
    @FXML private Label lblSaldoActual;
    @FXML private TableView<Movimiento> tablaMovimientos;
    @FXML private TableColumn<Movimiento, String> colFecha;
    @FXML private TableColumn<Movimiento, String> colTipo;
    @FXML private TableColumn<Movimiento, BigDecimal> colMonto;
    @FXML private TableColumn<Movimiento, String> colDesc;
    @FXML private VBox panelFormulario;
    @FXML private ComboBox<TipoMovimiento> cmbTipoMovimiento;
    @FXML private TextField txtMonto;
    @FXML private TextField txtDescripcion;
    @FXML private Label lblMensaje;

    private Usuario usuarioActual;
    private MovimientoService movimientoService;
    private CuentaService cuentaService;

    @FXML
    public void initialize() {
        movimientoService = ApplicationContextProvider.getBean(MovimientoService.class);
        cuentaService = ApplicationContextProvider.getBean(CuentaService.class);
        configurarTabla();
        cargarCuentas();
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        if (lblUsuarioMenu != null && usuario != null) {
            lblUsuarioMenu.setText(usuario.getNombre() + " | " + usuario.getRol());
        }
    }

    private void configurarTabla() {
        colFecha.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getFecha() != null ? cell.getValue().getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""
        ));
        colTipo.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getTipoMovimiento() != null ? cell.getValue().getTipoMovimiento().name() : ""
        ));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        colMonto.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); } else {
                    Movimiento mov = getTableView().getItems().get(getIndex());
                    setText("S/ " + item.toString());
                    if (mov != null && mov.getTipoMovimiento() != null) {
                        setStyle(mov.getTipoMovimiento() == TipoMovimiento.DEPOSITO
                                ? "-fx-text-fill: #66bb6a; -fx-font-weight: bold;"
                                : "-fx-text-fill: #ef5350; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void cargarCuentas() {
        List<CuentaBancaria> cuentas = cuentaService.listarTodas();
        cmbCuentasBusqueda.setItems(FXCollections.observableArrayList(cuentas));
        cmbCuentasBusqueda.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(CuentaBancaria c) { return c != null ? c.getNumeroCuenta() + " - Saldo: S/" + c.getSaldo() : ""; }
            @Override public CuentaBancaria fromString(String string) { return null; }
        });
        cmbTipoMovimiento.setItems(FXCollections.observableArrayList(TipoMovimiento.DEPOSITO, TipoMovimiento.RETIRO));
    }

    @FXML private void buscarMovimientos() {
        CuentaBancaria cuenta = cmbCuentasBusqueda.getValue();
        if (cuenta != null) {
            tablaMovimientos.setItems(FXCollections.observableArrayList(movimientoService.listarPorCuenta(cuenta.getId())));
            lblSaldoActual.setText("Saldo Actual: S/ " + cuenta.getSaldo());
        }
    }

    @FXML private void nuevoMovimiento() {
        if (cmbCuentasBusqueda.getValue() == null) { mostrarMensaje("⚠ Selecciona una cuenta primero.", false); return; }
        txtMonto.clear(); txtDescripcion.clear(); cmbTipoMovimiento.getSelectionModel().clearSelection();
        panelFormulario.setVisible(true); panelFormulario.setManaged(true); lblMensaje.setText("");
    }

    // 🚀 VALIDACIONES DE MOVIMIENTOS
    @FXML private void guardarMovimiento() {
        CuentaBancaria cuenta = cmbCuentasBusqueda.getValue();
        TipoMovimiento tipo = cmbTipoMovimiento.getValue();
        String montoStr = txtMonto.getText().trim();

        if (tipo == null || montoStr.isEmpty()) {
            mostrarMensaje("⚠ Completa el tipo de movimiento y el monto.", false);
            return;
        }

        try {
            BigDecimal monto = new BigDecimal(montoStr);

            // VALIDACIÓN 1: El monto no puede ser negativo ni cero
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarMensaje("⚠ El monto debe ser mayor a S/ 0.00.", false);
                return;
            }

            // VALIDACIÓN 2: No permitir retirar más del saldo disponible
            if (tipo == TipoMovimiento.RETIRO && cuenta.getSaldo().compareTo(monto) < 0) {
                mostrarMensaje("⚠ Saldo insuficiente. Solo tienes S/ " + cuenta.getSaldo(), false);
                return;
            }

            movimientoService.registrar(cuenta.getId(), tipo, monto, txtDescripcion.getText().trim());
            mostrarMensaje("✔ Movimiento exitoso.", true);
            panelFormulario.setVisible(false); panelFormulario.setManaged(false);

            // Refrescar datos visuales
            Optional<CuentaBancaria> actOpt = cuentaService.listarTodas().stream().filter(c -> c.getId().equals(cuenta.getId())).findFirst();
            if(actOpt.isPresent()) {
                int index = cmbCuentasBusqueda.getItems().indexOf(cuenta);
                if(index >= 0) cmbCuentasBusqueda.getItems().set(index, actOpt.get());
                cmbCuentasBusqueda.setValue(actOpt.get()); buscarMovimientos();
            }
        } catch (NumberFormatException e) {
            mostrarMensaje("⚠ Monto inválido. Ingresa solo números (Ej: 100.50).", false);
        } catch (IllegalArgumentException e) {
            mostrarMensaje("⚠ " + e.getMessage(), false);
        }
    }

    @FXML private void cancelarForm() { panelFormulario.setVisible(false); panelFormulario.setManaged(false); }
    private void mostrarMensaje(String msg, boolean exito) { lblMensaje.setStyle(exito ? "-fx-text-fill: #66bb6a;" : "-fx-text-fill: #ef5350;"); lblMensaje.setText(msg); }

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
    @FXML private void irMovimientos() { }
    @FXML private void irTransferencias() { navegar("Transferencias.fxml", 1280, 720); }
    @FXML private void irTarjetas() { navegar("Tarjetas.fxml", 1280, 720); }
    @FXML private void cerrarSesion() { navegar("Login.fxml", 900, 600); }
}