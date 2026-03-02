package com.ipos.pu.ui.controller;

import com.ipos.pu.ui.SceneManager;
import javafx.fxml.FXML;
import org.springframework.stereotype.Component;

@Component
public class RegisterController {

    @FXML
    private void onNonCommercialClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/register-non-commercial.fxml");
    }

    @FXML
    private void onCommercialClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/register-commercial.fxml");
    }

    @FXML
    private void onBackClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/main.fxml");
    }
}
