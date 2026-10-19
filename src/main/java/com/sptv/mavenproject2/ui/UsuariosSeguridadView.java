package com.sptv.mavenproject2.ui;

import com.sptv.mavenproject2.service.AuthService;
import com.sptv.mavenproject2.service.ValidationException;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import modelo.Usuariodelprograma;

/**
 * Vista de Seguridad General y Administración de Usuarios del Programa.
 * Reemplaza a SeguridadUsuarios y Administraciondeusuarios (Swing).
 */
public class UsuariosSeguridadView {

    private final AuthService authService;
    private final StackPane root;

    private VBox gatePane;
    private BorderPane adminPane;

    private final ObservableList<Usuariodelprograma> usuariosList = FXCollections.observableArrayList();
    private TableView<Usuariodelprograma> tableUsuarios;

    private Label adminFeedbackLabel;
    private HBox adminFeedbackBanner;

    private TextField txtDelId;
    private TextField txtDelUsuario;

    public UsuariosSeguridadView(AuthService authService) {
        this.authService = authService;
        this.root = buildView();
    }

    private StackPane buildView() {
        StackPane stack = new StackPane();
        gatePane = buildSecurityGatePane();
        adminPane = buildAdminUsersPane();

        adminPane.setVisible(false);
        adminPane.setManaged(false);

        stack.getChildren().addAll(gatePane, adminPane);
        return stack;
    }

    private VBox buildSecurityGatePane() {
        VBox wrapper = new VBox(16);
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setPadding(new Insets(32));

        VBox card = new VBox(16);
        card.getStyleClass().add("surface-card");
        card.setMaxWidth(460);
        card.setPadding(new Insets(28));

        Label title = new Label("Control de Seguridad de Usuarios");
        title.getStyleClass().add("heading-lg");

        Label desc = new Label(
                "Para acceder a la administración de usuarios del programa, ingrese la Contraseña General de Seguridad (tabla ContrasenaGeneral).");
        desc.getStyleClass().add("text-muted");
        desc.setWrapText(true);

        Label gateFeedbackLbl = new Label();
        HBox gateBanner = UiFeedback.createFeedbackBanner(gateFeedbackLbl);

        PasswordField txtContrasenaGeneral = new PasswordField();
        txtContrasenaGeneral.setPromptText("Contraseña General (inicial: serviserc2024)");

        HBox buttonRow = new HBox(10);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnAutoFill = new Button("Autocompletar inicial");
        btnAutoFill.getStyleClass().add("btn-secondary");
        btnAutoFill.setOnAction(e -> txtContrasenaGeneral.setText(AuthService.DEFAULT_GENERAL_PASS));

        Button btnDesbloquear = new Button("Verificar y Abrir Administración");
        btnDesbloquear.getStyleClass().add("btn-primary");
        btnDesbloquear.setDefaultButton(true);
        btnDesbloquear.setOnAction(e -> {
            try {
                authService.verificarContrasenaGeneral(txtContrasenaGeneral.getText());
                txtContrasenaGeneral.clear();
                gateBanner.setVisible(false);
                gateBanner.setManaged(false);
                mostrarPanelAdministracion();
            } catch (ValidationException ve) {
                UiFeedback.showBannerError(gateBanner, gateFeedbackLbl, ve.getMessage());
            } catch (Exception ex) {
                UiFeedback.showBannerError(gateBanner, gateFeedbackLbl,
                        "Error al verificar contraseña general: " + ex.getMessage());
            }
        });

        buttonRow.getChildren().addAll(btnAutoFill, btnDesbloquear);

        card.getChildren().addAll(
                title,
                desc,
                gateBanner,
                UiFeedback.createFieldLabel("Contraseña General de Seguridad *"),
                txtContrasenaGeneral,
                buttonRow
        );

        wrapper.getChildren().add(card);
        return wrapper;
    }

    private BorderPane buildAdminUsersPane() {
        BorderPane container = new BorderPane();
        container.setPadding(new Insets(20));

        VBox topSection = new VBox(12);
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(4);
        Label title = new Label("Administración de Usuarios del Programa");
        title.getStyleClass().add("heading-lg");
        Label subtitle = new Label(
                "Gestione las cuentas autorizadas con cifrado SHA-256 y la Contraseña General de protección del módulo.");
        subtitle.getStyleClass().add("text-muted");
        titleBox.getChildren().addAll(title, subtitle);

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnBloquear = new Button("🔒 Bloquear Módulo");
        btnBloquear.getStyleClass().add("btn-secondary");
        btnBloquear.setOnAction(e -> bloquearPanelAdministracion());

        headerRow.getChildren().addAll(titleBox, spacer, btnBloquear);

        adminFeedbackLabel = new Label();
        adminFeedbackBanner = UiFeedback.createFeedbackBanner(adminFeedbackLabel);

        topSection.getChildren().addAll(headerRow, adminFeedbackBanner);
        topSection.setPadding(new Insets(0, 0, 12, 0));
        container.setTop(topSection);

        // Tabla central de usuarios registrados
        tableUsuarios = new TableView<>();
        tableUsuarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Usuariodelprograma, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cd -> new SimpleStringProperty(String.valueOf(cd.getValue().getId())));
        colId.setMaxWidth(90);

        TableColumn<Usuariodelprograma, String> colUser = new TableColumn<>("Nombre de Usuario");
        colUser.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getUsuario()));

        TableColumn<Usuariodelprograma, String> colHash = new TableColumn<>("Hash SHA-256 de Contraseña");
        colHash.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getContrasena()));

        tableUsuarios.getColumns().setAll(List.of(colId, colUser, colHash));
        tableUsuarios.setItems(usuariosList);

        tableUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtDelId.setText(String.valueOf(newSel.getId()));
                txtDelUsuario.setText(newSel.getUsuario());
            }
        });

        container.setCenter(tableUsuarios);

        // Paneles inferiores: 1) Crear Usuario, 2) Eliminar Usuario (por ID + Nombre), 3) Cambiar Contraseña General
        GridPane actionsGrid = new GridPane();
        actionsGrid.setHgap(16);
        actionsGrid.setVgap(16);
        BorderPane.setMargin(actionsGrid, new Insets(16, 0, 0, 0));

        ColumnConstraints c1 = new ColumnConstraints();
        c1.setPercentWidth(33.33);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setPercentWidth(33.33);
        ColumnConstraints c3 = new ColumnConstraints();
        c3.setPercentWidth(33.33);
        actionsGrid.getColumnConstraints().addAll(c1, c2, c3);

        // Tarjeta 1: Agregar Usuario
        VBox cardAdd = new VBox(10);
        cardAdd.getStyleClass().add("surface-card-compact");
        Label lblAdd = new Label("Registrar Nuevo Usuario");
        lblAdd.getStyleClass().add("heading-md");
        TextField txtNuevoUser = new TextField();
        txtNuevoUser.setPromptText("Nombre de usuario");
        PasswordField txtNuevaPass = new PasswordField();
        txtNuevaPass.setPromptText("Contraseña (mín. 4 caracteres)");
        Button btnCrearUser = new Button("Agregar Usuario (SHA-256)");
        btnCrearUser.getStyleClass().add("btn-primary");
        btnCrearUser.setMaxWidth(Double.MAX_VALUE);
        btnCrearUser.setOnAction(e -> {
            try {
                Usuariodelprograma creado = authService.registrarUsuario(
                        txtNuevoUser.getText(), txtNuevaPass.getText());
                txtNuevoUser.clear();
                txtNuevaPass.clear();
                recargarUsuarios();
                UiFeedback.showBannerSuccess(adminFeedbackBanner, adminFeedbackLabel,
                        "Usuario «" + creado.getUsuario() + "» creado con ID " + creado.getId() + ".");
            } catch (ValidationException ve) {
                UiFeedback.showBannerError(adminFeedbackBanner, adminFeedbackLabel, ve.getMessage());
            }
        });
        cardAdd.getChildren().addAll(
                lblAdd, new Separator(),
                UiFeedback.createFieldLabel("Usuario:"), txtNuevoUser,
                UiFeedback.createFieldLabel("Contraseña:"), txtNuevaPass,
                btnCrearUser
        );

        // Tarjeta 2: Eliminar Usuario (verificando ID y Nombre de Usuario como en Administraciondeusuarios)
        VBox cardDel = new VBox(10);
        cardDel.getStyleClass().add("surface-card-compact");
        Label lblDel = new Label("Eliminar Usuario (Validación ID + Nombre)");
        lblDel.getStyleClass().add("heading-md");
        txtDelId = new TextField();
        txtDelId.setPromptText("ID del usuario (seleccione en tabla)");
        txtDelUsuario = new TextField();
        txtDelUsuario.setPromptText("Confirmar nombre exacto");
        Button btnEliminarUser = new Button("Eliminar Usuario");
        btnEliminarUser.getStyleClass().add("btn-danger");
        btnEliminarUser.setMaxWidth(Double.MAX_VALUE);
        btnEliminarUser.setOnAction(e -> {
            try {
                Long id = Long.valueOf(txtDelId.getText().trim());
                String username = txtDelUsuario.getText();
                authService.eliminarUsuario(id, username);
                txtDelId.clear();
                txtDelUsuario.clear();
                recargarUsuarios();
                UiFeedback.showBannerSuccess(adminFeedbackBanner, adminFeedbackLabel,
                        "Usuario «" + username + "» (ID " + id + ") eliminado correctamente.");
            } catch (NumberFormatException nfe) {
                UiFeedback.showBannerError(adminFeedbackBanner, adminFeedbackLabel,
                        "El ID del usuario a eliminar debe ser un número entero válido.");
            } catch (ValidationException ve) {
                UiFeedback.showBannerError(adminFeedbackBanner, adminFeedbackLabel, ve.getMessage());
            }
        });
        cardDel.getChildren().addAll(
                lblDel, new Separator(),
                UiFeedback.createFieldLabel("ID de Usuario:"), txtDelId,
                UiFeedback.createFieldLabel("Nombre de Usuario:"), txtDelUsuario,
                btnEliminarUser
        );

        // Tarjeta 3: Actualizar Contraseña General
        VBox cardGenPass = new VBox(10);
        cardGenPass.getStyleClass().add("surface-card-compact");
        Label lblGen = new Label("Actualizar Contraseña General");
        lblGen.getStyleClass().add("heading-md");
        Label lblGenHint = new Label("Define la llave maestra exigida al abrir este módulo:");
        lblGenHint.getStyleClass().add("text-muted");
        lblGenHint.setWrapText(true);
        PasswordField txtNuevaGeneral = new PasswordField();
        txtNuevaGeneral.setPromptText("Nueva contraseña general");
        Button btnActualizarGen = new Button("Guardar Contraseña General");
        btnActualizarGen.getStyleClass().add("btn-secondary");
        btnActualizarGen.setMaxWidth(Double.MAX_VALUE);
        btnActualizarGen.setOnAction(e -> {
            try {
                authService.actualizarContrasenaGeneral(txtNuevaGeneral.getText());
                txtNuevaGeneral.clear();
                UiFeedback.showBannerSuccess(adminFeedbackBanner, adminFeedbackLabel,
                        "Contraseña General actualizada y cifrada con SHA-256.");
            } catch (ValidationException ve) {
                UiFeedback.showBannerError(adminFeedbackBanner, adminFeedbackLabel, ve.getMessage());
            }
        });
        cardGenPass.getChildren().addAll(
                lblGen, new Separator(),
                lblGenHint,
                UiFeedback.createFieldLabel("Nueva Contraseña General:"), txtNuevaGeneral,
                btnActualizarGen
        );

        actionsGrid.add(cardAdd, 0, 0);
        actionsGrid.add(cardDel, 1, 0);
        actionsGrid.add(cardGenPass, 2, 0);

        container.setBottom(actionsGrid);
        return container;
    }

    private void mostrarPanelAdministracion() {
        recargarUsuarios();
        gatePane.setVisible(false);
        gatePane.setManaged(false);
        adminPane.setVisible(true);
        adminPane.setManaged(true);
    }

    public void bloquearPanelAdministracion() {
        adminPane.setVisible(false);
        adminPane.setManaged(false);
        gatePane.setVisible(true);
        gatePane.setManaged(true);
    }

    public void recargarUsuarios() {
        List<Usuariodelprograma> lista = authService.listarUsuarios();
        usuariosList.setAll(lista);
    }

    public Parent getView() {
        return root;
    }
}
