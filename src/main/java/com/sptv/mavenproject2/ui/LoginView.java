package com.sptv.mavenproject2.ui;

import com.sptv.mavenproject2.service.AuthService;
import com.sptv.mavenproject2.service.ValidationException;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import modelo.Usuariodelprograma;
import persistance.PersistenceManager;

/**
 * Vista de inicio de sesión que reemplaza a Bloqueoprincipal (Swing).
 */
public class LoginView {

    private final AuthService authService;
    private final Consumer<Usuariodelprograma> onLoginSuccess;
    private final BorderPane root;

    public LoginView(AuthService authService, Consumer<Usuariodelprograma> onLoginSuccess) {
        this.authService = authService;
        this.onLoginSuccess = onLoginSuccess;
        this.root = buildView();
    }

    private BorderPane buildView() {
        BorderPane container = new BorderPane();
        container.setPadding(new Insets(32));

        VBox card = new VBox(16);
        card.getStyleClass().add("surface-card");
        card.setMaxWidth(440);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(32));

        Label brandTitle = new Label("SERVISERC");
        brandTitle.getStyleClass().add("heading-xl");

        Label subtitle = new Label("Sistema de Administración Tributaria y Contable de Clientes");
        subtitle.getStyleClass().add("text-muted");
        subtitle.setWrapText(true);

        Label dbStatusBadge = new Label("Motor de Persistencia: " + PersistenceManager.getActiveStatusDescription());
        dbStatusBadge.getStyleClass().add("badge-ok");
        dbStatusBadge.setWrapText(true);

        Label feedbackLabel = new Label();
        HBox feedbackBanner = UiFeedback.createFeedbackBanner(feedbackLabel);

        VBox userBox = new VBox(6);
        Label userLabel = UiFeedback.createFieldLabel("Usuario del Programa");
        TextField txtUsuario = new TextField();
        txtUsuario.setPromptText("Ingrese su nombre de usuario (ej. admin)");
        userBox.getChildren().addAll(userLabel, txtUsuario);

        VBox passBox = new VBox(6);
        Label passLabel = UiFeedback.createFieldLabel("Contraseña");
        PasswordField txtPassword = new PasswordField();
        txtPassword.setPromptText("Ingrese su contraseña");
        passBox.getChildren().addAll(passLabel, txtPassword);

        Button btnIngresar = new Button("Iniciar Sesión");
        btnIngresar.getStyleClass().add("btn-primary");
        btnIngresar.setMaxWidth(Double.MAX_VALUE);
        btnIngresar.setDefaultButton(true);

        Runnable doLogin = () -> {
            try {
                Usuariodelprograma user = authService.autenticar(txtUsuario.getText(), txtPassword.getText());
                onLoginSuccess.accept(user);
            } catch (ValidationException ve) {
                UiFeedback.showBannerError(feedbackBanner, feedbackLabel, ve.getMessage());
            } catch (Exception ex) {
                UiFeedback.showBannerError(feedbackBanner, feedbackLabel,
                        "Error al verificar credenciales: " + ex.getMessage());
            }
        };

        btnIngresar.setOnAction(e -> doLogin.run());

        card.getChildren().addAll(
                brandTitle,
                subtitle,
                dbStatusBadge,
                feedbackBanner,
                userBox,
                passBox,
                btnIngresar
        );

        if (authService.isCredencialInicialPorDefectoActiva()) {
            HBox hintBox = new HBox(8);
            hintBox.getStyleClass().add("banner-info");
            hintBox.setAlignment(Pos.CENTER_LEFT);
            Label hintText = new Label(
                    "Acceso inicial detectado: Usuario «admin» / Contraseña «admin123» (Contraseña general: «serviserc2024»).");
            hintText.setWrapText(true);
            Button btnAutoFill = new Button("Usar admin");
            btnAutoFill.getStyleClass().add("btn-secondary");
            btnAutoFill.setOnAction(e -> {
                txtUsuario.setText(AuthService.DEFAULT_ADMIN_USER);
                txtPassword.setText(AuthService.DEFAULT_ADMIN_PASS);
            });
            hintBox.getChildren().addAll(hintText, btnAutoFill);
            card.getChildren().add(hintBox);
        }

        container.setCenter(card);
        return container;
    }

    public Parent getView() {
        return root;
    }
}
