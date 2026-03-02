package com.ipos.pu.ui.controller;

import com.ipos.pu.ui.SceneManager;
import javafx.fxml.FXML;
import org.springframework.stereotype.Component;

@Component
public class MainController {

    @FXML
    private void onLoginClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/login.fxml");
    }

    @FXML
    private void onRegisterClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/register.fxml");
    }
}
