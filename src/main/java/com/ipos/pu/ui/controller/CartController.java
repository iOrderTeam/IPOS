package com.ipos.pu.ui.controller;

import com.ipos.pu.model.CartItem;
import com.ipos.pu.service.CartService;
import com.ipos.pu.ui.SceneManager;
import com.ipos.pu.ui.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;
import java.util.List;

@Component("cartUiController")
public class CartController {

    private final CartService cartService;

    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> colProduct;
    @FXML private TableColumn<CartItem, String> colQty;
    @FXML private TableColumn<CartItem, String> colUnitPrice;
    @FXML private TableColumn<CartItem, String> colLineTotal;
    @FXML private Label totalLabel;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @FXML
    public void initialize() {
        colProduct.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProduct().getName()));
        colQty.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getQuantity())));
        colUnitPrice.setCellValueFactory(d -> new SimpleStringProperty("£" + d.getValue().getProduct().getPrice()));
        colLineTotal.setCellValueFactory(d -> new SimpleStringProperty(
                "£" + String.format("%.2f", d.getValue().getQuantity() * d.getValue().getProduct().getPrice())));

        Long memberId = SessionManager.getCurrentMember().getId();
        List<CartItem> items = cartService.getCart(memberId);
        cartTable.setItems(FXCollections.observableArrayList(items));

        double total = cartService.getCartTotal(memberId);
        totalLabel.setText("Total: £" + String.format("%.2f", total));
    }

    @FXML
    private void onPlaceOrderClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/checkout.fxml");
    }

    @FXML
    private void onBackClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/catalogue.fxml");
    }
}
