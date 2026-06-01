package com.neobank.controller;

import com.neobank.ApplicationContextProvider;
import com.neobank.model.CuentaBancaria;
import com.neobank.model.Movimiento;
import com.neobank.model.Usuario;
import com.neobank.repository.ClienteRepository;
import com.neobank.repository.CuentaRepository;
import com.neobank.repository.MovimientoRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public class DashboardController {

    @FXML private Label lblUsuarioMenu;
    @FXML private Label lblTotalClientes;
    @FXML private Label lblTotalCuentas;
    @FXML private Label lblDineroTotal;
    @FXML private Label lblTransferenciasHoy;
    @FXML private BarChart<String, Number> barChart;

    private Usuario usuarioActual;

    // Repositorios para leer la base de datos real
    private ClienteRepository clienteRepository;
    private CuentaRepository cuentaRepository;
    private MovimientoRepository movimientoRepository;

    @FXML
    public void initialize() {
        clienteRepository = ApplicationContextProvider.getBean(ClienteRepository.class);
        cuentaRepository = ApplicationContextProvider.getBean(CuentaRepository.class);
        movimientoRepository = ApplicationContextProvider.getBean(MovimientoRepository.class);

        cargarEstadisticas();
        cargarGrafico();
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        if (lblUsuarioMenu != null && usuario != null) {
            lblUsuarioMenu.setText(usuario.getNombre() + " | " + usuario.getRol());
        }
    }

    private void cargarEstadisticas() {
        // 1. Contar Clientes
        long totalClientes = clienteRepository.count();
        lblTotalClientes.setText(String.valueOf(totalClientes));

        // 2. Contar Cuentas y sumar el dinero total del banco
        List<CuentaBancaria> cuentas = (List<CuentaBancaria>) cuentaRepository.findAll();
        lblTotalCuentas.setText(String.valueOf(cuentas.size()));

        BigDecimal dineroTotal = BigDecimal.ZERO;
        for (CuentaBancaria cuenta : cuentas) {
            if (cuenta.getSaldo() != null) {
                dineroTotal = dineroTotal.add(cuenta.getSaldo());
            }
        }
        lblDineroTotal.setText("S/ " + dineroTotal.toString());

        // 3. Contar Movimientos totales
        long totalMovimientos = movimientoRepository.count();
        lblTransferenciasHoy.setText(String.valueOf(totalMovimientos));
    }

    private void cargarGrafico() {
        barChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Resumen NeoBank");

        long totalClientes = clienteRepository.count();
        long totalCuentas = cuentaRepository.count();
        long totalMovs = movimientoRepository.count();

        series.getData().add(new XYChart.Data<>("Clientes", totalClientes));
        series.getData().add(new XYChart.Data<>("Cuentas", totalCuentas));
        series.getData().add(new XYChart.Data<>("Movimientos", totalMovs));

        barChart.getData().add(series);
        barChart.setLegendVisible(false);
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

            Stage stage = (Stage) lblUsuarioMenu.getScene().getWindow();
            stage.setScene(new Scene(root, w, h));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void irDashboard() {
        cargarEstadisticas();
        cargarGrafico();
    }

    @FXML private void irClientes() { navegar("Clientes.fxml", 1280, 720); }
    @FXML private void irCuentas() { navegar("Cuentas.fxml", 1280, 720); }
    @FXML private void irMovimientos() { navegar("Movimientos.fxml", 1280, 720); }
    @FXML private void irTransferencias() { navegar("Transferencias.fxml", 1280, 720); }
    @FXML private void irTarjetas() { }
    @FXML private void cerrarSesion() { navegar("Login.fxml", 900, 600); }
}