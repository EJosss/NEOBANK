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
import java.text.DecimalFormat;
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

    // Listas para la gestión centralizada
    private ObservableList<CuentaBancaria> listaCuentasMaster = FXCollections.observableArrayList();
    private FilteredList<CuentaBancaria> listaFiltrada;

    // 🚀 Lista dedicada para el autocompletado del ComboBox
    private ObservableList<Cliente> listaClientesCombo = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cuentaService = ApplicationContextProvider.getBean(CuentaService.class);
        clienteService = ApplicationContextProvider.getBean(ClienteService.class);

        configurarTabla();
        configurarBusquedaEnTiempoReal();
        cargarDatos();
        configurarMenuContextual();
        configurarLimpiezaAutomatica(lblErrorForm, cmbClientes, cmbTipo);
        configurarAutocompletadoClientes(); // 🚀 Inicializa el buscador por DNI
    }

    // 🚀 NUEVO MÉTODO: Lógica del buscador por DNI o Nombre
    private void configurarAutocompletadoClientes() {
        // 1. Enseñamos al ComboBox cómo mostrar al cliente (DNI - Nombre)
        cmbClientes.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Cliente c) {
                return c == null ? "" : c.getDni() + " - " + c.getNombre();
            }

            @Override
            public Cliente fromString(String string) {
                return listaClientesCombo.stream()
                        .filter(c -> toString(c).equals(string)).findFirst().orElse(null);
            }
        });

        // 2. Volvemos editable la cajita
        cmbClientes.setEditable(true);

        // 3. Agregamos el motor de búsqueda en tiempo real
        cmbClientes.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;

            // Si la cajita está limpia, mostramos todos
            if (newValue.trim().isEmpty()) {
                cmbClientes.setItems(listaClientesCombo);
                cmbClientes.hide();
            } else {
                ObservableList<Cliente> filtrados = FXCollections.observableArrayList();
                for (Cliente c : listaClientesCombo) {
                    // 🚀 BÚSQUEDA MÁGICA: Por DNI exacto o por Nombre en minúsculas
                    if ((c.getDni() != null && c.getDni().contains(newValue)) ||
                            (c.getNombre() != null && c.getNombre().toLowerCase().contains(newValue.toLowerCase()))) {
                        filtrados.add(c);
                    }
                }

                // Evita problemas gráficos con JavaFX usando Platform.runLater
                javafx.application.Platform.runLater(() -> {
                    cmbClientes.setItems(filtrados);
                    cmbClientes.getEditor().setText(newValue);
                    cmbClientes.getEditor().positionCaret(newValue.length());
                    if (!filtrados.isEmpty()) {
                        cmbClientes.show();
                    } else {
                        cmbClientes.hide();
                    }
                });
            }
        });
    }

    private void configurarBusquedaEnTiempoReal() {
        listaFiltrada = new FilteredList<>(listaCuentasMaster, cuenta -> cuenta.getEstado() == EstadoCuenta.ACTIVA);

        Runnable actualizarFiltro = () -> {
            String textoBusqueda = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase().trim() : "";
            boolean mostrarCerradas = chkVerCerradas.isSelected();

            listaFiltrada.setPredicate(cuenta -> {
                EstadoCuenta estadoEsperado = mostrarCerradas ? EstadoCuenta.CERRADA : EstadoCuenta.ACTIVA;
                if (cuenta.getEstado() != estadoEsperado) return false;
                if (textoBusqueda.isEmpty()) return true;

                if (cuenta.getNumeroCuenta() != null && cuenta.getNumeroCuenta().toLowerCase().contains(textoBusqueda)) {
                    return true;
                }

                if (cuenta.getIdCliente() != null) {
                    for (Cliente c : clienteService.listarTodos()) {
                        if (c.getId().equals(cuenta.getIdCliente())) {
                            String dniCliente = c.getDni() != null ? c.getDni().toLowerCase().trim() : "";
                            String nombreCliente = c.getNombre() != null ? c.getNombre().toLowerCase().trim() : "";
                            return dniCliente.contains(textoBusqueda) || nombreCliente.contains(textoBusqueda);
                        }
                    }
                }
                return false;
            });
        };

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

        // Reemplaza la configuración de colSaldo por esto:
        colSaldo.setCellValueFactory(new PropertyValueFactory<>("saldo"));
        colSaldo.setCellFactory(column -> new TableCell<CuentaBancaria, BigDecimal>() {
            private final DecimalFormat format = new DecimalFormat("S/ #,##0.00");

            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(format.format(item));
                }
            }
        });
    }

    private void cargarDatos() {
        List<CuentaBancaria> cuentas = cuentaService.listarTodas();
        listaCuentasMaster.setAll(cuentas);
    }

    @FXML private void mostrarTodas() { txtBuscar.clear(); cargarDatos(); }

    @FXML private void nuevaCuenta() {
        // 🚀 Llenamos la lista del autocompletado
        listaClientesCombo.setAll(clienteService.listarTodos());
        cmbClientes.setItems(listaClientesCombo);
        cmbTipo.setItems(FXCollections.observableArrayList(TipoCuenta.values()));

        // Limpiamos los selectores
        cmbClientes.getSelectionModel().clearSelection();
        cmbClientes.getEditor().clear();
        cmbTipo.getSelectionModel().clearSelection();

        panelFormulario.setVisible(true);
        panelFormulario.setManaged(true);
        lblErrorForm.setText("");
    }

    @FXML private void guardarCuenta() {
        Cliente cliente = cmbClientes.getValue();

        // 🚀 Si escribieron el nombre/DNI y le dieron a guardar sin hacer clic en la lista:
        if (cliente == null && !cmbClientes.getEditor().getText().isEmpty()) {
            cliente = cmbClientes.getConverter().fromString(cmbClientes.getEditor().getText());
        }

        if (cliente == null || cmbTipo.getValue() == null) {
            lblErrorForm.setText("⚠ Selecciona un Cliente válido de la lista y Tipo de Cuenta.");
            return;
        }

        TipoCuenta tipo = cmbTipo.getValue();
        String numero = "4532-" + (1000 + new Random().nextInt(9000)) + "-" + (1000 + new Random().nextInt(9000));

        CuentaBancaria nuevaCuenta = new CuentaBancaria();
        nuevaCuenta.setNumeroCuenta(numero);
        nuevaCuenta.setIdCliente(cliente.getId());
        nuevaCuenta.setTipoCuenta(tipo);
        nuevaCuenta.setSaldo(BigDecimal.ZERO);
        nuevaCuenta.setEstado(EstadoCuenta.ACTIVA);

        cuentaService.guardar(nuevaCuenta);
        cargarDatos();
        mostrarFormulario(false);

        // 🚀 REEMPLAZO POR MENSAJE NO BLOQUEANTE
        mostrarMensajeUniversal(lblMensaje, "✔ Cuenta '" + numero + "' vinculada a " + cliente.getNombre(), true);
    }

    @FXML private void eliminarCuenta() {
        CuentaBancaria sel = tablaCuentas.getSelectionModel().getSelectedItem();
        if (sel != null) {
            try {
                cuentaService.eliminar(sel.getId(), usuarioActual);
                cargarDatos();
                // 🚀 REEMPLAZO POR MENSAJE NO BLOQUEANTE
                mostrarMensajeUniversal(lblMensaje, "✔ Cuenta '" + sel.getNumeroCuenta() + "' CERRADA correctamente.", true);
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