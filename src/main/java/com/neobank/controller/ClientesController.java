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

    // Menú
    @FXML private Label lblUsuarioMenu;

    // Tabla
    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, Long>   colId;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colDni;
    @FXML private TableColumn<Cliente, String> colTelefono;
    @FXML private TableColumn<Cliente, String> colCorreo;
    @FXML private TableColumn<Cliente, String> colDireccion;
    @FXML private TableColumn<Cliente, String> colEstado;

    // Búsqueda
    @FXML private TextField txtBuscar;
    @FXML private Label lblMensaje;

    // Formulario
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
    private ObservableList<Cliente> listaClientes;

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

        // CORRECCIÓN VITAL: Convertir el Enum a String para que JavaFX no oculte la fila
        colEstado.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getEstado() != null ? cellData.getValue().getEstado().name() : ""
        ));

        // Color verde/rojo según estado
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
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
        listaClientes = FXCollections.observableArrayList(clientes);
        tablaClientes.setItems(listaClientes);
    }

    @FXML
    private void buscarCliente() {
        String texto = txtBuscar.getText().trim();
        if (texto.isEmpty()) {
            cargarClientes();
            return;
        }
        List<Cliente> resultado = clienteService.buscarPorNombre(texto);
        tablaClientes.setItems(FXCollections.observableArrayList(resultado));
    }

    @FXML
    private void mostrarTodos() {
        txtBuscar.clear();
        cargarClientes();
    }

    @FXML
    private void nuevoCliente() {
        clienteEditando = null;
        limpiarFormulario();
        lblTituloForm.setText("➕ Nuevo Cliente");
        mostrarFormulario(true);
    }

    @FXML
    private void editarCliente() {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarMensaje("⚠ Selecciona un cliente", false);
            return;
        }
        clienteEditando = seleccionado;
        lblTituloForm.setText("✏ Editar Cliente");
        txtNombre.setText(seleccionado.getNombre());
        txtDni.setText(seleccionado.getDni());
        txtTelefono.setText(seleccionado.getTelefono());
        txtCorreo.setText(seleccionado.getCorreo());
        txtDireccion.setText(seleccionado.getDireccion());
        mostrarFormulario(true);
    }

    @FXML
    private void eliminarCliente() {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarMensaje("⚠ Selecciona un cliente", false);
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Eliminar cliente?");
        alert.setContentText("Se eliminará: " + seleccionado.getNombre());
        alert.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                clienteService.eliminar(seleccionado.getId());
                cargarClientes();
                mostrarMensaje("✔ Cliente eliminado correctamente", true);
            }
        });
    }

    @FXML
    private void guardarCliente() {
        if (txtNombre.getText().trim().isEmpty() || txtDni.getText().trim().isEmpty()) {
            lblErrorForm.setText("⚠ Nombre y DNI son obligatorios");
            return;
        }

        try {
            if (clienteEditando == null) {
                Cliente nuevo = new Cliente();
                nuevo.setNombre(txtNombre.getText().trim());
                nuevo.setDni(txtDni.getText().trim());
                nuevo.setTelefono(txtTelefono.getText().trim());
                nuevo.setCorreo(txtCorreo.getText().trim());
                nuevo.setDireccion(txtDireccion.getText().trim());
                nuevo.setEstado(EstadoCliente.ACTIVO);
                clienteService.guardar(nuevo);
                mostrarMensaje("✔ Cliente registrado correctamente", true);
            } else {
                clienteEditando.setNombre(txtNombre.getText().trim());
                clienteEditando.setDni(txtDni.getText().trim());
                clienteEditando.setTelefono(txtTelefono.getText().trim());
                clienteEditando.setCorreo(txtCorreo.getText().trim());
                clienteEditando.setDireccion(txtDireccion.getText().trim());
                clienteService.actualizar(clienteEditando);
                mostrarMensaje("✔ Cliente actualizado correctamente", true);
            }
            cargarClientes();
            mostrarFormulario(false);
            limpiarFormulario();

        } catch (IllegalArgumentException e) {
            lblErrorForm.setText("⚠ " + e.getMessage());
        }
    }

    @FXML
    private void cancelarForm() {
        mostrarFormulario(false);
        limpiarFormulario();
    }

    private void mostrarFormulario(boolean visible) {
        panelFormulario.setVisible(visible);
        panelFormulario.setManaged(visible);
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        txtDni.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        txtDireccion.clear();
        lblErrorForm.setText("");
        clienteEditando = null;
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

            if (ctrl instanceof DashboardController dc) {
                dc.setUsuario(usuarioActual);
            } else if (ctrl instanceof ClientesController cc) {
                cc.setUsuario(usuarioActual);
            }

            Stage stage = (Stage) lblUsuarioMenu.getScene().getWindow();
            stage.setScene(new Scene(root, w, h));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void irDashboard() { navegar("Dashboard.fxml", 1280, 720); }
    @FXML private void irClientes() {}
    @FXML private void irCuentas() {}
    @FXML private void irMovimientos() {}
    @FXML private void irTransferencias() {}
    @FXML private void irTarjetas() {}

    @FXML
    private void cerrarSesion() {
        navegar("Login.fxml", 900, 600);
    }
}