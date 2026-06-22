package com.neobank.controller;

import com.neobank.ApplicationContextProvider;
import com.neobank.model.Factura;
import com.neobank.model.enums.EstadoFactura;
import com.neobank.service.FacturaService;
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

public class FacturasController extends BaseController {

    @FXML private Label lblUsuarioMenu;
    @FXML private TableView<Factura> tablaFacturas;
    @FXML private TableColumn<Factura, Long> colId;
    @FXML private TableColumn<Factura, String> colNumero;
    @FXML private TableColumn<Factura, Long> colCliente;
    @FXML private TableColumn<Factura, String> colConcepto;
    @FXML private TableColumn<Factura, String> colFecha;
    @FXML private TableColumn<Factura, BigDecimal> colSubtotal;
    @FXML private TableColumn<Factura, BigDecimal> colIgv;
    @FXML private TableColumn<Factura, BigDecimal> colTotal;
    @FXML private TableColumn<Factura, String> colEstado;

    // Campos del nuevo formulario
    @FXML private TextField txtIdClienteForm;
    @FXML private TextField txtConceptoForm;
    @FXML private TextField txtMontoForm;

    @FXML private TextField txtBuscar;
    @FXML private CheckBox chkVerAnuladas;
    @FXML private Button btnAnular;
    @FXML private Button btnMenuDashboard;

    private FacturaService facturaService;
    private ObservableList<Factura> listaMaster = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        facturaService = ApplicationContextProvider.getBean(FacturaService.class);
        configurarTabla();
        cargarDatos();
        configurarBusquedaReactiva();
    }

    @Override
    protected void actualizarInfoUsuario() {
        if (lblUsuarioMenu != null && usuarioActual != null) {
            lblUsuarioMenu.setText(usuarioActual.getNombre() + " | " + usuarioActual.getRol());
            if (usuarioActual.getRol() == com.neobank.model.enums.RolUsuario.CAJERO) {
                if (btnAnular != null) { btnAnular.setVisible(false); btnAnular.setManaged(false); }
                if (btnMenuDashboard != null) { btnMenuDashboard.setVisible(false); btnMenuDashboard.setManaged(false); }
            }
        }
    }

    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroFactura"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colConcepto.setCellValueFactory(new PropertyValueFactory<>("concepto"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        colIgv.setCellValueFactory(new PropertyValueFactory<>("igv"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        colFecha.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getFechaEmision() != null ? cell.getValue().getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""
        ));

        colEstado.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getEstado() != null ? cell.getValue().getEstado().name() : ""
        ));

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else {
                    setText(item);
                    if (item.equals("PAGADA")) setStyle("-fx-text-fill: #43A047; -fx-font-weight: bold;");
                    else if (item.equals("PENDIENTE")) setStyle("-fx-text-fill: #FB8C00; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: #E53935; -fx-font-weight: bold;");
                }
            }
        });
    }

    private void cargarDatos() {
        List<Factura> facturas = facturaService.listarTodas();
        listaMaster.setAll(facturas);
    }

    private void configurarBusquedaReactiva() {
        FilteredList<Factura> filtrada = new FilteredList<>(listaMaster, f -> f.getEstado() != EstadoFactura.ANULADA);

        Runnable filtrar = () -> {
            String txt = txtBuscar.getText() != null ? txtBuscar.getText().toLowerCase().trim() : "";
            boolean verAnuladas = chkVerAnuladas.isSelected();

            filtrada.setPredicate(factura -> {
                EstadoFactura esperado = verAnuladas ? EstadoFactura.ANULADA : EstadoFactura.PAGADA;

                if (!verAnuladas && factura.getEstado() == EstadoFactura.PENDIENTE) {
                    // Permitir mostrar pendientes
                } else if (factura.getEstado() != esperado) {
                    return false;
                }

                if (txt.isEmpty()) return true;
                return factura.getNumeroFactura().toLowerCase().contains(txt);
            });
        };

        txtBuscar.textProperty().addListener((o, old, nw) -> filtrar.run());
        chkVerAnuladas.selectedProperty().addListener((o, old, nw) -> filtrar.run());
        tablaFacturas.setItems(filtrada);
    }

    // EL MÉTODO FALTANTE PARA AGREGAR LA FACTURA
    @FXML
    private void agregarNuevaFactura() {
        try {
            if (txtIdClienteForm.getText().isEmpty() || txtConceptoForm.getText().isEmpty() || txtMontoForm.getText().isEmpty()) {
                mostrarNotificacionError("Campos Vacíos", "Por favor, complete todos los campos del formulario.");
                return;
            }

            Long idCliente = Long.parseLong(txtIdClienteForm.getText().trim());
            String concepto = txtConceptoForm.getText().trim();
            BigDecimal monto = new BigDecimal(txtMontoForm.getText().trim());

            facturaService.registrarFactura(idCliente, concepto, monto);

            // Limpiar formulario y recargar la tabla
            txtIdClienteForm.clear();
            txtConceptoForm.clear();
            txtMontoForm.clear();
            cargarDatos();

            mostrarNotificacionExito("Éxito", "Factura emitida y registrada contablemente.");
        } catch (NumberFormatException e) {
            mostrarNotificacionError("Error de Formato", "Asegúrese de que el ID Cliente y el Monto Base sean números válidos (ej: 1500.50).");
        } catch (IllegalArgumentException e) {
            mostrarNotificacionError("Datos Inválidos", e.getMessage());
        } catch (Exception e) {
            mostrarNotificacionError("Error de Sistema", "Ocurrió un problema al guardar la factura.");
        }
    }

    @FXML private void limpiarFiltro() { txtBuscar.clear(); cargarDatos(); }

    @FXML private void anularFactura() {
        Factura sel = tablaFacturas.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarNotificacionError("Selección Requerida", "Marque un comprobante de la tabla para anularlo."); return; }
        try {
            facturaService.anularFactura(sel.getId(), usuarioActual);
            cargarDatos();
            mostrarNotificacionExito("Comprobante Anulado", "La factura " + sel.getNumeroFactura() + " fue anulada en el libro contable.");
        } catch (IllegalArgumentException e) {
            mostrarNotificacionError("Operación Rechazada", e.getMessage());
        }
    }

    @FXML private void simularImpresion() {
        Factura sel = tablaFacturas.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarNotificacionError("Selección Requerida", "Seleccione una factura para simular su descarga."); return; }
        mostrarNotificacionExito("Impresión Solicitada", "Generando archivo PDF para el comprobante " + sel.getNumeroFactura() + "...\nGuardado con éxito en la carpeta raíz del sistema.");
    }

    @FXML private void irDashboard() { navegar(lblUsuarioMenu, "Dashboard.fxml", 1280, 720); }
    @FXML private void irClientes() { navegar(lblUsuarioMenu, "Clientes.fxml", 1280, 720); }
    @FXML private void irCuentas() { navegar(lblUsuarioMenu, "Cuentas.fxml", 1280, 720); }
    @FXML private void irMovimientos() { navegar(lblUsuarioMenu, "Movimientos.fxml", 1280, 720); }
    @FXML private void irTransferencias() { navegar(lblUsuarioMenu, "Transferencias.fxml", 1280, 720); }
    @FXML private void irTarjetas() { navegar(lblUsuarioMenu, "Tarjetas.fxml", 1280, 720); }
    @FXML private void cerrarSesion() { navegar(lblUsuarioMenu, "Login.fxml", 900, 600); }
}