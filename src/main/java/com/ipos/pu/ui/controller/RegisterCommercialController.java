package com.ipos.pu.ui.controller;

import com.ipos.pu.service.MemberService;
import com.ipos.pu.ui.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

@Component
public class RegisterCommercialController {

    private final MemberService memberService;

    @FXML private TextField emailField;
    @FXML private TextField companyRegField;
    @FXML private TextField directorField;
    @FXML private TextField businessTypeField;
    @FXML private TextField addressField;
    @FXML private Label errorLabel;
    @FXML private Label successLabel;

    public RegisterCommercialController(MemberService memberService) {
        this.memberService = memberService;
    }

    @FXML
    private void onRegisterClicked() {
        String email = emailField.getText().trim();
        String companyReg = companyRegField.getText().trim();
        String director = directorField.getText().trim();
        String businessType = businessTypeField.getText().trim();
        String address = addressField.getText().trim();

        if (email.isEmpty() || companyReg.isEmpty() || director.isEmpty()
                || businessType.isEmpty() || address.isEmpty()) {
            errorLabel.setText("All fields are required.");
            successLabel.setText("");
            return;
        }

        try {
            memberService.registerCommercial(email, companyReg, director, businessType, address);
            successLabel.setText("Application submitted! You will be notified once approved.");
            errorLabel.setText("");
            emailField.clear();
            companyRegField.clear();
            directorField.clear();
            businessTypeField.clear();
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
