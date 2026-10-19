package com.sptv.mavenproject2.ui;

import com.sptv.mavenproject2.dto.ClienteExpediente;
import com.sptv.mavenproject2.service.ClienteService;
import com.sptv.mavenproject2.service.ValidationException;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import modelo.Clavesol;
import modelo.Cliente;
import modelo.Cobranza;
import modelo.Libro;

/**
 * Vista de Consulta Integral de Expediente por RUC.
 * Reemplaza a BuscarporRuc (Swing).
 */
public class BuscarRucView {

    private final ClienteService clienteService;
    private final Consumer<ClienteExpediente> onEditRequested;
    private final ScrollPane root;

    private TextField txtRucBusqueda;
    private Label feedbackLabel;
    private HBox feedbackBanner;

    private VBox resultContainer;
    private ClienteExpediente currentExpediente;

    private Label valIdCliente;
    private Label valNombre;
    private Label valRuc;
    private Label valRegimen;
    private Label valHonorarios;
    private Label valCorreo;
    private Label valTelefono;
    private Label valNumeroCliente;
    private Label valFechaInicio;
    private Label valFechaPrimeraDec;

    private Label valUsuarioSol;
    private Label valClaveSol;

    private Label valRegVentas;
    private Label valRegCompras;
    private Label valDiarioSimplificado;

    private Label valCobranzaMensual;
    private Label valCobranzaAnual;
    private Label valCobranzaTotal;

    public BuscarRucView(ClienteService clienteService, Consumer<ClienteExpediente> onEditRequested) {
        this.clienteService = clienteService;
        this.onEditRequested = onEditRequested;
        this.root = buildView();
    }

    private ScrollPane buildView() {
        VBox mainLayout = new VBox(16);
        mainLayout.setPadding(new Insets(20));

        Label title = new Label("Consulta de Expediente Integral por RUC");
        title.getStyleClass().add("heading-lg");
        Label subtitle = new Label(
                "Ingrese el RUC de 11 dígitos para consultar en una sola vista los datos tributarios, Clave SOL, libros contables y estado de cobranza.");
        subtitle.getStyleClass().add("text-muted");

        HBox searchCard = new HBox(12);
        searchCard.getStyleClass().add("surface-card-compact");
        searchCard.setAlignment(Pos.CENTER_LEFT);

        Label lblRuc = UiFeedback.createFieldLabel("Número de RUC (11 dígitos):");
        txtRucBusqueda = new TextField();
        txtRucBusqueda.setPromptText("Ej. 20123456789");
        txtRucBusqueda.setPrefWidth(220);

        Button btnBuscar = new Button("Buscar por RUC");
        btnBuscar.getStyleClass().add("btn-primary");
        btnBuscar.setDefaultButton(true);
        btnBuscar.setOnAction(e -> ejecutarBusqueda(txtRucBusqueda.getText()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnEditarExpediente = new Button("✎ Editar este Cliente");
        btnEditarExpediente.getStyleClass().add("btn-secondary");
        btnEditarExpediente.setOnAction(e -> {
            if (currentExpediente != null && onEditRequested != null) {
                onEditRequested.accept(currentExpediente);
            } else {
                UiFeedback.showBannerInfo(feedbackBanner, feedbackLabel,
                        "Primero realice una búsqueda por RUC para abrir la edición.");
            }
        });

        searchCard.getChildren().addAll(lblRuc, txtRucBusqueda, btnBuscar, spacer, btnEditarExpediente);

        feedbackLabel = new Label();
        feedbackBanner = UiFeedback.createFeedbackBanner(feedbackLabel);

        // Contenedor de resultados
        resultContainer = new VBox(16);

        // Tarjeta Datos del Cliente
        VBox cardCliente = new VBox(12);
        cardCliente.getStyleClass().add("surface-card");
        Label lblDatosTitle = new Label("Ficha Tributaria del Contribuyente");
        lblDatosTitle.getStyleClass().add("heading-md");

        GridPane gridDatos = new GridPane();
        gridDatos.setHgap(24);
        gridDatos.setVgap(12);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(25);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(25);
        ColumnConstraints c3 = new ColumnConstraints();
        c3.setPercentWidth(25);
        ColumnConstraints c4 = new ColumnConstraints();
        c4.setPercentWidth(25);
        gridDatos.getColumnConstraints().addAll(c1, c2, c3, c4);

        valIdCliente = createValueLabel("-");
        valNombre = createValueLabel("-");
        valRuc = createValueLabel("-");
        valRegimen = createValueLabel("-");
        valHonorarios = createValueLabel("-");
        valCorreo = createValueLabel("-");
        valTelefono = createValueLabel("-");
        valNumeroCliente = createValueLabel("-");
        valFechaInicio = createValueLabel("-");
        valFechaPrimeraDec = createValueLabel("-");

        gridDatos.add(createDetailItem("ID Cliente", valIdCliente), 0, 0);
        gridDatos.add(createDetailItem("Nombre / Razón Social", valNombre), 1, 0);
        gridDatos.add(createDetailItem("RUC", valRuc), 2, 0);
        gridDatos.add(createDetailItem("Régimen Tributario", valRegimen), 3, 0);

        gridDatos.add(createDetailItem("Honorarios Mensuales", valHonorarios), 0, 1);
        gridDatos.add(createDetailItem("Correo Electrónico", valCorreo), 1, 1);
        gridDatos.add(createDetailItem("Teléfono", valTelefono), 2, 1);
        gridDatos.add(createDetailItem("N° de Cliente", valNumeroCliente), 3, 1);

        gridDatos.add(createDetailItem("Inicio de Actividades", valFechaInicio), 0, 2);
        gridDatos.add(createDetailItem("Primera Declaración", valFechaPrimeraDec), 1, 2);

        cardCliente.getChildren().addAll(lblDatosTitle, new Separator(), gridDatos);

        // Grid inferior de 3 tarjetas: Clave SOL, Libros, Cobranza
        GridPane subGrid = new GridPane();
        subGrid.setHgap(16);
        subGrid.setVgap(16);
        ColumnConstraints sc1 = new ColumnConstraints();
        sc1.setPercentWidth(33.33);
        ColumnConstraints sc2 = new ColumnConstraints();
        sc2.setPercentWidth(33.33);
        ColumnConstraints sc3 = new ColumnConstraints();
        sc3.setPercentWidth(33.33);
        subGrid.getColumnConstraints().addAll(sc1, sc2, sc3);

        VBox cardSol = new VBox(12);
        cardSol.getStyleClass().add("surface-card");
        Label lblSol = new Label("Credenciales Clave SOL");
        lblSol.getStyleClass().add("heading-md");
        valUsuarioSol = createValueLabel("-");
        valClaveSol = createValueLabel("-");
        cardSol.getChildren().addAll(
                lblSol,
                new Separator(),
                createDetailItem("Usuario SOL", valUsuarioSol),
                createDetailItem("Contraseña SOL", valClaveSol)
        );

        VBox cardLibros = new VBox(12);
        cardLibros.getStyleClass().add("surface-card");
        Label lblLibros = new Label("Libros Contables");
        lblLibros.getStyleClass().add("heading-md");
        valRegVentas = createValueLabel("-");
        valRegCompras = createValueLabel("-");
        valDiarioSimplificado = createValueLabel("-");
        cardLibros.getChildren().addAll(
                lblLibros,
                new Separator(),
                createDetailItem("Registro de Ventas", valRegVentas),
                createDetailItem("Registro de Compras", valRegCompras),
                createDetailItem("Diario Formato Simplificado", valDiarioSimplificado)
        );

        VBox cardCobranza = new VBox(12);
        cardCobranza.getStyleClass().add("surface-card");
        Label lblCobranza = new Label("Estado de Cobranza");
        lblCobranza.getStyleClass().add("heading-md");
        valCobranzaMensual = createValueLabel("-");
        valCobranzaAnual = createValueLabel("-");
        valCobranzaTotal = createValueLabel("-");
        cardCobranza.getChildren().addAll(
                lblCobranza,
                new Separator(),
                createDetailItem("Cobranza Mensual", valCobranzaMensual),
                createDetailItem("Cobranza Anual", valCobranzaAnual),
                createDetailItem("Estado Total", valCobranzaTotal)
        );

        subGrid.add(cardSol, 0, 0);
        subGrid.add(cardLibros, 1, 0);
        subGrid.add(cardCobranza, 2, 0);

        resultContainer.getChildren().addAll(cardCliente, subGrid);

        mainLayout.getChildren().addAll(
                new VBox(4, title, subtitle),
                searchCard,
                feedbackBanner,
                resultContainer
        );

        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }

    private Label createValueLabel(String initial) {
        Label lbl = new Label(initial);
        lbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #0F172A;");
        lbl.setWrapText(true);
        return lbl;
    }

    private VBox createDetailItem(String label, Label valueLabel) {
        VBox box = new VBox(4);
        box.getChildren().addAll(UiFeedback.createFieldLabel(label), valueLabel);
        return box;
    }

    public void ejecutarBusqueda(String rucInput) {
        txtRucBusqueda.setText(rucInput != null ? rucInput.trim() : "");
        try {
            ClienteExpediente exp = clienteService.buscarExpedientePorRuc(txtRucBusqueda.getText());
            this.currentExpediente = exp;
            renderExpediente(exp);
            UiFeedback.showBannerSuccess(feedbackBanner, feedbackLabel,
                    "Expediente localizado para RUC " + exp.getCliente().getRuc() + " (" + exp.getCliente().getNombre() + ").");
        } catch (ValidationException ve) {
            this.currentExpediente = null;
            UiFeedback.showBannerError(feedbackBanner, feedbackLabel, ve.getMessage());
        } catch (Exception ex) {
            this.currentExpediente = null;
            UiFeedback.showBannerError(feedbackBanner, feedbackLabel,
                    "Error al consultar RUC: " + ex.getMessage());
        }
    }

    private void renderExpediente(ClienteExpediente exp) {
        Cliente c = exp.getCliente();
        valIdCliente.setText(String.valueOf(c.getIdcliente()));
        valNombre.setText(c.getNombre());
        valRuc.setText(c.getRuc());
        valRegimen.setText(c.getRegimen());
        valHonorarios.setText("S/ " + (c.getHonorarios() != null ? c.getHonorarios().toPlainString() : "0.00"));
        valCorreo.setText(c.getCorreo());
        valTelefono.setText(c.getTelefono());
        valNumeroCliente.setText(c.getNumerodecliente());
        valFechaInicio.setText(c.getFechainicioact() != null ? c.getFechainicioact().toString() : "-");
        valFechaPrimeraDec.setText(c.getPrimeradec() != null ? c.getPrimeradec().toString() : "-");

        Clavesol sol = exp.getClavesol();
        if (sol != null) {
            valUsuarioSol.setText(sol.getUsuario());
            valClaveSol.setText(sol.getClave());
        } else {
            valUsuarioSol.setText("No registrado");
            valClaveSol.setText("No registrado");
        }

        Libro l = exp.getLibro();
        if (l != null) {
            valRegVentas.setText(l.getRegistrodeventas() != null ? l.getRegistrodeventas().name() : "NO_ACTIVO");
            valRegCompras.setText(l.getRegistrodecompras() != null ? l.getRegistrodecompras().name() : "NO_ACTIVO");
            valDiarioSimplificado.setText(l.getDiariodeformatosimplificado() != null
                    ? l.getDiariodeformatosimplificado().name()
                    : "NO_ACTIVO");
        } else {
            valRegVentas.setText("Sin registro");
            valRegCompras.setText("Sin registro");
            valDiarioSimplificado.setText("Sin registro");
        }

        Cobranza co = exp.getCobranza();
        if (co != null) {
            valCobranzaMensual.setText(Boolean.TRUE.equals(co.getEstadomensual()) ? "AL DÍA" : "DEBE / PENDIENTE");
            valCobranzaAnual.setText(Boolean.TRUE.equals(co.getEstadoanual()) ? "AL DÍA" : "DEBE / PENDIENTE");
            valCobranzaTotal.setText(Boolean.TRUE.equals(co.getEstadototal()) ? "AL DÍA" : "DEBE / PENDIENTE");
        } else {
            valCobranzaMensual.setText("Sin registro");
            valCobranzaAnual.setText("Sin registro");
            valCobranzaTotal.setText("Sin registro");
        }
    }

    public Parent getView() {
        return root;
    }
}
