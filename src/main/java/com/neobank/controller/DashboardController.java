package com.neobank.controller;

import com.neobank.ApplicationContextProvider;
import com.neobank.model.Usuario;
import com.neobank.repository.ClienteRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.File;

public class DashboardController {

    @FXML private Label lblUsuarioMenu;
    @FXML private Label lblTotalClientes;
    @FXML private Label lblTotalCuentas;
    @FXML private Label lblDineroTotal;
    @FXML private Label lblTransferenciasHoy;
    @FXML private BarChart<String, Number> barChart;

    private Usuario usuarioActual;
    private ClienteRepository clienteRepository;

    @FXML
    public void initialize() {
        clienteRepository = ApplicationContextProvider
                .getBean(ClienteRepository.class);
        cargarEstadisticas();
        cargarGrafico();
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        lblUsuarioMenu.setText(
                usuario.getNombre() + " | " + usuario.getRol()
        );
    }

    private void cargarEstadisticas() {
        long totalClientes = clienteRepository.count();
        lblTotalClientes.setText(String.valueOf(totalClientes));
        lblTotalCuentas.setText("0");
        lblDineroTotal.setText("S/ 0.00");
        lblTransferenciasHoy.setText("0");
    }

    private void cargarGrafico() {
        barChart.getData().clear();
        XYChart.Series<String, Number> series =
                new XYChart.Series<>();
        series.setName("NeoBank");
        series.getData().add(
                new XYChart.Data<>("Clientes",
                        clienteRepository.count()));
        series.getData().add(
                new XYChart.Data<>("Cuentas", 0));
        series.getData().add(
                new XYChart.Data<>("Movimientos", 0));
        series.getData().add(
                new XYChart.Data<>("Transferencias", 0));
        barChart.getData().add(series);
        barChart.setLegendVisible(false);
    }

    // ── Navegación ──────────────────────────────────
    private void navegar(String fxml, int w, int h) {
        try {
            String ruta = System.getProperty("user.dir")
                    + "/src/main/resources/fxml/" + fxml;
            FXMLLoader loader = new FXMLLoader(
                    new File(ruta).toURI().toURL()
            );
            Parent root = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof DashboardController dc)
                dc.setUsuario(usuarioActual);
            else if (ctrl instanceof ClientesController cc)
                cc.setUsuario(usuarioActual);
            Stage stage = (Stage) lblUsuarioMenu
                    .getScene().getWindow();
            stage.setScene(new Scene(root, w, h));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void irDashboard() {
        cargarEstadisticas();
        cargarGrafico();
    }

    @FXML
    private void irClientes() {
        navegar("Clientes.fxml", 1280, 720);
    }

    @FXML
    private void irCuentas() {
        // próximo módulo
    }

    @FXML
    private void irMovimientos() {
        // próximo módulo
    }

    @FXML
    private void irTransferencias() {
        // próximo módulo
    }

    @FXML
    private void irTarjetas() {
        // próximo módulo
    }

    @FXML
    private void cerrarSesion() {
        navegar("Login.fxml", 900, 600);
    }
}