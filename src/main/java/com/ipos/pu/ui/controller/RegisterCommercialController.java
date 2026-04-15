package com.ipos.pu.ui.controller;

import com.ipos.pu.service.MemberService;
import com.ipos.pu.ui.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

@Component
public class RegisterCommercialController {

    private final MemberService memberService;

    @FXML private TextField emailField;
    @FXML private TextField companyNameField;
    @FXML private TextField companyRegField;
    @FXML private TextArea addressField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    public RegisterCommercialController(MemberService memberService) {
        this.memberService = memberService;
    }

    @FXML
    private void onRegisterClicked() {
        String email = emailField.getText().trim();
        String companyName = companyNameField.getText().trim();
        String companyReg = companyRegField.getText().trim();
        String address = addressField.getText().trim();

        if (email.isEmpty() || companyName.isEmpty() || companyReg.isEmpty() || address.isEmpty()) {
            errorLabel.setText("All fields are required.");
            successLabel.setText("");
            return;
        }

        try {
            memberService.registerCommercial(email, companyName, companyReg, companyName, "Pharmacy", address);
            successLabel.setText("Application submitted! You will be notified once approved.");
            errorLabel.setText("");
            emailField.clear();
            companyNameField.clear();
            companyRegField.clear();
            addressField.clear();
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
