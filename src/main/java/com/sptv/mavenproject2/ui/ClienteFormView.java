package com.sptv.mavenproject2.ui;

import com.sptv.mavenproject2.dto.ClienteExpediente;
import com.sptv.mavenproject2.dto.ClienteFormularioDto;
import com.sptv.mavenproject2.service.ClienteService;
import com.sptv.mavenproject2.service.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
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

/**
 * Vista unificada de Registro y Edición Integral de Clientes.
 * Reemplaza y corrige Addcliente y Editcliente de la versión Swing.
 */
public class ClienteFormView {

    private final ClienteService clienteService;
    private final Runnable onDataChanged;
    private final ScrollPane root;

    private boolean modoEdicion = false;
    private Long idClienteActual = null;

    private Label lblTituloVista;
    private Label lblSubtituloVista;
    private HBox boxBusquedaEdicion;
    private TextField txtBuscarRucEdicion;

    private Label feedbackLabel;
    private HBox feedbackBanner;

    private TextField txtNombre;
    private TextField txtRuc;
    private ComboBox<String> cmbRegimen;
    private DatePicker dpInicioActividades;
    private DatePicker dpPrimeraDeclaracion;
    private TextField txtHonorarios;
    private TextField txtCorreo;
    private TextField txtTelefono;
    private TextField txtNumeroCliente;

    private TextField txtUsuarioSol;
    private TextField txtClaveSol;

    private CheckBox chkRegistroVentas;
    private CheckBox chkRegistroCompras;
    private CheckBox chkDiarioSimplificado;

    private CheckBox chkCobranzaMensual;
    private CheckBox chkCobranzaAnual;
    private CheckBox chkCobranzaTotal;

    private Button btnGuardar;
    private Button btnEliminar;

    public ClienteFormView(ClienteService clienteService, Runnable onDataChanged) {
        this.clienteService = clienteService;
        this.onDataChanged = onDataChanged;
        this.root = buildView();
    }

    private ScrollPane buildView() {
        VBox mainLayout = new VBox(16);
        mainLayout.setPadding(new Insets(20));

        // Cabecera del módulo
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        VBox titleBox = new VBox(4);
        lblTituloVista = new Label("Registrar Nuevo Cliente");
        lblTituloVista.getStyleClass().add("heading-lg");
        lblSubtituloVista = new Label(
                "Registra los datos tributarios, credenciales SOL, libros obligatorios, estado de cobranza y genera el cronograma 2024–2030.");
        lblSubtituloVista.getStyleClass().add("text-muted");
        titleBox.getChildren().addAll(lblTituloVista, lblSubtituloVista);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnModoNuevo = new Button("+ Modo Nuevo Cliente");
        btnModoNuevo.getStyleClass().add("btn-secondary");
        btnModoNuevo.setOnAction(e -> configurarModoNuevo());

        Button btnModoEditar = new Button("✎ Modo Editar por RUC");
        btnModoEditar.getStyleClass().add("btn-secondary");
        btnModoEditar.setOnAction(e -> configurarModoEdicionVacio());

        headerRow.getChildren().addAll(titleBox, spacer, btnModoNuevo, btnModoEditar);

        // Barra de búsqueda por RUC para modo edición (equivalente a Editcliente)
        boxBusquedaEdicion = new HBox(10);
        boxBusquedaEdicion.getStyleClass().add("surface-card-compact");
        boxBusquedaEdicion.setAlignment(Pos.CENTER_LEFT);
        Label lblBuscarRuc = UiFeedback.createFieldLabel("Cargar expediente por RUC (11 dígitos):");
        txtBuscarRucEdicion = new TextField();
        txtBuscarRucEdicion.setPromptText("Ej. 20123456789");
        txtBuscarRucEdicion.setPrefWidth(200);
        Button btnCargarPorRuc = new Button("Cargar Cliente");
        btnCargarPorRuc.getStyleClass().add("btn-primary");
        btnCargarPorRuc.setOnAction(e -> cargarPorRucDesdeBarra());
        boxBusquedaEdicion.getChildren().addAll(lblBuscarRuc, txtBuscarRucEdicion, btnCargarPorRuc);
        boxBusquedaEdicion.setVisible(false);
        boxBusquedaEdicion.setManaged(false);

        feedbackLabel = new Label();
        feedbackBanner = UiFeedback.createFeedbackBanner(feedbackLabel);

        // Tarjeta 1: Datos Generales y Tributarios del Cliente
        VBox cardDatosCliente = new VBox(14);
        cardDatosCliente.getStyleClass().add("surface-card");
        Label secDatosTitle = new Label("1. Identificación y Datos Tributarios del Cliente");
        secDatosTitle.getStyleClass().add("heading-md");

        GridPane gridCliente = new GridPane();
        gridCliente.setHgap(16);
        gridCliente.setVgap(12);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(33.33);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(33.33);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(33.33);
        gridCliente.getColumnConstraints().addAll(col1, col2, col3);

        txtNombre = new TextField();
        txtNombre.setPromptText("Razón social o apellidos y nombres");

        txtRuc = new TextField();
        txtRuc.setPromptText("11 dígitos numéricos");

        cmbRegimen = new ComboBox<>(FXCollections.observableArrayList(ClienteService.REGIMENES_DISPONIBLES));
        cmbRegimen.setEditable(true);
        cmbRegimen.getSelectionModel().selectFirst();
        cmbRegimen.setMaxWidth(Double.MAX_VALUE);

        dpInicioActividades = new DatePicker(LocalDate.now());
        dpInicioActividades.setMaxWidth(Double.MAX_VALUE);

        dpPrimeraDeclaracion = new DatePicker(LocalDate.now());
        dpPrimeraDeclaracion.setMaxWidth(Double.MAX_VALUE);

        txtHonorarios = new TextField("150.00");
        txtHonorarios.setPromptText("Ej. 250.00");

        txtCorreo = new TextField();
        txtCorreo.setPromptText("contacto@empresa.pe");

        txtTelefono = new TextField();
        txtTelefono.setPromptText("9 dígitos o teléfono fijo");

        txtNumeroCliente = new TextField();
        txtNumeroCliente.setPromptText("Código / N° de contacto (opcional)");

        gridCliente.add(createFieldBox("Nombre / Razón Social *", txtNombre), 0, 0);
        gridCliente.add(createFieldBox("RUC (11 dígitos) *", txtRuc), 1, 0);
        gridCliente.add(createFieldBox("Régimen Tributario *", cmbRegimen), 2, 0);

        gridCliente.add(createFieldBox("Fecha Inicio de Actividades *", dpInicioActividades), 0, 1);
        gridCliente.add(createFieldBox("Fecha Primera Declaración *", dpPrimeraDeclaracion), 1, 1);
        gridCliente.add(createFieldBox("Honorarios Mensuales (S/) *", txtHonorarios), 2, 1);

        gridCliente.add(createFieldBox("Correo Electrónico *", txtCorreo), 0, 2);
        gridCliente.add(createFieldBox("Teléfono *", txtTelefono), 1, 2);
        gridCliente.add(createFieldBox("N° de Cliente / Contacto Alterno", txtNumeroCliente), 2, 2);

        cardDatosCliente.getChildren().addAll(secDatosTitle, new Separator(), gridCliente);

        // Tarjeta 2: Credenciales Clave SOL, Libros Contables y Estado de Cobranza
        GridPane bottomGrid = new GridPane();
        bottomGrid.setHgap(16);
        bottomGrid.setVgap(16);
        bottomGrid.getColumnConstraints().addAll(col1, col2, col3);

        // Subtarjeta Clave SOL
        VBox cardClaveSol = new VBox(12);
        cardClaveSol.getStyleClass().add("surface-card");
        Label lblSolTitle = new Label("2. Credenciales Clave SOL");
        lblSolTitle.getStyleClass().add("heading-md");
        txtUsuarioSol = new TextField();
        txtUsuarioSol.setPromptText("Usuario SOL (SUNAT)");
        txtClaveSol = new TextField();
        txtClaveSol.setPromptText("Contraseña Clave SOL");
        cardClaveSol.getChildren().addAll(
                lblSolTitle,
                new Separator(),
                createFieldBox("Usuario Clave SOL *", txtUsuarioSol),
                createFieldBox("Clave SOL *", txtClaveSol)
        );

        // Subtarjeta Libros Contables
        VBox cardLibros = new VBox(12);
        cardLibros.getStyleClass().add("surface-card");
        Label lblLibrosTitle = new Label("3. Libros Contables");
        lblLibrosTitle.getStyleClass().add("heading-md");
        Label lblLibrosHint = new Label("Marque los libros con estado ACTIVO:");
        lblLibrosHint.getStyleClass().add("text-muted");
        chkRegistroVentas = new CheckBox("Registro de Ventas (ACTIVO)");
        chkRegistroCompras = new CheckBox("Registro de Compras (ACTIVO)");
        chkDiarioSimplificado = new CheckBox("Diario de Formato Simplificado (ACTIVO)");
        chkRegistroVentas.setSelected(true);
        chkRegistroCompras.setSelected(true);
        cardLibros.getChildren().addAll(
                lblLibrosTitle,
                new Separator(),
                lblLibrosHint,
                chkRegistroVentas,
                chkRegistroCompras,
                chkDiarioSimplificado
        );

        // Subtarjeta Cobranza
        VBox cardCobranza = new VBox(12);
        cardCobranza.getStyleClass().add("surface-card");
        Label lblCobranzaTitle = new Label("4. Estado de Cobranza");
        lblCobranzaTitle.getStyleClass().add("heading-md");
        Label lblCobranzaHint = new Label("Marque cuando el cliente se encuentre AL DÍA:");
        lblCobranzaHint.getStyleClass().add("text-muted");
        chkCobranzaMensual = new CheckBox("Cobranza Mensual al día");
        chkCobranzaAnual = new CheckBox("Cobranza Anual al día");
        chkCobranzaTotal = new CheckBox("Estado Total al día");
        chkCobranzaMensual.setSelected(true);
        chkCobranzaAnual.setSelected(true);
        chkCobranzaTotal.setSelected(true);
        cardCobranza.getChildren().addAll(
                lblCobranzaTitle,
                new Separator(),
                lblCobranzaHint,
                chkCobranzaMensual,
                chkCobranzaAnual,
                chkCobranzaTotal
        );

        bottomGrid.add(cardClaveSol, 0, 0);
        bottomGrid.add(cardLibros, 1, 0);
        bottomGrid.add(cardCobranza, 2, 0);

        // Barra inferior de acciones
        HBox actionBar = new HBox(12);
        actionBar.setAlignment(Pos.CENTER_RIGHT);

        Button btnLimpiar = new Button("Limpiar Campos");
        btnLimpiar.getStyleClass().add("btn-secondary");
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        btnEliminar = new Button("Eliminar Cliente y Registros");
        btnEliminar.getStyleClass().add("btn-danger");
        btnEliminar.setVisible(false);
        btnEliminar.setManaged(false);
        btnEliminar.setOnAction(e -> ejecutarEliminacion());

        btnGuardar = new Button("Registrar Cliente Completo");
        btnGuardar.getStyleClass().add("btn-primary");
        btnGuardar.setOnAction(e -> ejecutarGuardado());

        actionBar.getChildren().addAll(btnLimpiar, btnEliminar, btnGuardar);

        mainLayout.getChildren().addAll(
                headerRow,
                boxBusquedaEdicion,
                feedbackBanner,
                cardDatosCliente,
                bottomGrid,
                actionBar
        );

        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }

    private VBox createFieldBox(String labelText, javafx.scene.Node input) {
        VBox box = new VBox(6);
        box.getChildren().addAll(UiFeedback.createFieldLabel(labelText), input);
        return box;
    }

    public void configurarModoNuevo() {
        this.modoEdicion = false;
        this.idClienteActual = null;
        lblTituloVista.setText("Registrar Nuevo Cliente");
        lblSubtituloVista.setText(
                "Registra los datos tributarios, credenciales SOL, libros obligatorios, estado de cobranza y genera el cronograma 2024–2030.");
        boxBusquedaEdicion.setVisible(false);
        boxBusquedaEdicion.setManaged(false);
        btnGuardar.setText("Registrar Cliente Completo");
        btnEliminar.setVisible(false);
        btnEliminar.setManaged(false);
        feedbackBanner.setVisible(false);
        feedbackBanner.setManaged(false);
        limpiarFormulario();
    }

    public void configurarModoEdicionVacio() {
        this.modoEdicion = true;
        this.idClienteActual = null;
        lblTituloVista.setText("Editar / Eliminar Cliente Existente");
        lblSubtituloVista.setText(
                "Busque un cliente por su RUC de 11 dígitos para modificar su ficha integral o eliminar su expediente.");
        boxBusquedaEdicion.setVisible(true);
        boxBusquedaEdicion.setManaged(true);
        btnGuardar.setText("Guardar Cambios del Cliente");
        btnEliminar.setVisible(true);
        btnEliminar.setManaged(true);
    }

    public void cargarExpedienteParaEdicion(ClienteExpediente expediente) {
        configurarModoEdicionVacio();
        if (expediente == null || expediente.getCliente() == null) {
            return;
        }
        this.idClienteActual = expediente.getCliente().getIdcliente();
        txtBuscarRucEdicion.setText(expediente.getCliente().getRuc());

        ClienteFormularioDto dto = clienteService.toFormularioDto(expediente);
        txtNombre.setText(dto.getNombre());
        txtRuc.setText(dto.getRuc());
        cmbRegimen.setValue(dto.getRegimen());
        dpInicioActividades.setValue(dto.getFechaInicioActividades());
        dpPrimeraDeclaracion.setValue(dto.getFechaPrimeraDeclaracion());
        txtHonorarios.setText(dto.getHonorarios() != null ? dto.getHonorarios().toPlainString() : "0.00");
        txtCorreo.setText(dto.getCorreo());
        txtTelefono.setText(dto.getTelefono());
        txtNumeroCliente.setText(dto.getNumeroDeCliente());

        txtUsuarioSol.setText(dto.getUsuarioClaveSol() != null ? dto.getUsuarioClaveSol() : "");
        txtClaveSol.setText(dto.getClaveSolPassword() != null ? dto.getClaveSolPassword() : "");

        chkRegistroVentas.setSelected(dto.isRegistroVentasActivo());
        chkRegistroCompras.setSelected(dto.isRegistroComprasActivo());
        chkDiarioSimplificado.setSelected(dto.isDiarioSimplificadoActivo());

        chkCobranzaMensual.setSelected(dto.isEstadoMensualAlDia());
        chkCobranzaAnual.setSelected(dto.isEstadoAnualAlDia());
        chkCobranzaTotal.setSelected(dto.isEstadoTotalAlDia());

        UiFeedback.showBannerInfo(feedbackBanner, feedbackLabel,
                "Expediente cargado para edición: " + dto.getNombre() + " (ID: " + idClienteActual + ", RUC: " + dto.getRuc() + ").");
    }

    private void cargarPorRucDesdeBarra() {
        try {
            ClienteExpediente exp = clienteService.buscarExpedientePorRuc(txtBuscarRucEdicion.getText());
            cargarExpedienteParaEdicion(exp);
        } catch (ValidationException ve) {
            UiFeedback.showBannerError(feedbackBanner, feedbackLabel, ve.getMessage());
        } catch (Exception ex) {
            UiFeedback.showBannerError(feedbackBanner, feedbackLabel,
                    "Error al buscar cliente por RUC: " + ex.getMessage());
        }
    }

    private void ejecutarGuardado() {
        try {
            ClienteFormularioDto dto = construirDtoDesdeCampos();
            if (!modoEdicion) {
                ClienteExpediente creado = clienteService.registrarClienteIntegral(dto);
                UiFeedback.showBannerSuccess(feedbackBanner, feedbackLabel,
                        "Cliente «" + creado.getCliente().getNombre() + "» registrado exitosamente con sus 84 cuotas de pagos (2024–2030).");
                limpiarFormulario();
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            } else {
                if (idClienteActual == null) {
                    throw new ValidationException("Primero busque y cargue un cliente por RUC antes de guardar cambios.");
                }
                ClienteExpediente actualizado = clienteService.actualizarClienteIntegral(idClienteActual, dto);
                UiFeedback.showBannerSuccess(feedbackBanner, feedbackLabel,
                        "Expediente del cliente «" + actualizado.getCliente().getNombre() + "» actualizado correctamente.");
                if (onDataChanged != null) {
                    onDataChanged.run();
                }
            }
        } catch (ValidationException ve) {
            UiFeedback.showBannerError(feedbackBanner, feedbackLabel, ve.getMessage());
        } catch (Exception ex) {
            UiFeedback.showBannerError(feedbackBanner, feedbackLabel,
                    "Error al procesar operación: " + ex.getMessage());
        }
    }

    private void ejecutarEliminacion() {
        if (idClienteActual == null) {
            UiFeedback.showBannerError(feedbackBanner, feedbackLabel,
                    "Primero debe cargar un cliente existente para poder eliminarlo.");
            return;
        }
        String nombre = txtNombre.getText();
        String ruc = txtRuc.getText();
        boolean confirmado = UiFeedback.confirmAction(
                "Confirmar Eliminación Integral",
                "¿Eliminar cliente «" + nombre + "» (RUC: " + ruc + ")?",
                "Se eliminarán de manera permanente sus credenciales Clave SOL, libros contables, estado de cobranza y cronograma de pagos 2024–2030."
        );
        if (!confirmado) {
            return;
        }
        try {
            clienteService.eliminarClienteIntegral(idClienteActual);
            idClienteActual = null;
            limpiarFormulario();
            UiFeedback.showBannerSuccess(feedbackBanner, feedbackLabel,
                    "El cliente «" + nombre + "» y todos sus registros dependientes fueron eliminados.");
            if (onDataChanged != null) {
                onDataChanged.run();
            }
        } catch (ValidationException ve) {
            UiFeedback.showBannerError(feedbackBanner, feedbackLabel, ve.getMessage());
        } catch (Exception ex) {
            UiFeedback.showBannerError(feedbackBanner, feedbackLabel,
                    "Error al eliminar cliente: " + ex.getMessage());
        }
    }

    private ClienteFormularioDto construirDtoDesdeCampos() {
        BigDecimal honorarios;
        try {
            String rawHonorarios = txtHonorarios.getText() != null
                    ? txtHonorarios.getText().trim().replace(",", ".")
                    : "";
            honorarios = new BigDecimal(rawHonorarios);
        } catch (NumberFormatException nfe) {
            throw new ValidationException("El campo Honorarios debe ser un número decimal válido (ej. 250.00).");
        }

        ClienteFormularioDto dto = new ClienteFormularioDto();
        dto.setNombre(txtNombre.getText());
        dto.setRuc(txtRuc.getText());
        dto.setRegimen(cmbRegimen.getEditor().getText() != null && !cmbRegimen.getEditor().getText().isBlank()
                ? cmbRegimen.getEditor().getText()
                : cmbRegimen.getValue());
        dto.setFechaInicioActividades(dpInicioActividades.getValue());
        dto.setFechaPrimeraDeclaracion(dpPrimeraDeclaracion.getValue());
        dto.setHonorarios(honorarios);
        dto.setCorreo(txtCorreo.getText());
        dto.setTelefono(txtTelefono.getText());
        dto.setNumeroDeCliente(txtNumeroCliente.getText());
        dto.setUsuarioClaveSol(txtUsuarioSol.getText());
        dto.setClaveSolPassword(txtClaveSol.getText());
        dto.setRegistroVentasActivo(chkRegistroVentas.isSelected());
        dto.setRegistroComprasActivo(chkRegistroCompras.isSelected());
        dto.setDiarioSimplificadoActivo(chkDiarioSimplificado.isSelected());
        dto.setEstadoMensualAlDia(chkCobranzaMensual.isSelected());
        dto.setEstadoAnualAlDia(chkCobranzaAnual.isSelected());
        dto.setEstadoTotalAlDia(chkCobranzaTotal.isSelected());
        return dto;
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        txtRuc.clear();
        cmbRegimen.getSelectionModel().selectFirst();
        dpInicioActividades.setValue(LocalDate.now());
        dpPrimeraDeclaracion.setValue(LocalDate.now());
        txtHonorarios.setText("150.00");
        txtCorreo.clear();
        txtTelefono.clear();
        txtNumeroCliente.clear();
        txtUsuarioSol.clear();
        txtClaveSol.clear();
        chkRegistroVentas.setSelected(true);
        chkRegistroCompras.setSelected(true);
        chkDiarioSimplificado.setSelected(false);
        chkCobranzaMensual.setSelected(true);
        chkCobranzaAnual.setSelected(true);
        chkCobranzaTotal.setSelected(true);
    }

    public Parent getView() {
        return root;
    }
}
