package com.ipos.pu.ui.controller;

import com.ipos.pu.model.Product;
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

    private int quantity = 1;

    public CatalogueController(CatalogueService catalogueService, CartService cartService) {
        this.catalogueService = catalogueService;
        this.cartService = cartService;
    }

    @FXML
    public void initialize() {
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        colBrand.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getBrand()));
        colPrice.setCellValueFactory(d -> new SimpleStringProperty("£" + d.getValue().getPrice()));
        colStock.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getStockQuantity())));
        productsTable.setItems(FXCollections.observableArrayList(catalogueService.getAllProducts()));
        if (SessionManager.isLoggedIn()) {
            welcomeLabel.setText(SessionManager.getCurrentMember().getFirstName());
            int count = cartService.getCartItemCount(SessionManager.getCurrentMember().getId());
            cartNavButton.setText("My Cart" + (count > 0 ? "  (" + count + ")" : ""));
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
    private void onReportsClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/reports.fxml");
    }

    @FXML
    private void onLogoutClicked() {
        SessionManager.clearSession();
        SceneManager.switchTo("/com/ipos/pu/ui/login.fxml");
    }
}
