package com.neobank.controller;

import com.neobank.ApplicationContextProvider;
import com.neobank.model.Cliente;
import com.neobank.model.CuentaBancaria;
import com.neobank.model.Usuario;
import com.neobank.model.enums.EstadoCuenta;
import com.neobank.model.enums.TipoCuenta;
import com.neobank.service.ClienteService;
import com.neobank.service.CuentaService;
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
import javafx.util.StringConverter;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

public class CuentasController {

    @FXML private Label lblUsuarioMenu;
    @FXML private TableView<CuentaBancaria> tablaCuentas;
    @FXML private TableColumn<CuentaBancaria, Long> colId;
    @FXML private TableColumn<CuentaBancaria, String> colNumero;
    @FXML private TableColumn<CuentaBancaria, String> colTipo;
    @FXML private TableColumn<CuentaBancaria, BigDecimal> colSaldo;
    @FXML private TableColumn<CuentaBancaria, String> colEstado;

    @FXML private VBox panelFormulario;
    @FXML private ComboBox<Cliente> cbCliente;
    @FXML private ComboBox<TipoCuenta> cbTipoCuenta;
    @FXML private TextField txtSaldoInicial;
    @FXML private Label lblMensaje;

    private CuentaService cuentaService;
    private ClienteService clienteService;
    private Usuario usuarioActual;

    @FXML
    public void initialize() {
        cuentaService = ApplicationContextProvider.getBean(CuentaService.class);
        clienteService = ApplicationContextProvider.getBean(ClienteService.class);

        configurarTabla();
        cargarDatos();
        configurarComboBoxes();
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        if (lblUsuarioMenu != null && usuario != null) {
            lblUsuarioMenu.setText(usuario.getNombre() + " | " + usuario.getRol());
        }
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroCuenta"));
        colSaldo.setCellValueFactory(new PropertyValueFactory<>("saldo"));

        // Evitamos el error de Enum en JavaFX convirtiéndolo a String
        colTipo.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getTipoCuenta() != null ? cell.getValue().getTipoCuenta().name() : ""
        ));
        colEstado.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getEstado() != null ? cell.getValue().getEstado().name() : ""
        ));
    }

    private void cargarDatos() {
        List<CuentaBancaria> cuentas = cuentaService.listarTodos();
        tablaCuentas.setItems(FXCollections.observableArrayList(cuentas));
    }

    private void configurarComboBoxes() {
        // Cargar Tipos de Cuenta
        cbTipoCuenta.setItems(FXCollections.observableArrayList(TipoCuenta.values()));

        // Cargar Clientes en el desplegable y mostrar su nombre (no el código de memoria)
        List<Cliente> clientes = clienteService.listarTodos();
        cbCliente.setItems(FXCollections.observableArrayList(clientes));
        cbCliente.setConverter(new StringConverter<Cliente>() {
            @Override
            public String toString(Cliente cliente) {
                return cliente != null ? cliente.getNombre() + " (DNI: " + cliente.getDni() + ")" : "";
            }
            @Override
            public Cliente fromString(String string) { return null; }
        });
    }

    @FXML
    private void nuevaCuenta() {
        cbCliente.getSelectionModel().clearSelection();
        cbTipoCuenta.getSelectionModel().clearSelection();
        txtSaldoInicial.clear();
        panelFormulario.setVisible(true);
        panelFormulario.setManaged(true);
    }

    @FXML
    private void guardarCuenta() {
        Cliente clienteSel = cbCliente.getValue();
        TipoCuenta tipoSel = cbTipoCuenta.getValue();

        if (clienteSel == null || tipoSel == null || txtSaldoInicial.getText().isEmpty()) {
            lblMensaje.setText("⚠ Todos los campos son obligatorios.");
            lblMensaje.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            BigDecimal saldo = new BigDecimal(txtSaldoInicial.getText());

            CuentaBancaria nuevaCuenta = new CuentaBancaria();
            // Generar número de cuenta aleatorio tipo "CTA-102938"
            nuevaCuenta.setNumeroCuenta("CTA-" + (100000 + new Random().nextInt(900000)));
            // Si tu modelo usa Objeto Cliente o Long ID, esto asume que usas un Objeto o campo id_cliente
            // Si te da error aquí, cambiamos 'setIdCliente' según como esté en tu modelo CuentaBancaria.java
            nuevaCuenta.setIdCliente(clienteSel.getId());
            nuevaCuenta.setTipoCuenta(tipoSel);
            nuevaCuenta.setSaldo(saldo);
            nuevaCuenta.setEstado(EstadoCuenta.ACTIVA);

            cuentaService.guardar(nuevaCuenta);

            lblMensaje.setText("✔ Cuenta creada con éxito.");
            lblMensaje.setStyle("-fx-text-fill: green;");

            panelFormulario.setVisible(false);
            panelFormulario.setManaged(false);
            cargarDatos();

        } catch (Exception e) {
            lblMensaje.setText("⚠ Error en el saldo. Ingresa un número válido.");
            lblMensaje.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void cancelarForm() {
        panelFormulario.setVisible(false);
        panelFormulario.setManaged(false);
    }

    // ── NAVEGACIÓN (Igual que en Clientes) ──
    private void navegar(String fxml) {
        try {
            String ruta = System.getProperty("user.dir") + "/src/main/resources/fxml/" + fxml;
            FXMLLoader loader = new FXMLLoader(new File(ruta).toURI().toURL());
            Parent root = loader.load();
            Object ctrl = loader.getController();

            if (ctrl instanceof DashboardController dc) dc.setUsuario(usuarioActual);
            else if (ctrl instanceof ClientesController cc) cc.setUsuario(usuarioActual);

            Stage stage = (Stage) lblUsuarioMenu.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 720));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void irDashboard() { navegar("Dashboard.fxml"); }
    @FXML private void irClientes() { navegar("Clientes.fxml"); }
    @FXML private void cerrarSesion() { navegar("Login.fxml"); }
}