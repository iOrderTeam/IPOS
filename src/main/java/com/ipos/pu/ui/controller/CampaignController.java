package com.ipos.pu.ui.controller;

import com.ipos.pu.model.Campaign;
import com.ipos.pu.service.AdminService;
import com.ipos.pu.ui.SceneManager;
import com.ipos.pu.ui.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

@Component
public class CampaignController {

    private final AdminService adminService;

    @FXML private TableView<Campaign> campaignsTable;
    @FXML private TableColumn<Campaign, String> colName;
    @FXML private TableColumn<Campaign, String> colDiscount;
    @FXML private TableColumn<Campaign, String> colStart;
    @FXML private TableColumn<Campaign, String> colEnd;
    @FXML private TableColumn<Campaign, String> colHits;
    @FXML private TextField nameField;
    @FXML private TextField descField;
    @FXML private TextField discountField;
    @FXML private TextField startField;
    @FXML private TextField endField;
    @FXML private Label messageLabel;
    @FXML private Label welcomeLabel;

    public CampaignController(AdminService adminService) {
        this.adminService = adminService;
    }

    @FXML
    public void initialize() {
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colDiscount.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDiscountPercentage() + "%"));
        colStart.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStartDate().toString()));
        colEnd.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEndDate().toString()));
        colHits.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getHits())));
        campaignsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                nameField.setText(newVal.getName());
                descField.setText(newVal.getDescription());
                discountField.setText(String.valueOf(newVal.getDiscountPercentage()));
                startField.setText(newVal.getStartDate().toString());
                endField.setText(newVal.getEndDate().toString());
            }
        });
        if (SessionManager.isLoggedIn()) {
            welcomeLabel.setText(SessionManager.getCurrentMember().getFirstName());
        }
        loadCampaigns();
    }

    private void loadCampaigns() {
        List<Campaign> campaigns = adminService.getActiveCampaigns();
        campaignsTable.setItems(FXCollections.observableArrayList(campaigns));
    }

    @FXML
    private void onCreateClicked() {
        try {
            adminService.createCampaign(
                    nameField.getText(),
                    descField.getText(),
                    Double.parseDouble(discountField.getText()),
                    LocalDate.parse(startField.getText()),
                    LocalDate.parse(endField.getText())
            );
            messageLabel.setText("Campaign created.");
            messageLabel.setStyle("-fx-text-fill: #27ae60;");
            loadCampaigns();
        } catch (Exception e) {
            messageLabel.setText("Error: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void onModifyClicked() {
        Campaign selected = campaignsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Please select a campaign to modify.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        try {
            double discount = Double.parseDouble(discountField.getText());
            LocalDate start = LocalDate.parse(startField.getText());
            LocalDate end = LocalDate.parse(endField.getText());
            adminService.modifyCampaign(selected.getId(), discount, start, end);
            messageLabel.setText("Campaign updated.");
            messageLabel.setStyle("-fx-text-fill: #27ae60;");
            loadCampaigns();
        } catch (Exception e) {
            messageLabel.setText("Error: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void onDeleteClicked() {
        Campaign selected = campaignsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Please select a campaign to delete.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        adminService.deleteCampaign(selected.getId());
        messageLabel.setText("Campaign deleted.");
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
        loadCampaigns();
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
    private void onAdminClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/admin.fxml");
    }

    @FXML
    private void onLogoutClicked() {
        SessionManager.clearSession();
        SceneManager.switchTo("/com/ipos/pu/ui/login.fxml");
    }
}
