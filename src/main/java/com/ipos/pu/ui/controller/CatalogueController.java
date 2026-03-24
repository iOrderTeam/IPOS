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
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colBrand;
    @FXML private TableColumn<Product, String> colPrice;
    @FXML private TableColumn<Product, String> colStock;
    @FXML private Label messageLabel;

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
    private void onAddToCartClicked() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Please select a product first.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        Long memberId = SessionManager.getCurrentMember().getId();
        cartService.addToCart(memberId, selected.getId(), 1);
        messageLabel.setText(selected.getName() + " added to cart.");
        messageLabel.setStyle("-fx-text-fill: green;");
    }

    @FXML
    private void onCartClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/cart.fxml");
    }

    @FXML
    private void onBackClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/main.fxml");
    }
}
