package com.neobank.controller;

import com.neobank.ApplicationContextProvider;
import com.neobank.model.*;
import com.neobank.model.enums.TipoMovimiento;
import com.neobank.service.CuentaService;
import com.neobank.service.MovimientoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class MovimientosController extends BaseController {

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

    @FXML private Button btnMenuDashboard;

    private MovimientoService movimientoService;
    private CuentaService cuentaService;

    // Constantes de estilo unificadas de caja completa
    private final String ESTILO_NORMAL = "-fx-background-color: #FFFFFF; -fx-border-color: #CCCCCC; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8; -fx-font-size: 14px; -fx-text-fill: #1A1A1A;";
    private final String ESTILO_ERROR = "-fx-background-color: #FFFFFF; -fx-border-color: #E53935; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8; -fx-font-size: 14px; -fx-text-fill: #1A1A1A;";
    private final String ESTILO_CMB_NORMAL = "-fx-background-color: #FFFFFF; -fx-border-color: #CCCCCC; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6;";
    private final String ESTILO_CMB_ERROR = "-fx-background-color: #FFFFFF; -fx-border-color: #E53935; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6;";

    @FXML
    public void initialize() {
        movimientoService = ApplicationContextProvider.getBean(MovimientoService.class);
        cuentaService = ApplicationContextProvider.getBean(CuentaService.class);
        configurarTabla();
        cargarCuentas();
        configurarValidacionesDinamicas();
        configurarLimpiezaAutomatica(lblMensaje, txtDescripcion);
    }

    private void configurarValidacionesDinamicas() {
        // Validación en tiempo real para Monto (Solo números y punto decimal)
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

        cmbTipoMovimiento.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) cmbTipoMovimiento.setStyle(ESTILO_CMB_NORMAL);
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
                                ? "-fx-text-fill: #43A047; -fx-font-weight: bold;"
                                : "-fx-text-fill: #E53935; -fx-font-weight: bold;");
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

    @FXML private void nuevoMovement() { }

    @FXML private void nuevoMovimiento() {
        if (cmbCuentasBusqueda.getValue() == null) { mostrarNotificacionError("Falta Cuenta", "Primero debe seleccionar una cuenta de la lista superior."); return; }
        txtMonto.clear(); txtDescripcion.clear(); cmbTipoMovimiento.getSelectionModel().clearSelection();
        txtMonto.setStyle(ESTILO_NORMAL); cmbTipoMovimiento.setStyle(ESTILO_CMB_NORMAL);
        panelFormulario.setVisible(true); panelFormulario.setManaged(true); lblMensaje.setText("");
    }

    @FXML private void guardarMovimiento() {
        boolean hayError = false;

        if (cmbTipoMovimiento.getValue() == null) { cmbTipoMovimiento.setStyle(ESTILO_CMB_ERROR); hayError = true; }
        if (txtMonto.getText().trim().isEmpty()) { txtMonto.setStyle(ESTILO_ERROR); hayError = true; }

        if (hayError) {
            mostrarNotificacionError("Campos Requeridos", "Debe ingresar el tipo de operación y la cantidad de dinero.");
            return;
        }

        CuentaBancaria cuenta = cmbCuentasBusqueda.getValue();
        TipoMovimiento tipo = cmbTipoMovimiento.getValue();
        String montoStr = txtMonto.getText().trim();

        try {
            BigDecimal monto = new BigDecimal(montoStr);
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarNotificacionError("Valor Inválido", "El monto de la transacción debe ser mayor a S/ 0.00.");
                return;
            }

            movimientoService.registrar(cuenta.getId(), tipo, monto, txtDescripcion.getText().trim(), usuarioActual);
            panelFormulario.setVisible(false); panelFormulario.setManaged(false);
            mostrarNotificacionExito("Transacción Exitosa", "Se ha registrado el " + tipo.name() + " por un valor de S/ " + monto + " en la cuenta.");

            Optional<CuentaBancaria> actOpt = cuentaService.listarTodas().stream().filter(c -> c.getId().equals(cuenta.getId())).findFirst();
            if(actOpt.isPresent()) {
                int index = cmbCuentasBusqueda.getItems().indexOf(cuenta);
                if(index >= 0) cmbCuentasBusqueda.getItems().set(index, actOpt.get());
                cmbCuentasBusqueda.setValue(actOpt.get()); buscarMovimientos();
            }
        } catch (NumberFormatException e) {
            txtMonto.setStyle(ESTILO_ERROR);
            mostrarNotificacionError("Error de Formato", "Ingrese únicamente números válidos para el dinero de la transacción.");
        } catch (IllegalArgumentException e) {
            mostrarNotificacionError("Operación Rechazada", e.getMessage());
        }
    }

    @FXML private void cancelarForm() { panelFormulario.setVisible(false); panelFormulario.setManaged(false); }

    @FXML private void irDashboard() { navegar(lblUsuarioMenu, "Dashboard.fxml", 1280, 720); }
    @FXML private void irClientes() { navegar(lblUsuarioMenu, "Clientes.fxml", 1280, 720); }
    @FXML private void irCuentas() { navegar(lblUsuarioMenu, "Cuentas.fxml", 1280, 720); }
    @FXML private void irMovimientos() { }
    @FXML private void irTransferencias() { navegar(lblUsuarioMenu, "Transferencias.fxml", 1280, 720); }
    @FXML private void irTarjetas() { navegar(lblUsuarioMenu, "Tarjetas.fxml", 1280, 720); }
    @FXML private void cerrarSesion() { navegar(lblUsuarioMenu, "Login.fxml", 900, 600); }
}