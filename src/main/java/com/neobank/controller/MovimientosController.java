package com.neobank.controller;

import com.neobank.ApplicationContextProvider;
import com.neobank.model.*;
import com.neobank.model.enums.TipoMovimiento;
import com.neobank.service.ClienteService;
import com.neobank.service.CuentaService;
import com.neobank.service.MovimientoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private ClienteService clienteService;

    // 🚀 Lista en memoria para respaldar la búsqueda sin perder datos
    private ObservableList<CuentaBancaria> listaCuentasOriginal = FXCollections.observableArrayList();

    private final String ESTILO_NORMAL = "-fx-background-color: #FFFFFF; -fx-border-color: #CCCCCC; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8; -fx-font-size: 14px; -fx-text-fill: #1A1A1A;";
    private final String ESTILO_ERROR = "-fx-background-color: #FFFFFF; -fx-border-color: #E53935; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8; -fx-font-size: 14px; -fx-text-fill: #1A1A1A;";
    private final String ESTILO_CMB_NORMAL = "-fx-background-color: #FFFFFF; -fx-border-color: #CCCCCC; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6;";
    private final String ESTILO_CMB_ERROR = "-fx-background-color: #FFFFFF; -fx-border-color: #E53935; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6;";

    @FXML
    public void initialize() {
        movimientoService = ApplicationContextProvider.getBean(MovimientoService.class);
        cuentaService = ApplicationContextProvider.getBean(CuentaService.class);
        clienteService = ApplicationContextProvider.getBean(ClienteService.class);

        configurarTabla();
        cargarCuentas();
        configurarValidacionesDinamicas();
        configurarLimpiezaAutomatica(lblMensaje, txtDescripcion);
    }

    private void configurarValidacionesDinamicas() {
        txtMonto.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (!newValue.matches("^\\d*\\.?\\d*$")) txtMonto.setStyle(ESTILO_ERROR);
                else txtMonto.setStyle(ESTILO_NORMAL);
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
        listaCuentasOriginal.setAll(cuentaService.listarTodas());
        cmbCuentasBusqueda.setItems(listaCuentasOriginal);

        cmbCuentasBusqueda.setConverter(new javafx.util.StringConverter<>() {
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
                return listaCuentasOriginal.stream()
                        .filter(c -> toString(c).equals(string)).findFirst().orElse(null);
            }
        });

        // 🚀 HACK: Transformamos el ComboBox en un AutoComplete Editable
        cmbCuentasBusqueda.setEditable(true);

        // 🚀 FILTRO ESTRICTO EN TIEMPO REAL: Solo evalúa el DNI
        cmbCuentasBusqueda.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            // Ignoramos el evento si el texto es el resultado de hacer clic en una opción
            if (newValue == null || newValue.startsWith("DNI:")) {
                return;
            }

            if (newValue.trim().isEmpty()) {
                cmbCuentasBusqueda.setItems(listaCuentasOriginal);
                cmbCuentasBusqueda.hide();
            } else {
                ObservableList<CuentaBancaria> filtradas = FXCollections.observableArrayList();
                for (CuentaBancaria c : listaCuentasOriginal) {
                    if (c.getIdCliente() != null) {
                        for (Cliente cli : clienteService.listarTodos()) {
                            if (cli.getId().equals(c.getIdCliente())) {
                                // 🔒 CONDICIÓN BLINDADA: Exclusivamente DNI
                                if (cli.getDni() != null && cli.getDni().contains(newValue.trim())) {
                                    filtradas.add(c);
                                }
                                break;
                            }
                        }
                    }
                }

                // Actualizamos la UI sin borrar lo que el cajero está escribiendo
                javafx.application.Platform.runLater(() -> {
                    cmbCuentasBusqueda.setItems(filtradas);
                    cmbCuentasBusqueda.getEditor().setText(newValue);
                    cmbCuentasBusqueda.getEditor().positionCaret(newValue.length());
                    if (!filtradas.isEmpty()) {
                        cmbCuentasBusqueda.show(); // Abre el menú con los resultados
                    } else {
                        cmbCuentasBusqueda.hide();
                    }
                });
            }
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
        if (cmbCuentasBusqueda.getValue() == null) { mostrarNotificacionError("Falta Cuenta", "Seleccione una cuenta superior."); return; }
        txtMonto.clear(); txtDescripcion.clear(); cmbTipoMovimiento.getSelectionModel().clearSelection();
        txtMonto.setStyle(ESTILO_NORMAL); cmbTipoMovimiento.setStyle(ESTILO_CMB_NORMAL);
        panelFormulario.setVisible(true); panelFormulario.setManaged(true); lblMensaje.setText("");
    }

    @FXML private void guardarMovimiento() {
        boolean hayError = false;
        if (cmbTipoMovimiento.getValue() == null) { cmbTipoMovimiento.setStyle(ESTILO_CMB_ERROR); hayError = true; }
        if (txtMonto.getText().trim().isEmpty()) { txtMonto.setStyle(ESTILO_ERROR); hayError = true; }
        if (hayError) { mostrarNotificacionError("Campos Requeridos", "Ingrese tipo y monto."); return; }

        CuentaBancaria cuenta = cmbCuentasBusqueda.getValue();
        if (cuenta == null) { mostrarNotificacionError("Selección Inválida", "Asegúrese de seleccionar un cliente válido de la lista."); return; }

        TipoMovimiento tipo = cmbTipoMovimiento.getValue();

        try {
            BigDecimal monto = new BigDecimal(txtMonto.getText().trim());
            if (monto.compareTo(BigDecimal.ZERO) <= 0) { mostrarNotificacionError("Inválido", "Monto mayor a S/ 0.00."); return; }

            Movimiento movimientoRealizado = movimientoService.registrar(cuenta.getId(), tipo, monto, txtDescripcion.getText().trim(), usuarioActual);
            panelFormulario.setVisible(false); panelFormulario.setManaged(false);

            String numeroOperacion = String.format("%08d", movimientoRealizado.getId());
            mostrarVoucherPantalla(numeroOperacion, cuenta.getNumeroCuenta(), tipo.name(), monto);

            Optional<CuentaBancaria> actOpt = cuentaService.listarTodas().stream().filter(c -> c.getId().equals(cuenta.getId())).findFirst();
            if(actOpt.isPresent()) {
                listaCuentasOriginal.setAll(cuentaService.listarTodas()); // Refrescamos lista oculta
                cmbCuentasBusqueda.setValue(actOpt.get());
                buscarMovimientos();
            }

        } catch (NumberFormatException e) {
            txtMonto.setStyle(ESTILO_ERROR); mostrarNotificacionError("Error", "Números válidos solamente.");
        } catch (IllegalArgumentException e) {
            mostrarNotificacionError("Rechazado", e.getMessage());
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