package com.ipos.pu.ui.controller;

import com.ipos.pu.model.Product;
import com.ipos.pu.service.AdminService;
import com.ipos.pu.service.CartService;
import com.ipos.pu.service.CatalogueService;
import com.ipos.pu.ui.SceneManager;
import com.ipos.pu.ui.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

@Component("catalogueUiController")
public class CatalogueController {

    private final CatalogueService catalogueService;
    private final CartService cartService;
    private final AdminService adminService;

    @FXML private TextField searchField;
    @FXML private TextField quantityField;
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colBrand;
    @FXML private TableColumn<Product, String> colPrice;
    @FXML private TableColumn<Product, String> colStock;
    @FXML private Label messageLabel;
    @FXML private Label welcomeLabel;
    @FXML private Button cartNavButton;
    @FXML private Button campaignsNavBtn;
    @FXML private Button reportsNavBtn;

    private int quantity = 1;

    public CatalogueController(CatalogueService catalogueService, CartService cartService,
                               AdminService adminService) {
        this.catalogueService = catalogueService;
        this.cartService = cartService;
        this.adminService = adminService;
    }

    @FXML
    public void initialize() {
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colBrand.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBrand()));
        colPrice.setCellValueFactory(d -> {
            Product p = d.getValue();
            double discount = adminService.getBestDiscount(p.getId());
            if (discount > 0) {
                double discounted = p.getPrice() * (1 - discount / 100.0);
                return new SimpleStringProperty(String.format("\u00a3%.2f  (-%.0f%%)", discounted, discount));
            }
            return new SimpleStringProperty("\u00a3" + String.format("%.2f", p.getPrice()));
        });
        colStock.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getStockQuantity())));
        productsTable.setItems(FXCollections.observableArrayList(catalogueService.getAllProducts()));
        if (SessionManager.isLoggedIn()) {
            welcomeLabel.setText(SessionManager.getCurrentMember().getFirstName());
            int count = cartService.getCartItemCount(SessionManager.getCurrentMember().getId());
            cartNavButton.setText("My Cart" + (count > 0 ? "  (" + count + ")" : ""));
        }
        if (!SessionManager.isAdmin()) {
            campaignsNavBtn.setVisible(false); campaignsNavBtn.setManaged(false);
            reportsNavBtn.setVisible(false); reportsNavBtn.setManaged(false);
        }
    }

    @FXML
    private void onSearchClicked() {
        productsTable.setItems(FXCollections.observableArrayList(
                catalogueService.searchByName(searchField.getText())));
        messageLabel.setText("");
    }

    @FXML
    private void onClearClicked() {
        searchField.clear();
        productsTable.setItems(FXCollections.observableArrayList(catalogueService.getAllProducts()));
        messageLabel.setText("");
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
    private void onAddToCartClicked() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Please select a product first.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        Long memberId = SessionManager.getCurrentMember().getId();
        cartService.addToCart(memberId, selected.getId(), quantity);
        messageLabel.setText(quantity + "x " + selected.getName() + " added to cart.");
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
        quantity = 1;
        quantityField.setText("1");
        int count = cartService.getCartItemCount(memberId);
        cartNavButton.setText("My Cart" + (count > 0 ? "  (" + count + ")" : ""));
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
