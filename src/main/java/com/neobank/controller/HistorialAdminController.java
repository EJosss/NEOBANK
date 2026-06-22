package com.neobank.controller;

import com.neobank.ApplicationContextProvider;
import com.neobank.model.*;
import com.neobank.service.ClienteService;
import com.neobank.service.CuentaService;
import com.neobank.service.MovimientoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HistorialAdminController extends BaseController {

    @FXML private Label lblUsuarioMenu;
    @FXML private TableView<Movimiento> tablaHistorialGlobal;
    @FXML private TableColumn<Movimiento, String> colFecha;
    @FXML private TableColumn<Movimiento, String> colOperacion;
    @FXML private TableColumn<Movimiento, String> colCuenta;
    @FXML private TableColumn<Movimiento, String> colCliente;
    @FXML private TableColumn<Movimiento, String> colTipo;
    @FXML private TableColumn<Movimiento, BigDecimal> colMonto;
    @FXML private TableColumn<Movimiento, String> colCajero;
    @FXML private TextField txtBuscarFiltro;

    private MovimientoService movimientoService;
    private CuentaService cuentaService;
    private ClienteService clienteService;

    private ObservableList<Movimiento> listaLogMaster = FXCollections.observableArrayList();
    private FilteredList<Movimiento> listaFiltradaLog;

    @FXML
    public void initialize() {
        movimientoService = ApplicationContextProvider.getBean(MovimientoService.class);
        cuentaService = ApplicationContextProvider.getBean(CuentaService.class);
        clienteService = ApplicationContextProvider.getBean(ClienteService.class);

        configurarColumnasTabla();
    }

    @Override
    protected void actualizarInfoUsuario() {
        if (usuarioActual != null) {
            lblUsuarioMenu.setText(usuarioActual.getNombre() + " | " + usuarioActual.getRol());

            // 🚀 CORRECCIÓN 1: Comparamos usando .contains() para que funcione sea ADMIN o ADMINISTRADOR
            if (!usuarioActual.getRol().name().contains("ADMIN")) {
                javafx.application.Platform.runLater(() -> {
                    mostrarNotificacionError("Violación de Seguridad", "Acceso denegado. Su usuario no posee privilegios para ver la bitácora global.");
                    navegar(lblUsuarioMenu, "Login.fxml", 900, 600);
                });
            } else {
                cargarBitacoraCompleta();
                configurarFiltroDinamico();
            }
        }
    }

    private void configurarColumnasTabla() {
        colFecha.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getFecha() != null ? cell.getValue().getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : ""
        ));

        colOperacion.setCellValueFactory(cell -> new SimpleStringProperty(String.format("OP-%06d", cell.getValue().getId())));

        colCuenta.setCellValueFactory(cellData -> {
            Long idCuenta = cellData.getValue().getIdCuenta();
            if (idCuenta != null) {
                return cuentaService.listarTodas().stream()
                        .filter(c -> c.getId().equals(idCuenta))
                        .map(CuentaBancaria::getNumeroCuenta)
                        .findFirst()
                        .map(SimpleStringProperty::new)
                        .orElse(new SimpleStringProperty("N/A"));
            }
            return new SimpleStringProperty("-");
        });

        colCliente.setCellValueFactory(cellData -> {
            Long idCuenta = cellData.getValue().getIdCuenta();
            if (idCuenta != null) {
                return cuentaService.listarTodas().stream()
                        .filter(c -> c.getId().equals(idCuenta))
                        .findFirst()
                        .map(cuenta -> {
                            return clienteService.listarTodos().stream()
                                    .filter(cli -> cli.getId().equals(cuenta.getIdCliente()))
                                    .map(cli -> cli.getDni() + " - " + cli.getNombre())
                                    .findFirst()
                                    .orElse("ID Cliente: " + cuenta.getIdCliente());
                        })
                        .map(SimpleStringProperty::new)
                        .orElse(new SimpleStringProperty("-"));
            }
            return new SimpleStringProperty("-");
        });

        colTipo.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getTipoMovimiento() != null ? cell.getValue().getTipoMovimiento().name() : ""
        ));

        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));

        // 🚀 CORRECCIÓN 2: Como el modelo de datos no rastrea al usuario, marcamos la operación genéricamente
        colCajero.setCellValueFactory(cell -> new SimpleStringProperty("Ventanilla / Sistema"));
    }

    private void cargarBitacoraCompleta() {
        List<Movimiento> todosLosMovimientos = movimientoService.listarTodos();
        listaLogMaster.setAll(todosLosMovimientos);
    }

    private void configurarFiltroDinamico() {
        listaFiltradaLog = new FilteredList<>(listaLogMaster, p -> true);

        txtBuscarFiltro.textProperty().addListener((observable, oldValue, newValue) -> {
            String query = newValue != null ? newValue.toLowerCase().trim() : "";

            listaFiltradaLog.setPredicate(movimiento -> {
                if (query.isEmpty()) return true;

                // Solo filtramos por el tipo de movimiento ya que el campo Cajero no existe en base de datos
                boolean coincideTipo = movimiento.getTipoMovimiento() != null && movimiento.getTipoMovimiento().name().toLowerCase().contains(query);

                return coincideTipo;
            });
        });

        tablaHistorialGlobal.setItems(listaFiltradaLog);
    }

    @FXML private void refrescarLog() { cargarBitacoraCompleta(); }
    @FXML private void irDashboard() { navegar(lblUsuarioMenu, "Dashboard.fxml", 1280, 720); }
    @FXML private void irClientes() { navegar(lblUsuarioMenu, "Clientes.fxml", 1280, 720); }
    @FXML private void irCuentas() { navegar(lblUsuarioMenu, "Cuentas.fxml", 1280, 720); }
    @FXML private void irMovimientos() { navegar(lblUsuarioMenu, "Movimientos.fxml", 1280, 720); }
    @FXML private void irTransferencias() { navegar(lblUsuarioMenu, "Transferencias.fxml", 1280, 720); }
    @FXML private void irTarjetas() { navegar(lblUsuarioMenu, "Tarjetas.fxml", 1280, 720); }
    @FXML private void cerrarSesion() { navegar(lblUsuarioMenu, "Login.fxml", 900, 600); }
}