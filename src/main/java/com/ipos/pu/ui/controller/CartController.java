package com.ipos.pu.ui.controller;

import com.ipos.pu.model.CartItem;
import com.ipos.pu.service.CartService;
import com.ipos.pu.ui.SceneManager;
import com.ipos.pu.ui.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    @FXML private TableColumn<CartItem, Void> colRemove;
    @FXML private Label totalLabel;
    @FXML private Label welcomeLabel;
    @FXML private Button cartNavButton;

    private ObservableList<CartItem> cartItems;

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

        colRemove.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Remove");
            {
                btn.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 11px; -fx-background-radius: 4; -fx-cursor: hand; -fx-padding: 4 8;");
                btn.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    cartService.removeFromCart(item.getId());
                    refreshCart();
                });
            }
            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        if (SessionManager.isLoggedIn()) {
            welcomeLabel.setText(SessionManager.getCurrentMember().getFirstName());
        }
        refreshCart();
    }

    private void refreshCart() {
        Long memberId = SessionManager.getCurrentMember().getId();
        List<CartItem> items = cartService.getCart(memberId);
        cartItems = FXCollections.observableArrayList(items);
        cartTable.setItems(cartItems);

        double total = cartService.getCartTotal(memberId);
        totalLabel.setText("Total: £" + String.format("%.2f", total));

        int count = cartService.getCartItemCount(memberId);
        cartNavButton.setText("My Cart" + (count > 0 ? "  (" + count + ")" : ""));
    }

    @FXML
    private void onPlaceOrderClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/checkout.fxml");
    }

    @FXML
    private void onCatalogueClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/catalogue.fxml");
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

    @FXML
    private void onBackClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/catalogue.fxml");
    }
}
