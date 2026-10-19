package com.sptv.mavenproject2.ui;

import java.util.Optional;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;

/**
 * Utilidades de retroalimentación visual, banners de estado y cuadros de diálogo
 * para la interfaz JavaFX de ServiSerc.
 */
public final class UiFeedback {

    private UiFeedback() {
    }

    public static Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return label;
    }

    public static void showBannerSuccess(HBox container, Label messageLabel, String message) {
        container.getStyleClass().removeAll("banner-info", "banner-success", "banner-error");
        container.getStyleClass().add("banner-success");
        messageLabel.setText("✔ " + message);
        messageLabel.setTextFill(Color.web("#166534"));
        container.setVisible(true);
        container.setManaged(true);
    }

    public static void showBannerError(HBox container, Label messageLabel, String message) {
        container.getStyleClass().removeAll("banner-info", "banner-success", "banner-error");
        container.getStyleClass().add("banner-error");
        messageLabel.setText("✖ " + message);
        messageLabel.setTextFill(Color.web("#991B1B"));
        container.setVisible(true);
        container.setManaged(true);
    }

    public static void showBannerInfo(HBox container, Label messageLabel, String message) {
        container.getStyleClass().removeAll("banner-info", "banner-success", "banner-error");
        container.getStyleClass().add("banner-info");
        messageLabel.setText("ℹ " + message);
        messageLabel.setTextFill(Color.web("#1E40AF"));
        container.setVisible(true);
        container.setManaged(true);
    }

    public static HBox createFeedbackBanner(Label messageLabel) {
        HBox banner = new HBox(10);
        banner.setAlignment(Pos.CENTER_LEFT);
        messageLabel.setWrapText(true);
        HBox.setHgrow(messageLabel, Priority.ALWAYS);
        banner.getChildren().add(messageLabel);
        banner.setVisible(false);
        banner.setManaged(false);
        return banner;
    }

    public static boolean confirmAction(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        ButtonType yesBtn = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesBtn, cancelBtn);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yesBtn;
    }
}
