package com.sptv.mavenproject2.ui;

import com.sptv.mavenproject2.dto.FilaPagosAnuales;
import com.sptv.mavenproject2.service.ClienteService;
import com.sptv.mavenproject2.service.PagoMensualService;
import com.sptv.mavenproject2.service.ValidationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Vista de Matriz Anual de Pagos Mensuales (2024–2030).
 * Reemplaza a Tabladedepagos (Swing) corrigiendo el modelo sobrescrito,
 * el typo de Noviembre y las consultas N+1.
 */
public class PagosMatrixView {

    private final PagoMensualService pagoMensualService;
    private final BorderPane root;

    private ComboBox<Integer> cmbAnio;
    private final ObservableList<FilaPagosAnuales> masterRows = FXCollections.observableArrayList();
    private FilteredList<FilaPagosAnuales> filteredRows;
    private TableView<FilaPagosAnuales> table;

    private Label feedbackLabel;
    private HBox feedbackBanner;

    private ComboBox<String> cmbMesRapido;
    private TextField txtMontoRapido;
    private Label lblClienteSeleccionado;

    public PagosMatrixView(PagoMensualService pagoMensualService) {
        this.pagoMensualService = pagoMensualService;
        this.root = buildView();
        recargarMatriz();
    }

    private BorderPane buildView() {
        BorderPane container = new BorderPane();
        container.setPadding(new Insets(20));

        VBox topBox = new VBox(12);
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Control y Matriz Anual de Pagos Mensuales");
        title.getStyleClass().add("heading-lg");
        Label subtitle = new Label(
                "Seleccione el ejercicio fiscal (2024–2030) y edite los pagos mensuales directamente en la tabla o en el panel inferior.");
        subtitle.getStyleClass().add("text-muted");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblAnio = UiFeedback.createFieldLabel("Año Fiscal:");
        List<Integer> anios = new ArrayList<>();
        for (int a = ClienteService.ANIO_INICIO_PAGOS; a <= ClienteService.ANIO_FIN_PAGOS; a++) {
            anios.add(a);
        }
        cmbAnio = new ComboBox<>(FXCollections.observableArrayList(anios));
        cmbAnio.setValue(ClienteService.ANIO_INICIO_PAGOS);
        cmbAnio.setOnAction(e -> recargarMatriz());

        TextField txtFiltro = new TextField();
        txtFiltro.setPromptText("Filtrar cliente o RUC...");
        txtFiltro.setPrefWidth(200);

        Button btnRefrescar = new Button("↻ Recargar Año");
        btnRefrescar.getStyleClass().add("btn-secondary");
        btnRefrescar.setOnAction(e -> recargarMatriz());

        headerRow.getChildren().addAll(titleBox, spacer, lblAnio, cmbAnio, txtFiltro, btnRefrescar);

        feedbackLabel = new Label();
        feedbackBanner = UiFeedback.createFeedbackBanner(feedbackLabel);

        topBox.getChildren().addAll(headerRow, feedbackBanner);
        topBox.setPadding(new Insets(0, 0, 12, 0));
        container.setTop(topBox);

        table = new TableView<>();
        table.setEditable(true);

        TableColumn<FilaPagosAnuales, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue().getIdCliente())));
        colId.setPrefWidth(50);

        TableColumn<FilaPagosAnuales, String> colNombre = new TableColumn<>("Cliente ✎");
        colNombre.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNombreCliente()));
        colNombre.setCellFactory(TextFieldTableCell.forTableColumn());
        colNombre.setPrefWidth(190);
        colNombre.setOnEditCommit(event -> {
            FilaPagosAnuales fila = event.getRowValue();
            try {
                pagoMensualService.actualizarNombreClienteDesdePagos(fila.getIdCliente(), event.getNewValue());
                recargarMatriz();
                UiFeedback.showBannerSuccess(feedbackBanner, feedbackLabel,
                        "Nombre del cliente actualizado a «" + event.getNewValue() + "».");
            } catch (ValidationException ve) {
                UiFeedback.showBannerError(feedbackBanner, feedbackLabel, ve.getMessage());
                table.refresh();
            }
        });

        TableColumn<FilaPagosAnuales, String> colRuc = new TableColumn<>("RUC");
        colRuc.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getRuc()));
        colRuc.setPrefWidth(105);

        List<TableColumn<FilaPagosAnuales, String>> columns = new ArrayList<>();
        columns.add(colId);
        columns.add(colNombre);
        columns.add(colRuc);

        for (int m = 1; m <= 12; m++) {
            final int mesNumero = m;
            String nombreMes = FilaPagosAnuales.NOMBRES_MESES[m - 1];
            TableColumn<FilaPagosAnuales, String> colMes = new TableColumn<>(nombreMes.substring(0, 3) + " ✎");
            colMes.setPrefWidth(78);
            colMes.setCellValueFactory(cd -> new SimpleStringProperty(
                    cd.getValue().getMontoMes(mesNumero).toPlainString()));
            colMes.setCellFactory(TextFieldTableCell.forTableColumn());
            colMes.setOnEditCommit(event -> {
                FilaPagosAnuales fila = event.getRowValue();
                guardarPagoMes(fila, mesNumero, event.getNewValue());
            });
            columns.add(colMes);
        }

        TableColumn<FilaPagosAnuales, String> colTotal = new TableColumn<>("Total Anual (S/)");
        colTotal.setPrefWidth(115);
        colTotal.setCellValueFactory(cd -> new SimpleStringProperty(
                "S/ " + cd.getValue().getTotalAnual().toPlainString()));
        columns.add(colTotal);

        table.getColumns().setAll(columns);

        filteredRows = new FilteredList<>(masterRows, p -> true);
        txtFiltro.textProperty().addListener((obs, oldV, newV) -> {
            String q = newV == null ? "" : newV.trim().toLowerCase();
            filteredRows.setPredicate(fila -> {
                if (q.isEmpty()) {
                    return true;
                }
                return (fila.getNombreCliente() != null && fila.getNombreCliente().toLowerCase().contains(q))
                        || (fila.getRuc() != null && fila.getRuc().toLowerCase().contains(q));
            });
        });
        table.setItems(filteredRows);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                lblClienteSeleccionado.setText(newSel.getNombreCliente() + " (RUC: " + newSel.getRuc() + ")");
                int mesIdx = cmbMesRapido.getSelectionModel().getSelectedIndex() + 1;
                if (mesIdx >= 1 && mesIdx <= 12) {
                    txtMontoRapido.setText(newSel.getMontoMes(mesIdx).toPlainString());
                }
            } else {
                lblClienteSeleccionado.setText("Ningún cliente seleccionado");
            }
        });

        container.setCenter(table);

        // Barra inferior para registrar/actualizar pago puntual con comodidad
        HBox bottomBar = new HBox(12);
        bottomBar.getStyleClass().add("surface-card-compact");
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        BorderPane.setMargin(bottomBar, new Insets(12, 0, 0, 0));

        Label lblSelTitle = UiFeedback.createFieldLabel("Cliente seleccionado:");
        lblClienteSeleccionado = new Label("Ningún cliente seleccionado");

        Label lblMes = UiFeedback.createFieldLabel("Mes:");
        cmbMesRapido = new ComboBox<>(FXCollections.observableArrayList(FilaPagosAnuales.NOMBRES_MESES));
        cmbMesRapido.getSelectionModel().selectFirst();
        cmbMesRapido.setOnAction(e -> {
            FilaPagosAnuales sel = table.getSelectionModel().getSelectedItem();
            int mesIdx = cmbMesRapido.getSelectionModel().getSelectedIndex() + 1;
            if (sel != null && mesIdx >= 1 && mesIdx <= 12) {
                txtMontoRapido.setText(sel.getMontoMes(mesIdx).toPlainString());
            }
        });

        Label lblMonto = UiFeedback.createFieldLabel("Monto Pagado (S/):");
        txtMontoRapido = new TextField("0.00");
        txtMontoRapido.setPrefWidth(110);

        Button btnAplicarPago = new Button("Registrar / Actualizar Pago del Mes");
        btnAplicarPago.getStyleClass().add("btn-primary");
        btnAplicarPago.setOnAction(e -> {
            FilaPagosAnuales sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                UiFeedback.showBannerError(feedbackBanner, feedbackLabel,
                        "Seleccione primero un cliente en la tabla de pagos.");
                return;
            }
            int mesNumero = cmbMesRapido.getSelectionModel().getSelectedIndex() + 1;
            guardarPagoMes(sel, mesNumero, txtMontoRapido.getText());
        });

        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, Priority.ALWAYS);

        bottomBar.getChildren().addAll(
                lblSelTitle, lblClienteSeleccionado,
                bottomSpacer,
                lblMes, cmbMesRapido,
                lblMonto, txtMontoRapido,
                btnAplicarPago
        );

        container.setBottom(bottomBar);
        return container;
    }

    private void guardarPagoMes(FilaPagosAnuales fila, int mesNumero, String valorTexto) {
        try {
            String limpio = valorTexto != null ? valorTexto.trim().replace(",", ".") : "0";
            BigDecimal monto = new BigDecimal(limpio);
            int anio = cmbAnio.getValue() != null ? cmbAnio.getValue() : ClienteService.ANIO_INICIO_PAGOS;
            pagoMensualService.actualizarPagoMensual(fila.getIdCliente(), anio, mesNumero, monto);
            recargarMatriz();
            UiFeedback.showBannerSuccess(feedbackBanner, feedbackLabel,
                    "Pago de " + FilaPagosAnuales.NOMBRES_MESES[mesNumero - 1] + " " + anio
                            + " para «" + fila.getNombreCliente() + "» actualizado a S/ " + monto.toPlainString() + ".");
        } catch (NumberFormatException nfe) {
            UiFeedback.showBannerError(feedbackBanner, feedbackLabel,
                    "El monto ingresado debe ser un número decimal válido (ej. 150.00).");
            table.refresh();
        } catch (ValidationException ve) {
            UiFeedback.showBannerError(feedbackBanner, feedbackLabel, ve.getMessage());
            table.refresh();
        } catch (Exception ex) {
            UiFeedback.showBannerError(feedbackBanner, feedbackLabel,
                    "Error al actualizar pago: " + ex.getMessage());
            table.refresh();
        }
    }

    public final void recargarMatriz() {
        int anio = cmbAnio != null && cmbAnio.getValue() != null
                ? cmbAnio.getValue()
                : ClienteService.ANIO_INICIO_PAGOS;
        List<FilaPagosAnuales> filas = pagoMensualService.obtenerMatrizAnual(anio);
        masterRows.setAll(filas);
    }

    public Parent getView() {
        return root;
    }
}
