package com.ipos.pu.ui.controller;

import com.ipos.pu.model.Campaign;
import com.ipos.pu.model.CampaignProduct;
import com.ipos.pu.model.Product;
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
    @FXML private TableColumn<Campaign, String> colActive;
    @FXML private TextField nameField;
    @FXML private TextField descField;
    @FXML private TextField discountField;
    @FXML private TextField startField;
    @FXML private TextField endField;
    @FXML private Label messageLabel;
    @FXML private Label welcomeLabel;

    // Product management
    @FXML private ComboBox<Product> productCombo;
    @FXML private TextField productDiscountField;
    @FXML private TableView<CampaignProduct> productsTable;
    @FXML private TableColumn<CampaignProduct, String> colProdName;
    @FXML private TableColumn<CampaignProduct, String> colProdPrice;
    @FXML private TableColumn<CampaignProduct, String> colProdDiscount;
    @FXML private TableColumn<CampaignProduct, String> colProdHits;

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
        colActive.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isActive() ? "Yes" : "No"));

        colProdName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProduct().getName()));
        colProdPrice.setCellValueFactory(d -> new SimpleStringProperty(
                "\u00a3" + String.format("%.2f", d.getValue().getProduct().getPrice())));
        colProdDiscount.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%.0f%%", d.getValue().getEffectiveDiscount())));
        colProdHits.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getHits())));

        campaignsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                nameField.setText(newVal.getName());
                descField.setText(newVal.getDescription());
                discountField.setText(String.valueOf(newVal.getDiscountPercentage()));
                startField.setText(newVal.getStartDate().toString());
                endField.setText(newVal.getEndDate().toString());
                loadCampaignProducts(newVal);
            } else {
                productsTable.setItems(FXCollections.emptyObservableList());
            }
        });

        // Populate product combo for adding products to campaigns
        productCombo.setItems(FXCollections.observableArrayList(adminService.getAllProducts()));
        productCombo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName() + " (\u00a3" + item.getPrice() + ")");
            }
        });
        productCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        if (SessionManager.isLoggedIn()) {
            welcomeLabel.setText(SessionManager.getCurrentMember().getFirstName());
        }
        loadCampaigns();
    }

    private void loadCampaigns() {
        List<Campaign> campaigns = adminService.getAllCampaigns();
        campaignsTable.setItems(FXCollections.observableArrayList(campaigns));
    }

    private void loadCampaignProducts(Campaign campaign) {
        List<CampaignProduct> products = adminService.getCampaignProducts(campaign.getId());
        productsTable.setItems(FXCollections.observableArrayList(products));
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
    private void onTerminateClicked() {
        Campaign selected = campaignsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Please select a campaign to terminate.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        if (!selected.isActive()) {
            messageLabel.setText("Campaign is already terminated.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        adminService.terminateCampaign(selected.getId());
        messageLabel.setText("Campaign terminated early. End date set to today.");
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
        loadCampaigns();
    }

    @FXML
    private void onAddProductClicked() {
        Campaign selected = campaignsTable.getSelectionModel().getSelectedItem();
        Product product = productCombo.getValue();
        if (selected == null) {
            messageLabel.setText("Please select a campaign first.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        if (product == null) {
            messageLabel.setText("Please select a product to add.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        try {
            Double discountOverride = null;
            String discountText = productDiscountField.getText().trim();
            if (!discountText.isEmpty()) {
                discountOverride = Double.parseDouble(discountText);
                if (discountOverride < 0 || discountOverride > 100)
                    throw new IllegalArgumentException("Discount must be between 0 and 100.");
            }

            adminService.addProductToCampaign(selected.getId(), product.getId(), discountOverride);

            // Check for overlapping campaign conflicts
            List<String> conflicts = adminService.checkOverlappingCampaigns(
                    selected.getId(), product.getId());
            if (!conflicts.isEmpty()) {
                StringBuilder msg = new StringBuilder("Product added. CONFLICT WARNING:\n");
                for (String c : conflicts) {
                    msg.append("\u2022 ").append(c).append("\n");
                }
                messageLabel.setText(msg.toString().trim());
                messageLabel.setStyle("-fx-text-fill: #e67e22;");
            } else {
                messageLabel.setText("Product added to campaign.");
                messageLabel.setStyle("-fx-text-fill: #27ae60;");
            }
            productDiscountField.clear();
            loadCampaignProducts(selected);
        } catch (Exception e) {
            messageLabel.setText("Error: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void onRemoveProductClicked() {
        CampaignProduct selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Please select a product to remove.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        adminService.removeProductFromCampaign(selected.getID());
        messageLabel.setText("Product removed from campaign.");
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
        Campaign campaign = campaignsTable.getSelectionModel().getSelectedItem();
        if (campaign != null) {
            loadCampaignProducts(campaign);
        }
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
    private void onPromotionsClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/promotions.fxml");
    }

    @FXML
    private void onReportsClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/reports.fxml");
    }

    @FXML
    private void onLogoutClicked() {
        SessionManager.clearSession();
        SceneManager.switchTo("/com/ipos/pu/ui/login.fxml");
    }
}
