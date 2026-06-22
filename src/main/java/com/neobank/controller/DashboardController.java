package com.neobank.controller;

import com.neobank.ApplicationContextProvider;
import com.neobank.model.CuentaBancaria;
import com.neobank.repository.ClienteRepository;
import com.neobank.repository.CuentaRepository;
import com.neobank.repository.MovimientoRepository;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import java.math.BigDecimal;
import java.util.List;

public class DashboardController extends BaseController {

    @FXML private Label lblUsuarioMenu;
    @FXML private Label lblTotalClientes;
    @FXML private Label lblTotalCuentas;
    @FXML private Label lblDineroTotal;
    @FXML private Label lblTransferenciasHoy;
    @FXML private BarChart<String, Number> barChart;

    // Repositorios
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

    @Override
    protected void actualizarInfoUsuario() {
        if (lblUsuarioMenu != null && usuarioActual != null) {
            lblUsuarioMenu.setText(usuarioActual.getNombre() + " | " + usuarioActual.getRol());
        }
    }

    private void cargarEstadisticas() {
        long totalClientes = clienteRepository.count();
        lblTotalClientes.setText(String.valueOf(totalClientes));

        List<CuentaBancaria> cuentas = (List<CuentaBancaria>) cuentaRepository.findAll();
        lblTotalCuentas.setText(String.valueOf(cuentas.size()));

        BigDecimal dineroTotal = BigDecimal.ZERO;
        for (CuentaBancaria cuenta : cuentas) {
            if (cuenta.getSaldo() != null) {
                dineroTotal = dineroTotal.add(cuenta.getSaldo());
            }
        }
        lblDineroTotal.setText("S/ " + dineroTotal.toString());

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

    @FXML private void irDashboard() { cargarEstadisticas(); cargarGrafico(); }
    @FXML private void irClientes() { navegar(lblUsuarioMenu, "Clientes.fxml", 1280, 720); }
    @FXML private void irCuentas() { navegar(lblUsuarioMenu, "Cuentas.fxml", 1280, 720); }
    @FXML private void irMovimientos() { navegar(lblUsuarioMenu, "Movimientos.fxml", 1280, 720); }
    @FXML private void irTransferencias() { navegar(lblUsuarioMenu, "Transferencias.fxml", 1280, 720); }
    @FXML private void irTarjetas() { navegar(lblUsuarioMenu, "Tarjetas.fxml", 1280, 720); }
    @FXML private void cerrarSesion() { navegar(lblUsuarioMenu, "Login.fxml", 900, 600); }
    @FXML
    private void irHistorial() {
        navegar(lblUsuarioMenu, "HistorialAdmin.fxml", 1280, 720);
    }
}