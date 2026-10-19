package com.sptv.mavenproject2.ui;

import com.sptv.mavenproject2.dto.ClienteExpediente;
import com.sptv.mavenproject2.service.AuthService;
import com.sptv.mavenproject2.service.ClienteService;
import com.sptv.mavenproject2.service.PagoMensualService;
import java.math.BigDecimal;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import modelo.Usuariodelprograma;
import persistance.PersistenceManager;

/**
 * Panel Principal de Navegación y Resumen Ejecutivo de ServiSerc.
 * Reemplaza a PestañaPrincipal (Swing) articulando todas las vistas del sistema.
 */
public class MainDashboardView {

    private final AuthService authService;
    private final ClienteService clienteService;
    private final PagoMensualService pagoMensualService;
    private final Usuariodelprograma usuarioActivo;
    private final Runnable onLogout;

    private final BorderPane root;

    private Button btnNavResumen;
    private Button btnNavTablaClientes;
    private Button btnNavNuevoCliente;
    private Button btnNavEditarCliente;
    private Button btnNavBuscarRuc;
    private Button btnNavPagos;
    private Button btnNavUsuarios;

    private ClientesTableView clientesTableView;
    private ClienteFormView clienteFormView;
    private BuscarRucView buscarRucView;
    private PagosMatrixView pagosMatrixView;
    private UsuariosSeguridadView usuariosSeguridadView;

    private Label kpiTotalClientesVal;
    private Label kpiCobranzaAlDiaVal;
    private Label kpiHonorariosMensualesVal;
    private Label kpiUsuariosVal;
    private Label lblStatusResumen;

    public MainDashboardView(
            AuthService authService,
            ClienteService clienteService,
            PagoMensualService pagoMensualService,
            Usuariodelprograma usuarioActivo,
            Runnable onLogout) {
        this.authService = authService;
        this.clienteService = clienteService;
        this.pagoMensualService = pagoMensualService;
        this.usuarioActivo = usuarioActivo;
        this.onLogout = onLogout;
        this.root = buildView();
    }

    private BorderPane buildView() {
        BorderPane mainPane = new BorderPane();

        // Inicializar subvistas
        clientesTableView = new ClientesTableView(
                clienteService,
                this::abrirEdicionClienteConExpediente,
                this::abrirBusquedaConRuc
        );
        clienteFormView = new ClienteFormView(clienteService, this::sincronizarTodasLasVistas);
        buscarRucView = new BuscarRucView(clienteService, this::abrirEdicionClienteConExpediente);
        pagosMatrixView = new PagosMatrixView(pagoMensualService);
        usuariosSeguridadView = new UsuariosSeguridadView(authService);

        // Barra superior (Topbar)
        HBox topbar = new HBox(16);
        topbar.getStyleClass().add("topbar");
        topbar.setAlignment(Pos.CENTER_LEFT);

        Label appHeaderTitle = new Label("ServiSerc — Plataforma de Administración Tributaria y de Clientes");
        appHeaderTitle.getStyleClass().add("heading-md");

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        Label userBadge = new Label("Sesión activa: " + (usuarioActivo != null ? usuarioActivo.getUsuario() : "admin"));
        userBadge.getStyleClass().add("badge-ok");

        Button btnCerrarSesion = new Button("Cerrar Sesión");
        btnCerrarSesion.getStyleClass().add("btn-secondary");
        btnCerrarSesion.setOnAction(e -> {
            if (onLogout != null) {
                onLogout.run();
            }
        });

        topbar.getChildren().addAll(appHeaderTitle, topSpacer, userBadge, btnCerrarSesion);
        mainPane.setTop(topbar);

        // Barra lateral izquierda (Sidebar)
        VBox sidebar = new VBox(8);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(250);

        VBox brandBox = new VBox(4);
        brandBox.setPadding(new Insets(0, 8, 16, 8));
        Label brandTitle = new Label("SERVISERC");
        brandTitle.getStyleClass().add("sidebar-brand-title");
        Label brandSubtitle = new Label("Gestión Contable y SUNAT");
        brandSubtitle.getStyleClass().add("sidebar-brand-subtitle");
        brandBox.getChildren().addAll(brandTitle, brandSubtitle);

        btnNavResumen = createNavButton("⌂  Panel Principal");
        btnNavTablaClientes = createNavButton("☰  Tabla de Clientes");
        btnNavNuevoCliente = createNavButton("＋  Agregar Cliente");
        btnNavEditarCliente = createNavButton("✎  Editar / Eliminar Cliente");
        btnNavBuscarRuc = createNavButton("⌕  Buscar por RUC");
        btnNavPagos = createNavButton("S/  Tabla de Pagos (2024–2030)");
        btnNavUsuarios = createNavButton("⚙  Usuarios y Seguridad");

        Parent resumenPane = buildResumenHomePane();

        btnNavResumen.setOnAction(e -> {
            activarBotonNav(btnNavResumen);
            actualizarMetricasResumen();
            mainPane.setCenter(resumenPane);
        });

        btnNavTablaClientes.setOnAction(e -> {
            activarBotonNav(btnNavTablaClientes);
            clientesTableView.recargarDatos();
            mainPane.setCenter(clientesTableView.getView());
        });

        btnNavNuevoCliente.setOnAction(e -> {
            activarBotonNav(btnNavNuevoCliente);
            clienteFormView.configurarModoNuevo();
            mainPane.setCenter(clienteFormView.getView());
        });

        btnNavEditarCliente.setOnAction(e -> {
            activarBotonNav(btnNavEditarCliente);
            clienteFormView.configurarModoEdicionVacio();
            mainPane.setCenter(clienteFormView.getView());
        });

        btnNavBuscarRuc.setOnAction(e -> {
            activarBotonNav(btnNavBuscarRuc);
            mainPane.setCenter(buscarRucView.getView());
        });

        btnNavPagos.setOnAction(e -> {
            activarBotonNav(btnNavPagos);
            pagosMatrixView.recargarMatriz();
            mainPane.setCenter(pagosMatrixView.getView());
        });

        btnNavUsuarios.setOnAction(e -> {
            activarBotonNav(btnNavUsuarios);
            usuariosSeguridadView.bloquearPanelAdministracion();
            mainPane.setCenter(usuariosSeguridadView.getView());
        });

        sidebar.getChildren().addAll(
                brandBox,
                btnNavResumen,
                btnNavTablaClientes,
                btnNavNuevoCliente,
                btnNavEditarCliente,
                btnNavBuscarRuc,
                btnNavPagos,
                new Separator(),
                btnNavUsuarios
        );
        mainPane.setLeft(sidebar);

        // Barra de estado inferior (Statusbar)
        HBox statusbar = new HBox(16);
        statusbar.getStyleClass().add("statusbar");
        statusbar.setAlignment(Pos.CENTER_LEFT);

        Label lblMotor = new Label("Base de Datos: " + PersistenceManager.getActiveStatusDescription());
        lblMotor.getStyleClass().add("text-muted");

        Region statusSpacer = new Region();
        HBox.setHgrow(statusSpacer, Priority.ALWAYS);

        lblStatusResumen = new Label();
        lblStatusResumen.getStyleClass().add("text-muted");

        statusbar.getChildren().addAll(lblMotor, statusSpacer, lblStatusResumen);
        mainPane.setBottom(statusbar);

        // Vista inicial por defecto: Panel Principal
        activarBotonNav(btnNavResumen);
        actualizarMetricasResumen();
        mainPane.setCenter(resumenPane);

        return mainPane;
    }

    private Parent buildResumenHomePane() {
        VBox home = new VBox(20);
        home.setPadding(new Insets(24));

        VBox welcomeBox = new VBox(4);
        Label title = new Label("Panel Principal de Operaciones — ServiSerc");
        title.getStyleClass().add("heading-xl");
        Label subtitle = new Label(
                "Seleccione una operación o consulte las métricas consolidadas en tiempo real de la cartera de clientes.");
        subtitle.getStyleClass().add("text-muted");
        welcomeBox.getChildren().addAll(title, subtitle);

        // Fila de tarjetas KPI
        GridPane kpiGrid = new GridPane();
        kpiGrid.setHgap(16);
        kpiGrid.setVgap(16);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25);
            kpiGrid.getColumnConstraints().add(cc);
        }

        kpiTotalClientesVal = new Label("0");
        kpiTotalClientesVal.getStyleClass().add("kpi-value");
        kpiCobranzaAlDiaVal = new Label("0");
        kpiCobranzaAlDiaVal.getStyleClass().add("kpi-value");
        kpiHonorariosMensualesVal = new Label("S/ 0.00");
        kpiHonorariosMensualesVal.getStyleClass().add("kpi-value");
        kpiUsuariosVal = new Label("0");
        kpiUsuariosVal.getStyleClass().add("kpi-value");

        kpiGrid.add(createKpiCard("CLIENTES REGISTRADOS", kpiTotalClientesVal, "Total en cartera activa"), 0, 0);
        kpiGrid.add(createKpiCard("CLIENTES AL DÍA", kpiCobranzaAlDiaVal, "Estado de cobranza total al día"), 1, 0);
        kpiGrid.add(createKpiCard("HONORARIOS MENSUALES", kpiHonorariosMensualesVal, "Facturación mensual pactada"), 2, 0);
        kpiGrid.add(createKpiCard("USUARIOS DEL SISTEMA", kpiUsuariosVal, "Cuentas con acceso SHA-256"), 3, 0);

        // Tarjetas de acceso directo a los 6 módulos originales de PestañaPrincipal
        VBox modulesCard = new VBox(16);
        modulesCard.getStyleClass().add("surface-card");
        Label lblModTitle = new Label("Módulos Operativos de ServiSerc");
        lblModTitle.getStyleClass().add("heading-lg");

        GridPane modGrid = new GridPane();
        modGrid.setHgap(16);
        modGrid.setVgap(16);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(33.33);
            modGrid.getColumnConstraints().add(cc);
        }

        modGrid.add(createQuickModuleBox(
                "1. Tabla General de Clientes",
                "Visualice el padrón completo de clientes y edite rápidamente Nombre, Correo, Honorarios, Régimen y Teléfono.",
                "Abrir Tabla de Clientes",
                () -> btnNavTablaClientes.fire()
        ), 0, 0);

        modGrid.add(createQuickModuleBox(
                "2. Agregar Nuevo Cliente",
                "Registre un cliente con RUC, Clave SOL, Libros Contables, Estado de Cobranza y su cronograma 2024–2030 (84 meses).",
                "Registrar Cliente",
                () -> btnNavNuevoCliente.fire()
        ), 1, 0);

        modGrid.add(createQuickModuleBox(
                "3. Editar / Eliminar Cliente",
                "Cargue el expediente por RUC para modificar todos sus libros (Ventas, Compras, Diario), Clave SOL o darlo de baja.",
                "Editar Cliente",
                () -> btnNavEditarCliente.fire()
        ), 2, 0);

        modGrid.add(createQuickModuleBox(
                "4. Buscar Cliente por RUC",
                "Consulte al instante la ficha integral tributaria, credenciales SOL, libros y cobranza mediante el RUC de 11 dígitos.",
                "Buscar por RUC",
                () -> btnNavBuscarRuc.fire()
        ), 0, 1);

        modGrid.add(createQuickModuleBox(
                "5. Tabla de Pagos (2024–2030)",
                "Gestione la matriz anual de pagos de Enero a Diciembre por año fiscal y verifique el acumulado anual por cliente.",
                "Abrir Matriz de Pagos",
                () -> btnNavPagos.fire()
        ), 1, 1);

        modGrid.add(createQuickModuleBox(
                "6. Seguridad y Usuarios",
                "Acceda con la Contraseña General para dar de alta o baja a los operadores autorizados del sistema.",
                "Administrar Usuarios",
                () -> btnNavUsuarios.fire()
        ), 2, 1);

        modulesCard.getChildren().addAll(lblModTitle, new Separator(), modGrid);

        home.getChildren().addAll(welcomeBox, kpiGrid, modulesCard);
        return home;
    }

    private VBox createKpiCard(String title, Label valueLabel, String caption) {
        VBox box = new VBox(6);
        box.getStyleClass().add("kpi-card");
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("kpi-label");
        Label lblCap = new Label(caption);
        lblCap.getStyleClass().add("text-muted");
        box.getChildren().addAll(lblTitle, valueLabel, lblCap);
        return box;
    }

    private VBox createQuickModuleBox(String title, String description, String buttonText, Runnable action) {
        VBox box = new VBox(10);
        box.getStyleClass().add("surface-card-compact");
        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("heading-md");
        Label lblDesc = new Label(description);
        lblDesc.getStyleClass().add("text-muted");
        lblDesc.setWrapText(true);
        lblDesc.setMinHeight(48);

        Button btn = new Button(buttonText);
        btn.getStyleClass().add("btn-primary");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> action.run());

        box.getChildren().addAll(lblTitle, lblDesc, btn);
        return box;
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    private void activarBotonNav(Button active) {
        List<Button> all = List.of(
                btnNavResumen, btnNavTablaClientes, btnNavNuevoCliente,
                btnNavEditarCliente, btnNavBuscarRuc, btnNavPagos, btnNavUsuarios
        );
        for (Button b : all) {
            b.getStyleClass().remove("nav-button-active");
        }
        active.getStyleClass().add("nav-button-active");
    }

    private void abrirEdicionClienteConExpediente(ClienteExpediente expediente) {
        activarBotonNav(btnNavEditarCliente);
        clienteFormView.cargarExpedienteParaEdicion(expediente);
        root.setCenter(clienteFormView.getView());
    }

    private void abrirBusquedaConRuc(String ruc) {
        activarBotonNav(btnNavBuscarRuc);
        buscarRucView.ejecutarBusqueda(ruc);
        root.setCenter(buscarRucView.getView());
    }

    private void sincronizarTodasLasVistas() {
        clientesTableView.recargarDatos();
        pagosMatrixView.recargarMatriz();
        actualizarMetricasResumen();
    }

    private void actualizarMetricasResumen() {
        List<ClienteExpediente> expedientes = clienteService.listarExpedientes();
        int totalClientes = expedientes.size();
        int alDia = 0;
        BigDecimal sumaHonorarios = BigDecimal.ZERO;

        for (ClienteExpediente exp : expedientes) {
            if (exp.getCobranza() != null && Boolean.TRUE.equals(exp.getCobranza().getEstadototal())) {
                alDia++;
            }
            if (exp.getCliente().getHonorarios() != null) {
                sumaHonorarios = sumaHonorarios.add(exp.getCliente().getHonorarios());
            }
        }

        int totalUsuarios = authService.listarUsuarios().size();

        if (kpiTotalClientesVal != null) {
            kpiTotalClientesVal.setText(String.valueOf(totalClientes));
            kpiCobranzaAlDiaVal.setText(alDia + " / " + totalClientes);
            kpiHonorariosMensualesVal.setText("S/ " + sumaHonorarios.toPlainString());
            kpiUsuariosVal.setText(String.valueOf(totalUsuarios));
        }
        if (lblStatusResumen != null) {
            lblStatusResumen.setText("Clientes activos: " + totalClientes + " | Usuarios: " + totalUsuarios);
        }
    }

    public Parent getView() {
        return root;
    }
}
