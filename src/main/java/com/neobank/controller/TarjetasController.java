package com.neobank.controller;

import com.neobank.ApplicationContextProvider;
import com.neobank.model.CuentaBancaria;
import com.neobank.model.Tarjeta;
import com.neobank.model.enums.EstadoTarjeta;
import com.neobank.model.enums.TipoTarjeta;
import com.neobank.service.CuentaService;
import com.neobank.service.TarjetaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

public class TarjetasController extends BaseController {

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

    @FXML private Button btnBloquear;
    @FXML private Button btnMenuDashboard;

    private TarjetaService tarjetaService;
    private CuentaService cuentaService;

    @FXML
    public void initialize() {
        tarjetaService = ApplicationContextProvider.getBean(TarjetaService.class);
        cuentaService = ApplicationContextProvider.getBean(CuentaService.class);
        configurarTabla();
        cargarDatos();
        configurarLimpiezaAutomatica(lblMensaje, cmbCuentas, cmbTipo, txtLimite);
    }

    @Override
    protected void actualizarInfoUsuario() {
        if (lblUsuarioMenu != null && usuarioActual != null) {
            lblUsuarioMenu.setText(usuarioActual.getNombre() + " | " + usuarioActual.getRol());

            if (usuarioActual.getRol() == com.neobank.model.enums.RolUsuario.CAJERO) {
                if (btnBloquear != null) { btnBloquear.setVisible(false); btnBloquear.setManaged(false); }
                if (btnMenuDashboard != null) { btnMenuDashboard.setVisible(false); btnMenuDashboard.setManaged(false); }
            }
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
                    setStyle(item.equals("ACTIVA") ? "-fx-text-fill: #43A047; -fx-font-weight: bold;" : "-fx-text-fill: #E53935; -fx-font-weight: bold;");
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
        limpiarBordes(cmbCuentas, cmbTipo, txtLimite);
        panelFormulario.setVisible(true);
        panelFormulario.setManaged(true);
        lblMensaje.setText("");
    }

    @FXML private void guardarTarjeta() {
        limpiarBordes(cmbCuentas, cmbTipo, txtLimite);
        boolean hayError = false;

        if (cmbCuentas.getValue() == null) { marcarCampoErroneo(cmbCuentas); hayError = true; }
        if (cmbTipo.getValue() == null) { marcarCampoErroneo(cmbTipo); hayError = true; }

        if (hayError) {
            mostrarNotificacionError("Campos Obligatorios", "Complete la información de cuenta y tipo de tarjeta.");
            return;
        }

        CuentaBancaria cuenta = cmbCuentas.getValue();
        TipoTarjeta tipo = cmbTipo.getValue();
        BigDecimal limite = BigDecimal.ZERO;

        if (tipo == TipoTarjeta.CREDITO) {
            if (txtLimite.getText().trim().isEmpty()) {
                marcarCampoErroneo(txtLimite);
                mostrarNotificacionError("Límite Requerido", "Las tarjetas de crédito exigen un monto límite de operación.");
                return;
            }
            try {
                limite = new BigDecimal(txtLimite.getText().trim());
                if (limite.compareTo(BigDecimal.ZERO) <= 0) {
                    mostrarNotificacionError("Monto Inválido", "El límite de crédito tiene que ser estrictamente superior a 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarNotificacionError("Formato de Monto", "El campo límite solo acepta valores numéricos.");
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

        try {
            tarjetaService.guardar(nueva, usuarioActual);
            cargarDatos();
            panelFormulario.setVisible(false);
            panelFormulario.setManaged(false);
            mostrarNotificacionExito("Emisión Exitosa", "La tarjeta " + tipo.name() + " número '" + numero + "' ha sido generada correctamente.");
        } catch (IllegalArgumentException e) {
            mostrarNotificacionError("Restricción de Emisión", e.getMessage());
        }
    }

    @FXML private void bloquearTarjeta() {
        Tarjeta sel = tablaTarjetas.getSelectionModel().getSelectedItem();
        if (sel != null && sel.getEstado() == EstadoTarjeta.ACTIVA) {
            try {
                tarjetaService.bloquear(sel.getId(), usuarioActual);
                cargarDatos();
                mostrarNotificacionExito("Bloqueo Realizado", "La tarjeta '" + sel.getNumeroTarjeta() + "' ha sido dada de baja (BLOQUEADA) con éxito.");
            } catch (IllegalArgumentException e) {
                mostrarNotificacionError("Restricción de Acceso", e.getMessage());
            }
        } else {
            mostrarNotificacionError("Selección Inválida", "Marque una tarjeta que se encuentre en estado ACTIVA para inhabilitarla.");
        }
    }

    @FXML private void cancelarForm() {
        panelFormulario.setVisible(false);
        panelFormulario.setManaged(false);
    }

    @FXML private void irDashboard() { navegar(lblUsuarioMenu, "Dashboard.fxml", 1280, 720); }
    @FXML private void irClientes() { navegar(lblUsuarioMenu, "Clientes.fxml", 1280, 720); }
    @FXML private void irCuentas() { navegar(lblUsuarioMenu, "Cuentas.fxml", 1280, 720); }
    @FXML private void irMovimientos() { navegar(lblUsuarioMenu, "Movimientos.fxml", 1280, 720); }
    @FXML private void irTransferencias() { navegar(lblUsuarioMenu, "Transferencias.fxml", 1280, 720); }
    @FXML private void irTarjetas() { }
    @FXML private void cerrarSesion() { navegar(lblUsuarioMenu, "Login.fxml", 900, 600); }
}