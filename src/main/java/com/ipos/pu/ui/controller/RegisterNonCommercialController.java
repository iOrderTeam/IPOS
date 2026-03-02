package com.ipos.pu.ui.controller;

import com.ipos.pu.service.MemberService;
import com.ipos.pu.ui.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

@Component
public class RegisterNonCommercialController {

    private final MemberService memberService;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    public RegisterNonCommercialController(MemberService memberService) {
        this.memberService = memberService;
    }

    @FXML
    private void onRegisterClicked() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
            errorLabel.setText("All fields are required.");
            successLabel.setText("");
            return;
        }

        try {
            memberService.registerNonCommercial(email, firstName, lastName);
            successLabel.setText("Account created! Check your email for your temporary password.");
            errorLabel.setText("");
            firstNameField.clear();
            lastNameField.clear();
            emailField.clear();
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
            successLabel.setText("");
        }
    }

    @FXML
    private void onBackClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/register.fxml");
    }
}
