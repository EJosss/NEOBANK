package com.neobank.controller;

import com.neobank.model.Usuario;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.io.File;
import java.io.FileOutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.itextpdf.text.Chunk;

public abstract class BaseController {

    protected Usuario usuarioActual;

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        actualizarInfoUsuario();
    }

    protected abstract void actualizarInfoUsuario();

    protected void navegar(Label nodoOrigen, String fxml, int w, int h) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxml));
            Parent root = loader.load();
            Object ctrl = loader.getController();

            if (ctrl instanceof BaseController baseCtrl) {
                baseCtrl.setUsuario(this.usuarioActual);
            }

            Stage stage = (Stage) nodoOrigen.getScene().getWindow();
            stage.setScene(new Scene(root, w, h));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("⚠ Error crítico al cargar la vista: /fxml/" + fxml);
        }
    }

    protected void mostrarMensajeUniversal(Label lbl, String msg, boolean exito) {
        if (lbl != null) {
            lbl.setStyle(exito ? "-fx-text-fill: #66bb6a;" : "-fx-text-fill: #ef5350;");
            lbl.setText(msg);
        }
    }

    protected void marcarCampoErroneo(javafx.scene.control.Control campo) {
        campo.setStyle("-fx-border-color: #ef5350; -fx-border-width: 2px; -fx-border-radius: 4px; -fx-background-radius: 4px;");
    }

    protected void limpiarBordes(javafx.scene.control.Control... campos) {
        for (javafx.scene.control.Control campo : campos) {
            campo.setStyle("");
        }
    }

    protected void configurarLimpiezaAutomatica(javafx.scene.control.Label lblError, javafx.scene.control.Control... campos) {
        for (javafx.scene.control.Control campo : campos) {
            if (campo instanceof javafx.scene.control.TextInputControl textInput) {
                textInput.textProperty().addListener((observable, oldValue, newValue) -> {
                    campo.setStyle("");
                    if (lblError != null) lblError.setText("");
                });
            } else if (campo instanceof javafx.scene.control.ComboBox<?> combo) {
                combo.valueProperty().addListener((observable, oldValue, newValue) -> {
                    campo.setStyle("");
                    if (lblError != null) lblError.setText("");
                });
            }
        }
    }

    protected void mostrarNotificacionExito(String titulo, String mensaje) {
        javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alerta.setTitle("NeoBank - Confirmación");
        alerta.setHeaderText(null);
        alerta.setContentText("✔ " + titulo + "\n\n" + mensaje);
        javafx.scene.control.DialogPane dialogPane = alerta.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #FFFFFF; -fx-font-size: 14px; -fx-border-color: #43A047; -fx-border-width: 0 0 0 6;");
        alerta.showAndWait();
    }

    protected void mostrarNotificacionError(String titulo, String mensaje) {
        javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alerta.setTitle("NeoBank - Alerta de Seguridad");
        alerta.setHeaderText(null);
        alerta.setContentText("⛔ " + titulo + "\n\n" + mensaje);
        javafx.scene.control.DialogPane dialogPane = alerta.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #FFFFFF; -fx-font-size: 14px; -fx-border-color: #E53935; -fx-border-width: 0 0 0 6;");
        alerta.showAndWait();
    }

    // 🚀 ESTE ES EL MÉTODO QUE TU ARCHIVO NO ENCONTRABA
    protected void mostrarVoucherPantalla(String numOperacion, String numCuenta, String tipoOp, BigDecimal monto) {
        javafx.scene.control.Dialog<javafx.scene.control.ButtonType> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Comprobante Electrónico");

        javafx.scene.layout.VBox ticket = new javafx.scene.layout.VBox(15);
        ticket.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 30; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #E0E0E0; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 5);");
        ticket.setPrefWidth(420);

        javafx.scene.control.Label lblIcono = new javafx.scene.control.Label("✓");
        lblIcono.setStyle("-fx-font-size: 50px; -fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-alignment: center;");
        lblIcono.setMaxWidth(Double.MAX_VALUE);

        javafx.scene.control.Label lblHeader = new javafx.scene.control.Label("¡Operación Exitosa!");
        lblHeader.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1A1A1A; -fx-alignment: center;");
        lblHeader.setMaxWidth(Double.MAX_VALUE);

        javafx.scene.control.Label lblSubHeader = new javafx.scene.control.Label("Comprobante Electrónico - NeoBank");
        lblSubHeader.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888; -fx-alignment: center;");
        lblSubHeader.setMaxWidth(Double.MAX_VALUE);

        javafx.scene.layout.Region separador1 = new javafx.scene.layout.Region();
        separador1.setPrefHeight(1);
        separador1.setStyle("-fx-background-color: #EEEEEE;");

        javafx.scene.layout.GridPane gridDatos = new javafx.scene.layout.GridPane();
        gridDatos.setVgap(12);
        gridDatos.setHgap(20);

        String cuentaOculta = "**** **** **** " + (numCuenta.length() >= 4 ? numCuenta.substring(numCuenta.length() - 4) : numCuenta);
        String fechaHoraActual = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        javafx.scene.control.Label lblTit1 = new javafx.scene.control.Label("N° de Operación:"); lblTit1.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888;");
        javafx.scene.control.Label lblVal1 = new javafx.scene.control.Label(numOperacion); lblVal1.setStyle("-fx-font-size: 15px; -fx-text-fill: #1A1A1A; -fx-font-weight: bold;");
        gridDatos.add(lblTit1, 0, 0); gridDatos.add(lblVal1, 1, 0);

        javafx.scene.control.Label lblTit2 = new javafx.scene.control.Label("Fecha y Hora:"); lblTit2.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888;");
        javafx.scene.control.Label lblVal2 = new javafx.scene.control.Label(fechaHoraActual); lblVal2.setStyle("-fx-font-size: 15px; -fx-text-fill: #1A1A1A; -fx-font-weight: bold;");
        gridDatos.add(lblTit2, 0, 1); gridDatos.add(lblVal2, 1, 1);

        javafx.scene.control.Label lblTit3 = new javafx.scene.control.Label("Tipo Operación:"); lblTit3.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888;");
        javafx.scene.control.Label lblVal3 = new javafx.scene.control.Label(tipoOp); lblVal3.setStyle("-fx-font-size: 15px; -fx-text-fill: #1A1A1A; -fx-font-weight: bold;");
        gridDatos.add(lblTit3, 0, 2); gridDatos.add(lblVal3, 1, 2);

        javafx.scene.control.Label lblTit4 = new javafx.scene.control.Label("Cuenta Afectada:"); lblTit4.setStyle("-fx-font-size: 14px; -fx-text-fill: #888888;");
        javafx.scene.control.Label lblVal4 = new javafx.scene.control.Label(cuentaOculta); lblVal4.setStyle("-fx-font-size: 15px; -fx-text-fill: #1A1A1A; -fx-font-weight: bold;");
        gridDatos.add(lblTit4, 0, 3); gridDatos.add(lblVal4, 1, 3);

        javafx.scene.layout.Region separador2 = new javafx.scene.layout.Region();
        separador2.setPrefHeight(1);
        separador2.setStyle("-fx-background-color: #EEEEEE;");

        javafx.scene.layout.HBox boxTotal = new javafx.scene.layout.HBox();
        boxTotal.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        javafx.scene.control.Label lblTotalTxt = new javafx.scene.control.Label("Importe Total:");
        lblTotalTxt.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #666666;");
        javafx.scene.control.Label lblTotalMonto = new javafx.scene.control.Label("S/ " + monto.toString());
        lblTotalMonto.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #1D52A8;");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        boxTotal.getChildren().addAll(lblTotalTxt, spacer, lblTotalMonto);

        ticket.getChildren().addAll(lblIcono, lblHeader, lblSubHeader, separador1, gridDatos, separador2, boxTotal);
        dialog.getDialogPane().setContent(ticket);

        javafx.scene.control.ButtonType btnImprimir = new javafx.scene.control.ButtonType("🖨 Imprimir");
        javafx.scene.control.ButtonType btnPdf = new javafx.scene.control.ButtonType("💾 Exportar PDF");
        javafx.scene.control.ButtonType btnCerrar = new javafx.scene.control.ButtonType("Cerrar", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);

        dialog.getDialogPane().getButtonTypes().addAll(btnImprimir, btnPdf, btnCerrar);

        dialog.showAndWait().ifPresent(response -> {
            if (response == btnImprimir) {
                mostrarNotificacionExito("Impresión en curso", "Enviando el comprobante " + numOperacion + " a la impresora.");
            } else if (response == btnPdf) {
                generarPDFReal(numOperacion, numCuenta, tipoOp, monto);
            }
        });
    }

    private void generarPDFReal(String numOperacion, String numCuenta, String tipoOp, BigDecimal monto) {
        try {
            String userHome = System.getProperty("user.home");
            String carpetaDescargas = userHome + File.separator + "Downloads";
            String rutaArchivo = carpetaDescargas + File.separator + "NeoBank_Voucher_" + numOperacion + ".pdf";

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(rutaArchivo));
            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, new BaseColor(29, 82, 168));
            Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 14, BaseColor.GRAY);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
            Font fontTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(67, 160, 71));

            Paragraph titulo = new Paragraph("⬢ NeoBank\n", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            Paragraph subtitulo = new Paragraph("Comprobante Electrónico de Operación\n\n", fontSubtitulo);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitulo);

            document.add(new Chunk(new LineSeparator()));
            document.add(new Paragraph("\n"));

            String cuentaOculta = "**** **** **** " + (numCuenta.length() >= 4 ? numCuenta.substring(numCuenta.length() - 4) : numCuenta);
            String fechaHoraActual = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            document.add(new Paragraph("N° de Operación: " + numOperacion, fontNormal));
            document.add(new Paragraph("Fecha y Hora: " + fechaHoraActual, fontNormal));
            document.add(new Paragraph("Tipo de Operación: " + tipoOp, fontNormal));
            document.add(new Paragraph("Cuenta Afectada: " + cuentaOculta, fontNormal));

            document.add(new Paragraph("\n"));
            document.add(new Chunk(new LineSeparator()));
            document.add(new Paragraph("\n"));

            Paragraph totalTxt = new Paragraph("IMPORTE TOTAL: S/ " + monto.toString(), fontTotal);
            totalTxt.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalTxt);

            document.close();

            mostrarNotificacionExito("Exportación Exitosa", "El comprobante PDF se guardó correctamente en:\n" + rutaArchivo);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarNotificacionError("Error al generar PDF", "Ocurrió un problema de escritura en el disco: " + e.getMessage());
        }
    }
}