package com.neobank.controller;

import com.neobank.ApplicationContextProvider;
import com.neobank.model.*;
import com.neobank.model.enums.*;
import com.neobank.service.ClienteService;
import com.neobank.service.CuentaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

public class CuentasController extends BaseController {

    @FXML private Label lblUsuarioMenu;
    @FXML private TableView<CuentaBancaria> tablaCuentas;
    @FXML private TableColumn<CuentaBancaria, Long> colId;
    @FXML private TableColumn<CuentaBancaria, String> colNumero;
    @FXML private TableColumn<CuentaBancaria, String> colCliente;
    @FXML private TableColumn<CuentaBancaria, String> colTipo;
    @FXML private TableColumn<CuentaBancaria, BigDecimal> colSaldo;
    @FXML private TableColumn<CuentaBancaria, String> colEstado;
    @FXML private TextField txtBuscar;
    @FXML private Label lblMensaje;

    @FXML private VBox panelFormulario;
    @FXML private ComboBox<Cliente> cmbClientes;
    @FXML private ComboBox<TipoCuenta> cmbTipo;
    @FXML private Label lblErrorForm;

    @FXML private Button btnEliminar;
    @FXML private Button btnMenuDashboard;
    @FXML private CheckBox chkVerCerradas;

    private CuentaService cuentaService;
    private ClienteService clienteService;

    // Lista observable para gestionar el filtrado dinámico
    private ObservableList<CuentaBancaria> listaCuentasMaster = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cuentaService = ApplicationContextProvider.getBean(CuentaService.class);
        clienteService = ApplicationContextProvider.getBean(ClienteService.class);
        configurarTabla();
        cargarDatos();
        configurarMenuContextual();
        configurarBusquedaEnTiempoReal(); // 🚀 Activamos el súper filtro
        configurarLimpiezaAutomatica(lblErrorForm, cmbClientes, cmbTipo);
    }

    private void configurarBusquedaEnTiempoReal() {
        FilteredList<CuentaBancaria> listaFiltrada = new FilteredList<>(listaCuentasMaster, cuenta -> cuenta.getEstado() == EstadoCuenta.ACTIVA);

        // Función unificada que evalúa ambos componentes reactivos
        Runnable actualizarFiltro = () -> {
            String textoBusqueda = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase().trim() : "";
            boolean mostrarCerradas = chkVerCerradas.isSelected();

            listaFiltrada.setPredicate(cuenta -> {
                // 1. Condición de Estado Dinámica
                EstadoCuenta estadoEsperado = mostrarCerradas ? EstadoCuenta.CERRADA : EstadoCuenta.ACTIVA;
                if (cuenta.getEstado() != estadoEsperado) {
                    return false;
                }

                // 2. Condición de Texto
                if (textoBusqueda.isEmpty()) {
                    return true;
                }
                return cuenta.getNumeroCuenta() != null && cuenta.getNumeroCuenta().toLowerCase().contains(textoBusqueda);
            });
        };

        // Vinculamos los escuchadores al activador
        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> actualizarFiltro.run());
        chkVerCerradas.selectedProperty().addListener((observable, oldValue, newValue) -> actualizarFiltro.run());

        tablaCuentas.setItems(listaFiltrada);
    }

    @Override
    protected void actualizarInfoUsuario() {
        if (lblUsuarioMenu != null && usuarioActual != null) {
            lblUsuarioMenu.setText(usuarioActual.getNombre() + " | " + usuarioActual.getRol());

            if (usuarioActual.getRol() == com.neobank.model.enums.RolUsuario.CAJERO) {
                if (btnEliminar != null) { btnEliminar.setVisible(false); btnEliminar.setManaged(false); }
                if (btnMenuDashboard != null) { btnMenuDashboard.setVisible(false); btnMenuDashboard.setManaged(false); }
            }
        }
    }

    private void configurarMenuContextual() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem copiarItem = new MenuItem("📋 Copiar Número de Cuenta");

        copiarItem.setOnAction(event -> {
            CuentaBancaria cuenta = tablaCuentas.getSelectionModel().getSelectedItem();
            if (cuenta != null) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(cuenta.getNumeroCuenta());
                clipboard.setContent(content);
                mostrarNotificacionExito("Copiado al Portapapeles", "El número de cuenta " + cuenta.getNumeroCuenta() + " está listo para usarse.");
            } else {
                mostrarNotificacionError("Selección Requerida", "Por favor seleccione una cuenta.");
            }
        });

        contextMenu.getItems().add(copiarItem);
        tablaCuentas.setContextMenu(contextMenu);
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroCuenta"));

        colCliente.setCellValueFactory(cellData -> {
            Long idC = cellData.getValue().getIdCliente();
            if (idC != null) {
                for (Cliente c : clienteService.listarTodos()) {
                    if (c.getId().equals(idC)) { return new SimpleStringProperty(c.getNombre()); }
                }
                return new SimpleStringProperty("ID: " + idC);
            }
            return new SimpleStringProperty("Sin Cliente");
        });

        colTipo.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getTipoCuenta() != null ? cellData.getValue().getTipoCuenta().name() : ""
        ));

        colSaldo.setCellValueFactory(new PropertyValueFactory<>("saldo"));

        colEstado.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getEstado() != null ? cellData.getValue().getEstado().name() : ""
        ));
    }

    private void cargarDatos() {
        List<CuentaBancaria> cuentas = cuentaService.listarTodas();
        listaCuentasMaster.setAll(cuentas);
        // Si no se está buscando nada, la tabla muestra la lista master inicial
        if (txtBuscar.getText().trim().isEmpty()) {
            tablaCuentas.setItems(listaCuentasMaster);
        }
    }

    @FXML private void mostrarTodas() { txtBuscar.clear(); cargarDatos(); }

    @FXML private void nuevaCuenta() {
        cmbClientes.setItems(FXCollections.observableArrayList(clienteService.listarTodos()));
        cmbTipo.setItems(FXCollections.observableArrayList(TipoCuenta.values()));
        cmbClientes.getSelectionModel().clearSelection(); cmbTipo.getSelectionModel().clearSelection();
        panelFormulario.setVisible(true); panelFormulario.setManaged(true);
        lblErrorForm.setText("");
    }

    @FXML private void guardarCuenta() {
        if (cmbClientes.getValue() == null || cmbTipo.getValue() == null) {
            lblErrorForm.setText("⚠ Selecciona un Cliente y Tipo de Cuenta.");
            return;
        }

        Cliente cliente = cmbClientes.getValue(); TipoCuenta tipo = cmbTipo.getValue();
        String numero = "4532-" + (1000 + new Random().nextInt(9000)) + "-" + (1000 + new Random().nextInt(9000));

        CuentaBancaria nuevaCuenta = new CuentaBancaria();
        nuevaCuenta.setNumeroCuenta(numero); nuevaCuenta.setIdCliente(cliente.getId());
        nuevaCuenta.setTipoCuenta(tipo); nuevaCuenta.setSaldo(BigDecimal.ZERO);
        nuevaCuenta.setEstado(EstadoCuenta.ACTIVA);

        cuentaService.guardar(nuevaCuenta);
        cargarDatos();
        mostrarFormulario(false);
        mostrarNotificacionExito("Apertura de Cuenta", "La cuenta número '" + numero + "' vinculada al cliente '" + cliente.getNombre() + "' fue abierta con éxito.");
    }

    @FXML private void eliminarCuenta() {
        CuentaBancaria sel = tablaCuentas.getSelectionModel().getSelectedItem();
        if (sel != null) {
            try {
                cuentaService.eliminar(sel.getId(), usuarioActual);
                cargarDatos();
                mostrarNotificacionExito("Cuenta Inhabilitada", "La cuenta número '" + sel.getNumeroCuenta() + "' se encuentra ahora en estado CERRADA.");
            } catch (IllegalArgumentException e) {
                mostrarNotificacionError("Restricción de Acceso", e.getMessage());
            }
        } else {
            mostrarNotificacionError("Selección Requerida", "Debe marcar una cuenta de la lista para proceder al cierre.");
        }
    }

    @FXML private void cancelarForm() { mostrarFormulario(false); }
    private void mostrarFormulario(boolean visible) { panelFormulario.setVisible(visible); panelFormulario.setManaged(visible); }

    @FXML private void irDashboard() { navegar(lblUsuarioMenu, "Dashboard.fxml", 1280, 720); }
    @FXML private void irClientes() { navegar(lblUsuarioMenu, "Clientes.fxml", 1280, 720); }
    @FXML private void irCuentas() { }
    @FXML private void irMovimientos() { navegar(lblUsuarioMenu, "Movimientos.fxml", 1280, 720); }
    @FXML private void irTransferencias() { navegar(lblUsuarioMenu, "Transferencias.fxml", 1280, 720); }
    @FXML private void irTarjetas() { navegar(lblUsuarioMenu, "Tarjetas.fxml", 1280, 720); }
    @FXML private void cerrarSesion() { navegar(lblUsuarioMenu, "Login.fxml", 900, 600); }
}