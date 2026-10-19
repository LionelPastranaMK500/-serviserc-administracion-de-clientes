package com.sptv.mavenproject2;

import com.sptv.mavenproject2.service.AuthService;
import com.sptv.mavenproject2.service.ClienteService;
import com.sptv.mavenproject2.service.PagoMensualService;
import com.sptv.mavenproject2.ui.LoginView;
import com.sptv.mavenproject2.ui.MainDashboardView;
import java.net.URL;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import modelo.Usuariodelprograma;
import persistance.PersistenceManager;

/**
 * Aplicación principal JavaFX de ServiSerc — Administración de Clientes.
 */
public class ServisercApp extends Application {

    private Stage primaryStage;
    private AuthService authService;
    private ClienteService clienteService;
    private PagoMensualService pagoMensualService;

    @Override
    public void init() {
        PersistenceManager.getEntityManagerFactory();
        this.authService = new AuthService();
        this.clienteService = new ClienteService();
        this.pagoMensualService = new PagoMensualService();
        this.authService.ensureInitialSeed();
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setTitle("ServiSerc — Administración de Clientes (JavaFX)");
        this.primaryStage.setMinWidth(1120);
        this.primaryStage.setMinHeight(720);

        mostrarVistaLogin();
        this.primaryStage.show();

        if (Boolean.getBoolean("serviserc.smokeTest") || getParameters().getRaw().contains("--smoke-test")) {
            PauseTransition pause = new PauseTransition(Duration.millis(800));
            pause.setOnFinished(e -> {
                Usuariodelprograma adminUser = new Usuariodelprograma();
                adminUser.setUsuario(AuthService.DEFAULT_ADMIN_USER);
                mostrarDashboardPrincipal(adminUser);

                PauseTransition closePause = new PauseTransition(Duration.millis(800));
                closePause.setOnFinished(ev -> {
                    System.out.println("[SMOKE-TEST-OK] Ventana JavaFX, LoginView y MainDashboardView inicializados y renderizados correctamente.");
                    Platform.exit();
                });
                closePause.play();
            });
            pause.play();
        }
    }

    public void mostrarVistaLogin() {
        LoginView loginView = new LoginView(authService, this::mostrarDashboardPrincipal);
        Scene scene = new Scene(loginView.getView(), 1200, 760);
        aplicarHojaDeEstilos(scene);
        primaryStage.setScene(scene);
    }

    public void mostrarDashboardPrincipal(Usuariodelprograma usuarioAutenticado) {
        MainDashboardView dashboardView = new MainDashboardView(
                authService,
                clienteService,
                pagoMensualService,
                usuarioAutenticado,
                this::mostrarVistaLogin
        );
        Scene scene = new Scene(dashboardView.getView(), 1280, 800);
        aplicarHojaDeEstilos(scene);
        primaryStage.setScene(scene);
    }

    private void aplicarHojaDeEstilos(Scene scene) {
        URL cssUrl = getClass().getResource("/styles/app.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }
    }

    @Override
    public void stop() {
        PersistenceManager.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
