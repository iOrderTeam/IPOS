package com.ipos.pu.ui.controller;

import com.ipos.pu.ui.SceneManager;
import com.ipos.pu.ui.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.springframework.stereotype.Component;

@Component
public class MainController {

    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button catalogueButton;
    @FXML private Button cartButton;
    @FXML private Button ordersButton;
    @FXML private Button logoutButton;

    @FXML
    public void initialize() {
        boolean loggedIn = SessionManager.isLoggedIn();
        loginButton.setVisible(!loggedIn);
        registerButton.setVisible(!loggedIn);
        catalogueButton.setVisible(loggedIn);
        cartButton.setVisible(loggedIn);
        ordersButton.setVisible(loggedIn);
        logoutButton.setVisible(loggedIn);
    }

    @FXML
    private void onLoginClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/login.fxml");
    }

    @FXML
    private void onRegisterClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/register.fxml");
    }

    @FXML
    private void onCatalogueClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/catalogue.fxml");
    }

    @FXML
    private void onCartClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/cart.fxml");
    }

    @FXML
    private void onOrdersClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/track-orders.fxml");
    }

    @FXML
    private void onLogoutClicked() {
        SessionManager.clearSession();
        SceneManager.switchTo("/com/ipos/pu/ui/main.fxml");
    }
}
