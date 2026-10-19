package com.sptv.mavenproject2.ui;

import com.sptv.mavenproject2.dto.ClienteExpediente;
import com.sptv.mavenproject2.service.ClienteService;
import com.sptv.mavenproject2.service.ValidationException;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import modelo.Cliente;

/**
 * Vista de Directorio y Edición Rápida de Clientes.
 * Reemplaza a Tabladeclientes (Swing), incluyendo edición en celda y panel rápido.
 */
public class ClientesTableView {

    private final ClienteService clienteService;
    private final Consumer<ClienteExpediente> onEditIntegralRequested;
    private final Consumer<String> onViewRucDossierRequested;
    private final BorderPane root;

    private final ObservableList<ClienteExpediente> masterList = FXCollections.observableArrayList();
    private FilteredList<ClienteExpediente> filteredList;
    private TableView<ClienteExpediente> table;

    private Label feedbackLabel;
    private HBox feedbackBanner;

    private TextField txtQuickNombre;
    private TextField txtQuickCorreo;
    private TextField txtQuickHonorarios;
    private TextField txtQuickNumeroCliente;
    private ComboBox<String> cmbQuickRegimen;
    private TextField txtQuickTelefono;
    private ClienteExpediente selectedExpediente;

    public ClientesTableView(
            ClienteService clienteService,
            Consumer<ClienteExpediente> onEditIntegralRequested,
            Consumer<String> onViewRucDossierRequested) {
        this.clienteService = clienteService;
        this.onEditIntegralRequested = onEditIntegralRequested;
        this.onViewRucDossierRequested = onViewRucDossierRequested;
        this.root = buildView();
        recargarDatos();
    }

    private BorderPane buildView() {
        BorderPane container = new BorderPane();
        container.setPadding(new Insets(20));

        VBox topSection = new VBox(12);
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Directorio y Tabla General de Clientes");
        title.getStyleClass().add("heading-lg");
        Label subtitle = new Label(
                "Edite directamente haciendo doble clic en una celda editable o utilice la barra de edición rápida inferior.");
        subtitle.getStyleClass().add("text-muted");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField txtFiltro = new TextField();
        txtFiltro.setPromptText("Filtrar por nombre, RUC, régimen o correo...");
        txtFiltro.setPrefWidth(280);

        Button btnRefrescar = new Button("↻ Actualizar Tabla");
        btnRefrescar.getStyleClass().add("btn-secondary");
        btnRefrescar.setOnAction(e -> recargarDatos());

        headerRow.getChildren().addAll(titleBox, spacer, txtFiltro, btnRefrescar);

        feedbackLabel = new Label();
        feedbackBanner = UiFeedback.createFeedbackBanner(feedbackLabel);

        topSection.getChildren().addAll(headerRow, feedbackBanner);
        topSection.setPadding(new Insets(0, 0, 12, 0));
        container.setTop(topSection);

        // Configuración de la tabla editable
        table = new TableView<>();
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<ClienteExpediente, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getCliente().getIdcliente() != null
                        ? String.valueOf(cd.getValue().getCliente().getIdcliente())
                        : ""));
        colId.setMaxWidth(60);

        TableColumn<ClienteExpediente, String> colNombre = new TableColumn<>("Nombre / Razón Social ✎");
        colNombre.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCliente().getNombre()));
        colNombre.setCellFactory(TextFieldTableCell.forTableColumn());
        colNombre.setOnEditCommit(event -> {
            ClienteExpediente exp = event.getRowValue();
            guardarCambioCelda(exp, event.getNewValue(), exp.getCliente().getCorreo(),
                    exp.getCliente().getHonorarios(), exp.getCliente().getNumerodecliente(),
                    exp.getCliente().getRegimen(), exp.getCliente().getTelefono());
        });

        TableColumn<ClienteExpediente, String> colRuc = new TableColumn<>("RUC");
        colRuc.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCliente().getRuc()));
        colRuc.setMaxWidth(120);

        TableColumn<ClienteExpediente, String> colRegimen = new TableColumn<>("Régimen ✎");
        colRegimen.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCliente().getRegimen()));
        colRegimen.setCellFactory(TextFieldTableCell.forTableColumn());
        colRegimen.setOnEditCommit(event -> {
            ClienteExpediente exp = event.getRowValue();
            guardarCambioCelda(exp, exp.getCliente().getNombre(), exp.getCliente().getCorreo(),
                    exp.getCliente().getHonorarios(), exp.getCliente().getNumerodecliente(),
                    event.getNewValue(), exp.getCliente().getTelefono());
        });

        TableColumn<ClienteExpediente, String> colHonorarios = new TableColumn<>("Honorarios (S/) ✎");
        colHonorarios.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getCliente().getHonorarios() != null
                        ? cd.getValue().getCliente().getHonorarios().toPlainString()
                        : "0.00"));
        colHonorarios.setCellFactory(TextFieldTableCell.forTableColumn());
        colHonorarios.setOnEditCommit(event -> {
            ClienteExpediente exp = event.getRowValue();
            try {
                BigDecimal nuevoMonto = new BigDecimal(event.getNewValue().trim().replace(",", "."));
                guardarCambioCelda(exp, exp.getCliente().getNombre(), exp.getCliente().getCorreo(),
                        nuevoMonto, exp.getCliente().getNumerodecliente(),
                        exp.getCliente().getRegimen(), exp.getCliente().getTelefono());
            } catch (NumberFormatException ex) {
                UiFeedback.showBannerError(feedbackBanner, feedbackLabel,
                        "El monto de honorarios debe ser un número decimal válido.");
                table.refresh();
            }
        });

        TableColumn<ClienteExpediente, String> colCorreo = new TableColumn<>("Correo ✎");
        colCorreo.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCliente().getCorreo()));
        colCorreo.setCellFactory(TextFieldTableCell.forTableColumn());
        colCorreo.setOnEditCommit(event -> {
            ClienteExpediente exp = event.getRowValue();
            guardarCambioCelda(exp, exp.getCliente().getNombre(), event.getNewValue(),
                    exp.getCliente().getHonorarios(), exp.getCliente().getNumerodecliente(),
                    exp.getCliente().getRegimen(), exp.getCliente().getTelefono());
        });

        TableColumn<ClienteExpediente, String> colTelefono = new TableColumn<>("Teléfono ✎");
        colTelefono.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCliente().getTelefono()));
        colTelefono.setCellFactory(TextFieldTableCell.forTableColumn());
        colTelefono.setOnEditCommit(event -> {
            ClienteExpediente exp = event.getRowValue();
            guardarCambioCelda(exp, exp.getCliente().getNombre(), exp.getCliente().getCorreo(),
                    exp.getCliente().getHonorarios(), exp.getCliente().getNumerodecliente(),
                    exp.getCliente().getRegimen(), event.getNewValue());
        });

        TableColumn<ClienteExpediente, String> colNumCliente = new TableColumn<>("N° Cliente ✎");
        colNumCliente.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCliente().getNumerodecliente()));
        colNumCliente.setCellFactory(TextFieldTableCell.forTableColumn());
        colNumCliente.setOnEditCommit(event -> {
            ClienteExpediente exp = event.getRowValue();
            guardarCambioCelda(exp, exp.getCliente().getNombre(), exp.getCliente().getCorreo(),
                    exp.getCliente().getHonorarios(), event.getNewValue(),
                    exp.getCliente().getRegimen(), exp.getCliente().getTelefono());
        });

        TableColumn<ClienteExpediente, String> colInicioAct = new TableColumn<>("Inicio Act.");
        colInicioAct.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getCliente().getFechainicioact() != null
                        ? cd.getValue().getCliente().getFechainicioact().toString()
                        : "-"));

        TableColumn<ClienteExpediente, String> colPrimeraDec = new TableColumn<>("1ra Decl.");
        colPrimeraDec.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getCliente().getPrimeradec() != null
                        ? cd.getValue().getCliente().getPrimeradec().toString()
                        : "-"));

        TableColumn<ClienteExpediente, String> colEstadoCobranza = new TableColumn<>("Cobranza");
        colEstadoCobranza.setCellValueFactory(cd -> {
            if (cd.getValue().getCobranza() == null) {
                return new SimpleStringProperty("Sin registro");
            }
            boolean alDia = Boolean.TRUE.equals(cd.getValue().getCobranza().getEstadototal());
            return new SimpleStringProperty(alDia ? "AL DÍA" : "PENDIENTE");
        });

        table.getColumns().setAll(List.of(
                colId, colNombre, colRuc, colRegimen, colHonorarios,
                colCorreo, colTelefono, colNumCliente, colInicioAct, colPrimeraDec, colEstadoCobranza
        ));

        filteredList = new FilteredList<>(masterList, p -> true);
        txtFiltro.textProperty().addListener((obs, oldV, newV) -> {
            String query = newV == null ? "" : newV.trim().toLowerCase();
            filteredList.setPredicate(exp -> {
                if (query.isEmpty()) {
                    return true;
                }
                Cliente c = exp.getCliente();
                return (c.getNombre() != null && c.getNombre().toLowerCase().contains(query))
                        || (c.getRuc() != null && c.getRuc().toLowerCase().contains(query))
                        || (c.getRegimen() != null && c.getRegimen().toLowerCase().contains(query))
                        || (c.getCorreo() != null && c.getCorreo().toLowerCase().contains(query));
            });
        });
        table.setItems(filteredList);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            cargarSeleccionEnPanelRapido(newSel);
        });

        container.setCenter(table);

        // Panel inferior de edición rápida + acciones sobre el cliente seleccionado
        VBox bottomCard = new VBox(12);
        bottomCard.getStyleClass().add("surface-card-compact");
        BorderPane.setMargin(bottomCard, new Insets(12, 0, 0, 0));

        Label lblQuickTitle = new Label("Edición Rápida de Fila Seleccionada (Nombre, Correo, Honorarios, N° Cliente, Régimen y Teléfono)");
        lblQuickTitle.getStyleClass().add("heading-md");

        GridPane quickGrid = new GridPane();
        quickGrid.setHgap(12);
        quickGrid.setVgap(8);

        txtQuickNombre = new TextField();
        txtQuickNombre.setPromptText("Nombre");
        txtQuickCorreo = new TextField();
        txtQuickCorreo.setPromptText("Correo");
        txtQuickHonorarios = new TextField();
        txtQuickHonorarios.setPromptText("Honorarios");
        txtQuickNumeroCliente = new TextField();
        txtQuickNumeroCliente.setPromptText("N° Cliente");
        cmbQuickRegimen = new ComboBox<>(FXCollections.observableArrayList(ClienteService.REGIMENES_DISPONIBLES));
        cmbQuickRegimen.setEditable(true);
        txtQuickTelefono = new TextField();
        txtQuickTelefono.setPromptText("Teléfono");

        quickGrid.add(UiFeedback.createFieldLabel("Nombre:"), 0, 0);
        quickGrid.add(txtQuickNombre, 1, 0);
        quickGrid.add(UiFeedback.createFieldLabel("Correo:"), 2, 0);
        quickGrid.add(txtQuickCorreo, 3, 0);
        quickGrid.add(UiFeedback.createFieldLabel("Honorarios (S/):"), 4, 0);
        quickGrid.add(txtQuickHonorarios, 5, 0);

        quickGrid.add(UiFeedback.createFieldLabel("Régimen:"), 0, 1);
        quickGrid.add(cmbQuickRegimen, 1, 1);
        quickGrid.add(UiFeedback.createFieldLabel("Teléfono:"), 2, 1);
        quickGrid.add(txtQuickTelefono, 3, 1);
        quickGrid.add(UiFeedback.createFieldLabel("N° Cliente:"), 4, 1);
        quickGrid.add(txtQuickNumeroCliente, 5, 1);

        HBox quickButtons = new HBox(10);
        quickButtons.setAlignment(Pos.CENTER_RIGHT);

        Button btnGuardarRapido = new Button("Guardar Edición Rápida");
        btnGuardarRapido.getStyleClass().add("btn-primary");
        btnGuardarRapido.setOnAction(e -> aplicarEdicionRapidaPanel());

        Button btnVerFichaRuc = new Button("Consultar Expediente por RUC");
        btnVerFichaRuc.getStyleClass().add("btn-secondary");
        btnVerFichaRuc.setOnAction(e -> {
            if (selectedExpediente != null && onViewRucDossierRequested != null) {
                onViewRucDossierRequested.accept(selectedExpediente.getCliente().getRuc());
            } else {
                UiFeedback.showBannerInfo(feedbackBanner, feedbackLabel, "Seleccione primero un cliente en la tabla.");
            }
        });

        Button btnEditarIntegral = new Button("Abrir Edición Integral (SOL / Libros / Cobranza)");
        btnEditarIntegral.getStyleClass().add("btn-secondary");
        btnEditarIntegral.setOnAction(e -> {
            if (selectedExpediente != null && onEditIntegralRequested != null) {
                onEditIntegralRequested.accept(selectedExpediente);
            } else {
                UiFeedback.showBannerInfo(feedbackBanner, feedbackLabel, "Seleccione primero un cliente en la tabla.");
            }
        });

        quickButtons.getChildren().addAll(btnVerFichaRuc, btnEditarIntegral, btnGuardarRapido);
        bottomCard.getChildren().addAll(lblQuickTitle, quickGrid, quickButtons);
        container.setBottom(bottomCard);

        return container;
    }

    private void cargarSeleccionEnPanelRapido(ClienteExpediente exp) {
        this.selectedExpediente = exp;
        if (exp == null || exp.getCliente() == null) {
            txtQuickNombre.clear();
            txtQuickCorreo.clear();
            txtQuickHonorarios.clear();
            txtQuickNumeroCliente.clear();
            txtQuickTelefono.clear();
            return;
        }
        Cliente c = exp.getCliente();
        txtQuickNombre.setText(c.getNombre());
        txtQuickCorreo.setText(c.getCorreo());
        txtQuickHonorarios.setText(c.getHonorarios() != null ? c.getHonorarios().toPlainString() : "0.00");
        txtQuickNumeroCliente.setText(c.getNumerodecliente());
        cmbQuickRegimen.setValue(c.getRegimen());
        txtQuickTelefono.setText(c.getTelefono());
    }

    private void aplicarEdicionRapidaPanel() {
        if (selectedExpediente == null) {
            UiFeedback.showBannerError(feedbackBanner, feedbackLabel,
                    "Seleccione una fila de la tabla para aplicar la edición rápida.");
            return;
        }
        try {
            BigDecimal monto = new BigDecimal(txtQuickHonorarios.getText().trim().replace(",", "."));
            String regimenVal = cmbQuickRegimen.getEditor().getText() != null && !cmbQuickRegimen.getEditor().getText().isBlank()
                    ? cmbQuickRegimen.getEditor().getText()
                    : cmbQuickRegimen.getValue();
            guardarCambioCelda(
                    selectedExpediente,
                    txtQuickNombre.getText(),
                    txtQuickCorreo.getText(),
                    monto,
                    txtQuickNumeroCliente.getText(),
                    regimenVal,
                    txtQuickTelefono.getText()
            );
        } catch (NumberFormatException nfe) {
            UiFeedback.showBannerError(feedbackBanner, feedbackLabel,
                    "El campo Honorarios debe ser un número decimal válido.");
        }
    }

    private void guardarCambioCelda(
            ClienteExpediente exp,
            String nombre,
            String correo,
            BigDecimal honorarios,
            String numeroCliente,
            String regimen,
            String telefono) {
        try {
            clienteService.actualizarDatosRapidosEnTabla(
                    exp.getCliente().getIdcliente(),
                    nombre,
                    correo,
                    honorarios,
                    numeroCliente,
                    regimen,
                    telefono
            );
            recargarDatos();
            UiFeedback.showBannerSuccess(feedbackBanner, feedbackLabel,
                    "Datos del cliente «" + nombre + "» actualizados y guardados en la base de datos.");
        } catch (ValidationException ve) {
            UiFeedback.showBannerError(feedbackBanner, feedbackLabel, ve.getMessage());
            table.refresh();
        } catch (Exception ex) {
            UiFeedback.showBannerError(feedbackBanner, feedbackLabel,
                    "Error al guardar cambios: " + ex.getMessage());
            table.refresh();
        }
    }

    public final void recargarDatos() {
        List<ClienteExpediente> expedientes = clienteService.listarExpedientes();
        masterList.setAll(expedientes);
    }

    public Parent getView() {
        return root;
    }
}
