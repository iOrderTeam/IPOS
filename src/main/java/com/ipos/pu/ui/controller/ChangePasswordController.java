package com.ipos.pu.ui.controller;

import com.ipos.pu.service.MemberService;
import com.ipos.pu.ui.SceneManager;
import com.ipos.pu.ui.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import org.springframework.stereotype.Component;

@Component
public class ChangePasswordController {

    private final MemberService memberService;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    public ChangePasswordController(MemberService memberService) {
        this.memberService = memberService;
    }

    @FXML
    private void onChangeClicked() {
        String current = currentPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            errorLabel.setText("All fields are required.");
            return;
        }

        if (!newPass.equals(confirm)) {
            errorLabel.setText("New passwords do not match.");
            return;
        }

        try {
            memberService.changePassword(SessionManager.getCurrentMember().getId(), current, newPass);
            SceneManager.switchTo("/com/ipos/pu/ui/main.fxml");
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }
}
