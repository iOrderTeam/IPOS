package com.ipos.pu.ui.controller;

import com.ipos.pu.model.Product;
import com.ipos.pu.service.CatalogueService;
import com.ipos.pu.ui.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

@Component("catalogueUiController")
public class CatalogueController {

    private final CatalogueService catalogueService;

    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colBrand;
    @FXML private TableColumn<Product, String> colPrice;
    @FXML private TableColumn<Product, String> colStock;
    @FXML private Label messageLabel;

    public CatalogueController(CatalogueService catalogueService) {
        this.catalogueService = catalogueService;
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
    private void onAddToCartClicked() {
        Product selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Please select a product first.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }
        // CartService will be wired in once built (22 March milestone)
        messageLabel.setText(selected.getName() + " selected — cart coming soon.");
        messageLabel.setStyle("-fx-text-fill: green;");
    }

    @FXML
    private void onBackClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/main.fxml");
    }
}
