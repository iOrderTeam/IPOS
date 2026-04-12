package com.ipos.pu.ui.controller;

import com.ipos.pu.model.Campaign;
import com.ipos.pu.model.CampaignProduct;
import com.ipos.pu.service.AdminService;
import com.ipos.pu.service.CartService;
import com.ipos.pu.ui.SceneManager;
import com.ipos.pu.ui.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class PromotionsController {

    private final AdminService adminService;
    private final CartService cartService;

    @FXML private TableView<Campaign> campaignsTable;
    @FXML private TableColumn<Campaign, String> colCampName;
    @FXML private TableColumn<Campaign, String> colCampDiscount;
    @FXML private TableColumn<Campaign, String> colCampEnd;
    @FXML private TableColumn<Campaign, String> colCampDesc;

    @FXML private TableView<CampaignProduct> productsTable;
    @FXML private TableColumn<CampaignProduct, String> colProdName;
    @FXML private TableColumn<CampaignProduct, String> colProdOrigPrice;
    @FXML private TableColumn<CampaignProduct, String> colProdDiscPrice;
    @FXML private TableColumn<CampaignProduct, String> colProdStock;

    @FXML private Label campaignDetailLabel;
    @FXML private Label messageLabel;
    @FXML private Label welcomeLabel;
    @FXML private Button cartNavButton;
    @FXML private Button campaignsNavBtn;
    @FXML private Button reportsNavBtn;
    @FXML private TextField quantityField;

    private int quantity = 1;

    public PromotionsController(AdminService adminService, CartService cartService) {
        this.adminService = adminService;
        this.cartService = cartService;
    }

    @FXML
    public void initialize() {
        colCampName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colCampDiscount.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDiscountPercentage() + "% off"));
        colCampEnd.setCellValueFactory(d -> new SimpleStringProperty(
                "Ends: " + d.getValue().getEndDate().toString()));
        colCampDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescription()));

        colProdName.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getProduct().getName()));
        colProdOrigPrice.setCellValueFactory(d -> new SimpleStringProperty(
                "\u00a3" + String.format("%.2f", d.getValue().getProduct().getPrice())));
        colProdDiscPrice.setCellValueFactory(d -> {
            double orig = d.getValue().getProduct().getPrice();
            double disc = d.getValue().getEffectiveDiscount();
            double discounted = orig * (1 - disc / 100.0);
            return new SimpleStringProperty("\u00a3" + String.format("%.2f", discounted));
        });
        colProdStock.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getProduct().getStockQuantity())));

        // When a campaign is selected, increment its hit counter and show products
        campaignsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                adminService.incrementCampaignHits(newVal.getId());
                List<CampaignProduct> products = adminService.getCampaignProducts(newVal.getId());
                productsTable.setItems(FXCollections.observableArrayList(products));
                campaignDetailLabel.setText(newVal.getName() + " — "
                        + newVal.getDiscountPercentage() + "% discount until "
                        + newVal.getEndDate());
            } else {
                productsTable.setItems(FXCollections.emptyObservableList());
                campaignDetailLabel.setText("Select a promotion above to view discounted products.");
            }
        });

        List<Campaign> active = adminService.getActiveCampaigns();
        campaignsTable.setItems(FXCollections.observableArrayList(active));

        if (SessionManager.isLoggedIn()) {
            welcomeLabel.setText(SessionManager.getCurrentMember().getFirstName());
            Long memberId = SessionManager.getCurrentMember().getId();
            int count = cartService.getCartItemCount(memberId);
            cartNavButton.setText("My Cart" + (count > 0 ? "  (" + count + ")" : ""));
        }
        if (!SessionManager.isAdmin()) {
            campaignsNavBtn.setVisible(false); campaignsNavBtn.setManaged(false);
            reportsNavBtn.setVisible(false); reportsNavBtn.setManaged(false);
        }
    }

    @FXML
    private void onAddToCartClicked() {
        CampaignProduct selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Please select a product first.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        Long memberId = SessionManager.getCurrentMember().getId();
        cartService.addToCart(memberId, selected.getProduct().getId(), quantity);
        messageLabel.setText(quantity + "x " + selected.getProduct().getName()
                + " added to cart (campaign discount applies at checkout).");
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
        quantity = 1;
        quantityField.setText("1");
        int count = cartService.getCartItemCount(memberId);
        cartNavButton.setText("My Cart" + (count > 0 ? "  (" + count + ")" : ""));
    }

    @FXML
    private void onIncrementQty() {
        quantity++;
        quantityField.setText(String.valueOf(quantity));
    }

    @FXML
    private void onDecrementQty() {
        if (quantity > 1) {
            quantity--;
            quantityField.setText(String.valueOf(quantity));
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
    private void onCampaignsClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/campaigns.fxml");
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
