package com.ipos.pu.ui.controller;

import com.ipos.pu.model.Member;
import com.ipos.pu.service.AdminService;
import com.ipos.pu.ui.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class AdminController {

    private final AdminService adminService;

    @FXML private ListView<String> pendingList;
    @FXML private Label messageLabel;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @FXML
    public void initialize() {
        loadPending();
    }

    private void loadPending() {
        List<Member> pending = adminService.getPendingApplications();
        pendingList.getItems().clear();
        for (Member m : pending) {
            pendingList.getItems().add(
                    m.getId() + " | " + m.getEmail() + " | " + m.getCompanyRegistrationNumber()
            );
        }
    }

    @FXML
    private void onApproveClicked() {
        String selected = pendingList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Please select an application first.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        Long memberId = Long.parseLong(selected.split(" \\| ")[0].trim());
        adminService.approveMember(memberId, "Temp1234!");
        messageLabel.setText("Member approved. Temporary password sent by email.");
        messageLabel.setStyle("-fx-text-fill: green;");
        loadPending();
    }

    @FXML
    private void onRejectClicked() {
        String selected = pendingList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Please select an application first.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        Long memberId = Long.parseLong(selected.split(" \\| ")[0].trim());
        adminService.rejectMember(memberId);
        messageLabel.setText("Member rejected.");
        messageLabel.setStyle("-fx-text-fill: green;");
        loadPending();
    }

    @FXML
    private void onBackClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/main.fxml");
    }
}