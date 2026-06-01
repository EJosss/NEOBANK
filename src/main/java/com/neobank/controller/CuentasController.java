package com.neobank.controller;

import com.neobank.ApplicationContextProvider;
import com.neobank.model.*;
import com.neobank.model.enums.*;
import com.neobank.service.ClienteService;
import com.neobank.service.CuentaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

public class CuentasController {

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

    private Usuario usuarioActual;
    private CuentaService cuentaService;
    private ClienteService clienteService;

    @FXML
    public void initialize() {
        cuentaService = ApplicationContextProvider.getBean(CuentaService.class);
        clienteService = ApplicationContextProvider.getBean(ClienteService.class);
        configurarTabla();
        cargarDatos();
        configurarMenuContextual();
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        if (lblUsuarioMenu != null && usuario != null) {
            lblUsuarioMenu.setText(usuario.getNombre() + " | " + usuario.getRol());
        }
    }

    // 🚀 AQUÍ CREAMOS EL CLIC DERECHO PARA COPIAR EL NÚMERO
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
                mostrarMensaje("✔ Número copiado: " + cuenta.getNumeroCuenta(), true);
            } else {
                mostrarMensaje("⚠ Selecciona una cuenta primero", false);
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
        tablaCuentas.setItems(FXCollections.observableArrayList(cuentas));
    }

    @FXML private void mostrarTodas() { cargarDatos(); }

    @FXML private void nuevaCuenta() {
        cmbClientes.setItems(FXCollections.observableArrayList(clienteService.listarTodos()));
        cmbTipo.setItems(FXCollections.observableArrayList(TipoCuenta.values()));
        cmbClientes.getSelectionModel().clearSelection(); cmbTipo.getSelectionModel().clearSelection();
        lblErrorForm.setText(""); mostrarFormulario(true);
    }

    @FXML private void guardarCuenta() {
        Cliente cliente = cmbClientes.getValue(); TipoCuenta tipo = cmbTipo.getValue();
        if (cliente == null || tipo == null) { lblErrorForm.setText("⚠ Selecciona Cliente y Tipo"); return; }

        String numero = "4532-" + (1000 + new Random().nextInt(9000)) + "-" + (1000 + new Random().nextInt(9000));
        CuentaBancaria nuevaCuenta = new CuentaBancaria();
        nuevaCuenta.setNumeroCuenta(numero); nuevaCuenta.setIdCliente(cliente.getId());
        nuevaCuenta.setTipoCuenta(tipo); nuevaCuenta.setSaldo(BigDecimal.ZERO);
        nuevaCuenta.setEstado(EstadoCuenta.ACTIVA);

        cuentaService.guardar(nuevaCuenta); cargarDatos(); mostrarFormulario(false);
        mostrarMensaje("✔ Cuenta " + numero + " creada", true);
    }

    @FXML private void eliminarCuenta() {
        CuentaBancaria sel = tablaCuentas.getSelectionModel().getSelectedItem();
        if (sel != null) {
            cuentaService.eliminar(sel.getId()); cargarDatos(); mostrarMensaje("✔ Cuenta eliminada", true);
        }
    }

    @FXML private void cancelarForm() { mostrarFormulario(false); }
    private void mostrarFormulario(boolean visible) { panelFormulario.setVisible(visible); panelFormulario.setManaged(visible); }
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

            Stage stage = (Stage) lblUsuarioMenu.getScene().getWindow();
            stage.setScene(new Scene(root, w, h));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void irDashboard() { navegar("Dashboard.fxml", 1280, 720); }
    @FXML private void irClientes() { navegar("Clientes.fxml", 1280, 720); }
    @FXML private void irCuentas() { }
    @FXML private void irMovimientos() { navegar("Movimientos.fxml", 1280, 720); }
    @FXML private void irTransferencias() { navegar("Transferencias.fxml", 1280, 720); }
    @FXML private void irTarjetas() { }
    @FXML private void cerrarSesion() { navegar("Login.fxml", 900, 600); }
}