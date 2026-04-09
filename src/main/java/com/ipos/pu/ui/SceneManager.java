package com.ipos.pu.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

public class SceneManager {

    private static Stage stage;
    private static ConfigurableApplicationContext springContext;

    public static void init(Stage stage, ConfigurableApplicationContext springContext) {
        SceneManager.stage = stage;
        SceneManager.springContext = springContext;
    }

    public static void switchTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> T loadWithController(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            loader.setControllerFactory(springContext::getBean);
            loader.load();
            return loader.getController();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
