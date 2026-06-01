package com.neobank.controller;

import com.neobank.ApplicationContextProvider;
import com.neobank.model.CuentaBancaria;
import com.neobank.model.Tarjeta;
import com.neobank.model.Usuario;
import com.neobank.model.enums.EstadoTarjeta;
import com.neobank.model.enums.TipoTarjeta;
import com.neobank.service.CuentaService;
import com.neobank.service.TarjetaService;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

public class TarjetasController {

    @FXML private Label lblUsuarioMenu;
    @FXML private TableView<Tarjeta> tablaTarjetas;
    @FXML private TableColumn<Tarjeta, String> colNumero;
    @FXML private TableColumn<Tarjeta, String> colCuenta;
    @FXML private TableColumn<Tarjeta, String> colTipo;
    @FXML private TableColumn<Tarjeta, String> colVencimiento;
    @FXML private TableColumn<Tarjeta, String> colEstado;

    @FXML private VBox panelFormulario;
    @FXML private ComboBox<CuentaBancaria> cmbCuentas;
    @FXML private ComboBox<TipoTarjeta> cmbTipo;
    @FXML private TextField txtLimite;
    @FXML private Label lblMensaje;

    private Usuario usuarioActual;
    private TarjetaService tarjetaService;
    private CuentaService cuentaService;

    @FXML
    public void initialize() {
        tarjetaService = ApplicationContextProvider.getBean(TarjetaService.class);
        cuentaService = ApplicationContextProvider.getBean(CuentaService.class);
        configurarTabla();
        cargarDatos();
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        if (lblUsuarioMenu != null && usuario != null) {
            lblUsuarioMenu.setText(usuario.getNombre() + " | " + usuario.getRol());
        }
    }

    private void configurarTabla() {
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroTarjeta"));
        colVencimiento.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));

        colCuenta.setCellValueFactory(cell -> {
            Long idC = cell.getValue().getIdCuenta();
            if (idC != null) {
                return cuentaService.listarTodas().stream()
                        .filter(c -> c.getId().equals(idC))
                        .map(c -> new SimpleStringProperty(c.getNumeroCuenta()))
                        .findFirst().orElse(new SimpleStringProperty("Desconocida"));
            }
            return new SimpleStringProperty("Sin Cuenta");
        });

        colTipo.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getTipoTarjeta() != null ? cell.getValue().getTipoTarjeta().name() : ""
        ));

        colEstado.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getEstado() != null ? cell.getValue().getEstado().name() : ""
        ));

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item);
                    setStyle(item.equals("ACTIVA") ? "-fx-text-fill: #66bb6a; -fx-font-weight: bold;" : "-fx-text-fill: #ef5350; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void cargarDatos() {
        List<Tarjeta> tarjetas = tarjetaService.listarTodas();
        tablaTarjetas.setItems(FXCollections.observableArrayList(tarjetas));
    }

    @FXML private void nuevaTarjeta() {
        cmbCuentas.setItems(FXCollections.observableArrayList(cuentaService.listarTodas()));
        cmbCuentas.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(CuentaBancaria c) { return c != null ? c.getNumeroCuenta() : ""; }
            @Override public CuentaBancaria fromString(String string) { return null; }
        });
        cmbTipo.setItems(FXCollections.observableArrayList(TipoTarjeta.values()));
        cmbCuentas.getSelectionModel().clearSelection();
        cmbTipo.getSelectionModel().clearSelection();
        txtLimite.clear();
        panelFormulario.setVisible(true);
        panelFormulario.setManaged(true);
        lblMensaje.setText("");
    }

    // 🚀 VALIDACIONES DE TARJETAS
    @FXML private void guardarTarjeta() {
        CuentaBancaria cuenta = cmbCuentas.getValue();
        TipoTarjeta tipo = cmbTipo.getValue();

        if (cuenta == null || tipo == null) {
            mostrarMensaje("⚠ Selecciona una cuenta y el tipo de tarjeta.", false);
            return;
        }

        BigDecimal limite = BigDecimal.ZERO;

        // VALIDAR: Solo pedimos límite si es de crédito
        if (tipo == TipoTarjeta.CREDITO) {
            String limiteStr = txtLimite.getText().trim();
            if (limiteStr.isEmpty()) {
                mostrarMensaje("⚠ Ingresa un límite para la tarjeta de crédito.", false);
                return;
            }
            try {
                limite = new BigDecimal(limiteStr);
                if (limite.compareTo(BigDecimal.ZERO) <= 0) {
                    mostrarMensaje("⚠ El límite de crédito debe ser mayor a 0.", false);
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarMensaje("⚠ Límite inválido. Ingresa solo números.", false);
                return;
            }
        }

        Random rnd = new Random();
        String numero = "4532-" + (1000 + rnd.nextInt(9000)) + "-" + (1000 + rnd.nextInt(9000)) + "-" + (1000 + rnd.nextInt(9000));
        String ccv = String.format("%03d", rnd.nextInt(1000));
        String vencimiento = LocalDate.now().plusYears(5).format(DateTimeFormatter.ofPattern("MM/yy"));

        Tarjeta nueva = new Tarjeta();
        nueva.setNumeroTarjeta(numero);
        nueva.setIdCuenta(cuenta.getId());
        nueva.setTipoTarjeta(tipo);
        nueva.setLimite(limite);
        nueva.setFechaVencimiento(vencimiento);
        nueva.setCcv(ccv);
        nueva.setEstado(EstadoTarjeta.ACTIVA);

        tarjetaService.guardar(nueva);
        cargarDatos();
        panelFormulario.setVisible(false);
        panelFormulario.setManaged(false);
        mostrarMensaje("✔ Tarjeta " + numero + " emitida con éxito", true);
    }

    @FXML private void bloquearTarjeta() {
        Tarjeta sel = tablaTarjetas.getSelectionModel().getSelectedItem();
        if (sel != null && sel.getEstado() == EstadoTarjeta.ACTIVA) {
            tarjetaService.bloquear(sel.getId());
            cargarDatos();
            mostrarMensaje("✔ Tarjeta bloqueada", true);
        } else {
            mostrarMensaje("⚠ Selecciona una tarjeta activa para bloquear", false);
        }
    }

    @FXML private void cancelarForm() {
        panelFormulario.setVisible(false);
        panelFormulario.setManaged(false);
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
    @FXML private void irMovimientos() { navegar("Movimientos.fxml", 1280, 720); }
    @FXML private void irTransferencias() { navegar("Transferencias.fxml", 1280, 720); }
    @FXML private void irTarjetas() { }
    @FXML private void cerrarSesion() { navegar("Login.fxml", 900, 600); }
}