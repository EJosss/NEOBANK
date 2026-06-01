package com.neobank.controller;

import com.neobank.ApplicationContextProvider;
import com.neobank.model.Cliente;
import com.neobank.model.Usuario;
import com.neobank.model.enums.EstadoCliente;
import com.neobank.service.ClienteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.File;
import java.util.List;

public class ClientesController {

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

    private ClienteService clienteService;
    private Usuario usuarioActual;
    private Cliente clienteEditando = null;

    @FXML
    public void initialize() {
        clienteService = ApplicationContextProvider.getBean(ClienteService.class);
        configurarTabla();
        cargarClientes();
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        if (lblUsuarioMenu != null && usuario != null) {
            lblUsuarioMenu.setText(usuario.getNombre() + " | " + usuario.getRol());
        }
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
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item);
                    setStyle(item.equals("ACTIVO")
                            ? "-fx-text-fill: #66bb6a; -fx-font-weight: bold;"
                            : "-fx-text-fill: #ef5350; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void cargarClientes() {
        List<Cliente> clientes = clienteService.listarTodos();
        ObservableList<Cliente> listaClientes = FXCollections.observableArrayList(clientes);
        tablaClientes.setItems(listaClientes);
    }

    @FXML private void buscarCliente() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) { cargarClientes(); return; }
        List<Cliente> resultado = clienteService.buscarPorNombre(texto);
        tablaClientes.setItems(FXCollections.observableArrayList(resultado));
    }

    @FXML private void mostrarTodos() { txtBuscar.clear(); cargarClientes(); }

    @FXML private void nuevoCliente() {
        clienteEditando = null; limpiarFormulario();
        lblTituloForm.setText("➕ Nuevo Cliente"); mostrarFormulario(true);
    }

    @FXML private void editarCliente() {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) { mostrarMensaje("⚠ Selecciona un cliente primero", false); return; }
        clienteEditando = seleccionado;
        lblTituloForm.setText("✏ Editar Cliente");
        txtNombre.setText(seleccionado.getNombre());
        txtDni.setText(seleccionado.getDni());
        txtTelefono.setText(seleccionado.getTelefono());
        txtCorreo.setText(seleccionado.getCorreo());
        txtDireccion.setText(seleccionado.getDireccion());
        mostrarFormulario(true);
    }

    @FXML private void eliminarCliente() {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) { mostrarMensaje("⚠ Selecciona un cliente", false); return; }
        clienteService.eliminar(seleccionado.getId());
        cargarClientes();
        mostrarMensaje("✔ Cliente eliminado correctamente", true);
    }

    // 🚀 AQUÍ ESTÁN LAS NUEVAS VALIDACIONES BLINDADAS
    @FXML private void guardarCliente() {
        String nombre = txtNombre.getText().trim();
        String dni = txtDni.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String correo = txtCorreo.getText().trim();
        String direccion = txtDireccion.getText().trim();

        // VALIDACIÓN 1: Ningún campo vacío
        if (nombre.isEmpty() || dni.isEmpty() || telefono.isEmpty() || correo.isEmpty() || direccion.isEmpty()) {
            lblErrorForm.setText("⚠ Todos los campos son obligatorios.");
            return;
        }

        // VALIDACIÓN 2: DNI de 8 números exactos
        if (!dni.matches("\\d{8}")) {
            lblErrorForm.setText("⚠ El DNI debe tener exactamente 8 números.");
            return;
        }

        // VALIDACIÓN 3: Teléfono de 9 números exactos
        if (!telefono.matches("\\d{9}")) {
            lblErrorForm.setText("⚠ El teléfono debe tener exactamente 9 números.");
            return;
        }

        // VALIDACIÓN 4: Formato de correo válido
        if (!correo.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            lblErrorForm.setText("⚠ Ingresa un correo electrónico válido.");
            return;
        }

        // VALIDACIÓN 5: DNI Único (que no se repita en la base de datos)
        boolean dniExiste = clienteService.listarTodos().stream()
                .anyMatch(c -> c.getDni().equals(dni) && (clienteEditando == null || !c.getId().equals(clienteEditando.getId())));

        if (dniExiste) {
            lblErrorForm.setText("⚠ Este DNI ya está registrado en el banco.");
            return;
        }

        try {
            if (clienteEditando == null) {
                Cliente nuevo = new Cliente();
                nuevo.setNombre(nombre); nuevo.setDni(dni);
                nuevo.setTelefono(telefono); nuevo.setCorreo(correo);
                nuevo.setDireccion(direccion); nuevo.setEstado(EstadoCliente.ACTIVO);
                clienteService.guardar(nuevo);
                mostrarMensaje("✔ Cliente registrado con éxito", true);
            } else {
                clienteEditando.setNombre(nombre); clienteEditando.setDni(dni);
                clienteEditando.setTelefono(telefono); clienteEditando.setCorreo(correo);
                clienteEditando.setDireccion(direccion);
                clienteService.actualizar(clienteEditando);
                mostrarMensaje("✔ Cliente actualizado", true);
            }
            cargarClientes(); mostrarFormulario(false); limpiarFormulario();
        } catch (IllegalArgumentException e) { lblErrorForm.setText("⚠ " + e.getMessage()); }
    }

    @FXML private void cancelarForm() { mostrarFormulario(false); limpiarFormulario(); }
    private void mostrarFormulario(boolean visible) { panelFormulario.setVisible(visible); panelFormulario.setManaged(visible); }
    private void limpiarFormulario() { txtNombre.clear(); txtDni.clear(); txtTelefono.clear(); txtCorreo.clear(); txtDireccion.clear(); lblErrorForm.setText(""); clienteEditando = null; }
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
    @FXML private void irClientes() { }
    @FXML private void irCuentas() { navegar("Cuentas.fxml", 1280, 720); }
    @FXML private void irMovimientos() { navegar("Movimientos.fxml", 1280, 720); }
    @FXML private void irTransferencias() { navegar("Transferencias.fxml", 1280, 720); }
    @FXML private void irTarjetas() { navegar("Tarjetas.fxml", 1280, 720); }
    @FXML private void cerrarSesion() { navegar("Login.fxml", 900, 600); }
}