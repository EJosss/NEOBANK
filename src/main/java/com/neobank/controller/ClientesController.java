package com.neobank.controller;

import com.neobank.ApplicationContextProvider;
import com.neobank.model.Cliente;
import com.neobank.model.enums.EstadoCliente;
import com.neobank.service.ClienteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.util.List;

public class ClientesController extends BaseController {

    @FXML private Label lblUsuarioMenu;
    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, Long> colId;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colDni;
    @FXML private TableColumn<Cliente, String> colTelefono;
    @FXML private TableColumn<Cliente, String> colCorreo;
    @FXML private TableColumn<Cliente, String> colDireccion;
    @FXML private TableColumn<Cliente, String> colEstado;
    @FXML private TextField txtBuscar;
    @FXML private Label lblMensaje;
    @FXML private VBox panelFormulario;
    @FXML private Label lblTituloForm;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDni;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtCorreo;
    @FXML private TextField txtDireccion;
    @FXML private Label lblErrorForm;

    @FXML private Button btnEliminar;
    @FXML private Button btnMenuDashboard;
    @FXML private CheckBox chkVerInactivos;

    private ClienteService clienteService;
    private Cliente clienteEditando = null;

    // Estilos corporativos en constante
    private final String ESTILO_NORMAL = "-fx-background-color: #FFFFFF; -fx-border-color: #CCCCCC; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8; -fx-font-size: 14px; -fx-text-fill: #1A1A1A;";
    private final String ESTILO_ERROR = "-fx-background-color: #FFFFFF; -fx-border-color: #E53935; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8; -fx-font-size: 14px; -fx-text-fill: #1A1A1A;";

    // Lista observable para gestionar el filtrado dinámico
    private ObservableList<Cliente> listaClientesMaster = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        clienteService = ApplicationContextProvider.getBean(ClienteService.class);
        configurarTabla();
        cargarClientes();
        configurarBusquedaEnTiempoReal(); // 🚀 Filtro dinámico activado
        configurarValidacionesEnTiempoReal();
        configurarLimpiezaAutomatica(lblErrorForm, txtCorreo, txtDireccion);
    }

    private void configurarBusquedaEnTiempoReal() {
        FilteredList<Cliente> listaFiltrada = new FilteredList<>(listaClientesMaster, cliente -> cliente.getEstado() == EstadoCliente.ACTIVO);

        Runnable actualizarFiltro = () -> {
            String textoBusqueda = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase().trim() : "";
            boolean mostrarInactivos = chkVerInactivos.isSelected();

            listaFiltrada.setPredicate(cliente -> {
                // 1. Condición de Estado Dinámica
                EstadoCliente estadoEsperado = mostrarInactivos ? EstadoCliente.INACTIVO : EstadoCliente.ACTIVO;
                if (cliente.getEstado() != estadoEsperado) {
                    return false;
                }

                // 2. Condición de Texto (Nombre o DNI)
                if (textoBusqueda.isEmpty()) {
                    return true;
                }

                String filter = textoBusqueda.toLowerCase();
                if (cliente.getNombre() != null && cliente.getNombre().toLowerCase().contains(filter)) {
                    return true;
                } else if (cliente.getDni() != null && cliente.getDni().toLowerCase().contains(filter)) {
                    return true;
                }
                return false;
            });
        };

        txtBuscar.textProperty().addListener((observable, oldValue, newValue) -> actualizarFiltro.run());
        chkVerInactivos.selectedProperty().addListener((observable, oldValue, newValue) -> actualizarFiltro.run());

        tablaClientes.setItems(listaFiltrada);
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

    private void configurarValidacionesEnTiempoReal() {
        txtNombre.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (!newValue.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]*$")) {
                    txtNombre.setStyle(ESTILO_ERROR);
                } else {
                    txtNombre.setStyle(ESTILO_NORMAL);
                }
            } else {
                txtNombre.setStyle(ESTILO_NORMAL);
            }
        });

        txtDni.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                if (!newValue.matches("^[0-9]*$") || newValue.length() > 8) {
                    txtDni.setStyle(ESTILO_ERROR);
                } else {
                    txtDni.setStyle(ESTILO_NORMAL);
                }
            } else {
                txtDni.setStyle(ESTILO_NORMAL);
            }
        });

        txtTelefono.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                // 🚀 NUEVA VALIDACIÓN: Solo acepta números que empiecen con 9 (Máximo 9 dígitos)
                if (!newValue.matches("^9[0-9]{0,8}$")) {
                    txtTelefono.setStyle(ESTILO_ERROR);
                } else {
                    txtTelefono.setStyle(ESTILO_NORMAL);
                }
            } else {
                txtTelefono.setStyle(ESTILO_NORMAL);
            }
        });
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));

        colEstado.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getEstado() != null ? cellData.getValue().getEstado().name() : ""
        ));

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item);
                    setStyle(item.equals("ACTIVO")
                            ? "-fx-text-fill: #43A047; -fx-font-weight: bold;"
                            : "-fx-text-fill: #E53935; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void cargarClientes() {
        List<Cliente> clientes = clienteService.listarTodos();
        listaClientesMaster.setAll(clientes);
        if (txtBuscar.getText().trim().isEmpty()) {
            tablaClientes.setItems(listaClientesMaster);
        }
    }

    @FXML private void buscarCliente() {
        if (txtBuscar.getText().trim().isEmpty()) { cargarClientes(); }
    }

    @FXML private void mostrarTodos() {
        txtBuscar.clear();
    }

    @FXML private void nuevoCliente() {
        clienteEditando = null; limpiarFormulario();
        restablecerEstilosCampos();
        lblTituloForm.setText("➕ Nuevo Cliente"); mostrarFormulario(true);
    }

    @FXML private void editarCliente() {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) { mostrarNotificacionError("Selección Requerida", "Debe seleccionar un cliente de la tabla para editarlo."); return; }
        clienteEditando = seleccionado;
        lblTituloForm.setText("✏ Editar Cliente");
        txtNombre.setText(seleccionado.getNombre()); txtDni.setText(seleccionado.getDni());
        txtTelefono.setText(seleccionado.getTelefono()); txtCorreo.setText(seleccionado.getCorreo());
        txtDireccion.setText(seleccionado.getDireccion());
        restablecerEstilosCampos();
        mostrarFormulario(true);
    }

    @FXML private void eliminarCliente() {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) { mostrarNotificacionError("Selección Requerida", "Debe seleccionar un cliente de la tabla para eliminarlo."); return; }

        try {
            clienteService.eliminar(seleccionado.getId(), usuarioActual);
            cargarClientes();
            // 🚀 REEMPLAZO POR MENSAJE NO BLOQUEANTE
            mostrarMensajeUniversal(lblMensaje, "✔ El cliente '" + seleccionado.getNombre() + "' fue desactivado.", true);
        } catch (IllegalArgumentException e) {
            mostrarNotificacionError("Restricción de Acceso", e.getMessage());
        }
    }

    @FXML private void guardarCliente() {
        String nombre = txtNombre.getText().trim(); String dni = txtDni.getText().trim();
        String telefono = txtTelefono.getText().trim(); String correo = txtCorreo.getText().trim();
        String direccion = txtDireccion.getText().trim();

        limpiarBordes(txtCorreo, txtDireccion);
        boolean hayError = false;

        if (nombre.isEmpty()) { txtNombre.setStyle(ESTILO_ERROR); hayError = true; }
        if (dni.isEmpty()) { txtDni.setStyle(ESTILO_ERROR); hayError = true; }
        if (telefono.isEmpty()) { txtTelefono.setStyle(ESTILO_ERROR); hayError = true; }
        if (correo.isEmpty()) { marcarCampoErroneo(txtCorreo); hayError = true; }
        if (direccion.isEmpty()) { marcarCampoErroneo(txtDireccion); hayError = true; }

        if (hayError) {
            lblErrorForm.setText("⚠ Completa los campos en rojo.");
            return;
        }

        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            txtNombre.setStyle(ESTILO_ERROR);
            mostrarNotificacionError("Formato de Nombre", "El campo nombre no puede contener números ni caracteres especiales.");
            return;
        }

        if (!dni.matches("^[0-9]{8}$")) {
            txtDni.setStyle(ESTILO_ERROR);
            mostrarNotificacionError("Formato de DNI", "El DNI es incorrecto. Debe tener exactamente 8 caracteres numéricos.");
            return;
        }

        // 🚀 NUEVA VALIDACIÓN AL GUARDAR
        if (!telefono.matches("^9[0-9]{8}$")) {
            txtTelefono.setStyle(ESTILO_ERROR);
            mostrarNotificacionError("Formato de Teléfono", "El número de celular en Perú debe empezar con 9 y tener exactamente 9 dígitos.");
            return;
        }

        try {
            if (clienteEditando == null) {
                Cliente nuevo = Cliente.builder().nombre(nombre).dni(dni).telefono(telefono)
                        .correo(correo).direccion(direccion).estado(EstadoCliente.ACTIVO).build();
                clienteService.guardar(nuevo);
                // 🚀 REEMPLAZO POR MENSAJE NO BLOQUEANTE
                mostrarMensajeUniversal(lblMensaje, "✔ Cliente registrado con éxito.", true);
            } else {
                clienteEditando.setNombre(nombre); clienteEditando.setDni(dni);
                clienteEditando.setTelefono(telefono); clienteEditando.setCorreo(correo);
                clienteEditando.setDireccion(direccion);
                clienteService.actualizar(clienteEditando);
                // 🚀 REEMPLAZO POR MENSAJE NO BLOQUEANTE
                mostrarMensajeUniversal(lblMensaje, "✔ Cliente actualizado con éxito.", true);
            }
            cargarClientes(); mostrarFormulario(false); limpiarFormulario();

        } catch (IllegalArgumentException e) {
            mostrarNotificacionError("Error de Validación", e.getMessage());
        } catch (jakarta.validation.ConstraintViolationException e) {
            mostrarNotificacionError("Error de Restricción", e.getConstraintViolations().iterator().next().getMessage());
        }
    }

    @FXML private void cancelarForm() { mostrarFormulario(false); limpiarFormulario(); }
    private void mostrarFormulario(boolean visible) { panelFormulario.setVisible(visible); panelFormulario.setManaged(visible); }

    private void limpiarFormulario() {
        txtNombre.clear(); txtDni.clear(); txtTelefono.clear(); txtCorreo.clear(); txtDireccion.clear();
        lblErrorForm.setText(""); clienteEditando = null;
    }

    private void restablecerEstilosCampos() {
        txtNombre.setStyle(ESTILO_NORMAL);
        txtDni.setStyle(ESTILO_NORMAL);
        txtTelefono.setStyle(ESTILO_NORMAL);
        limpiarBordes(txtCorreo, txtDireccion);
    }

    @FXML private void irDashboard() { navegar(lblUsuarioMenu, "Dashboard.fxml", 1280, 720); }
    @FXML private void irClientes() { }
    @FXML private void irCuentas() { navegar(lblUsuarioMenu, "Cuentas.fxml", 1280, 720); }
    @FXML private void irMovimientos() { navegar(lblUsuarioMenu, "Movimientos.fxml", 1280, 720); }
    @FXML private void irTransferencias() { navegar(lblUsuarioMenu, "Transferencias.fxml", 1280, 720); }
    @FXML private void irTarjetas() { navegar(lblUsuarioMenu, "Tarjetas.fxml", 1280, 720); }
    @FXML private void cerrarSesion() { navegar(lblUsuarioMenu, "Login.fxml", 900, 600); }
}