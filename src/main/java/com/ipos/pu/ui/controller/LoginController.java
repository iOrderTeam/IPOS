package com.ipos.pu.ui.controller;

import com.ipos.pu.model.Member;
import com.ipos.pu.service.MemberService;
import com.ipos.pu.ui.SceneManager;
import com.ipos.pu.ui.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

@Component
public class LoginController {

    private final MemberService memberService;

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    public LoginController(MemberService memberService) {
        this.memberService = memberService;
    }

    @FXML
    private void onLoginClicked() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter your email and password.");
            return;
        }

        try {
            Member member = memberService.login(email, password);
            SessionManager.setCurrentMember(member);

            if (member.isPasswordChangeRequired()) {
                SceneManager.switchTo("/com/ipos/pu/ui/change-password.fxml");
            } else {
                SceneManager.switchTo("/com/ipos/pu/ui/main.fxml");
            }
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onBackClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/main.fxml");
    }
}
