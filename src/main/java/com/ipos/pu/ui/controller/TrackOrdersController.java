package com.ipos.pu.ui.controller;

import com.ipos.pu.model.Order;
import com.ipos.pu.service.CartService;
import com.ipos.pu.service.OrderService;
import com.ipos.pu.ui.SceneManager;
import com.ipos.pu.ui.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class TrackOrdersController {

    private final OrderService orderService;
    private final CartService cartService;

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> colId;
    @FXML private TableColumn<Order, String> colDate;
    @FXML private TableColumn<Order, String> colTotal;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, String> colRef;
    @FXML private Label welcomeLabel;
    @FXML private Button cartNavButton;

    public TrackOrdersController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPlacedAt().toLocalDate().toString()));
        colTotal.setCellValueFactory(d -> new SimpleStringProperty(
                "£" + String.format("%.2f", d.getValue().getTotalAmount())));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStatus().toString()));
        colRef.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPaymentReference()));

        Long memberId = SessionManager.getCurrentMember().getId();
        List<Order> orders = orderService.getOrdersForMember(memberId);
        ordersTable.setItems(FXCollections.observableArrayList(orders));
        if (SessionManager.isLoggedIn()) {
            welcomeLabel.setText(SessionManager.getCurrentMember().getFirstName());
            int count = cartService.getCartItemCount(memberId);
            cartNavButton.setText("My Cart" + (count > 0 ? "  (" + count + ")" : ""));
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
    private void onAdminClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/admin.fxml");
    }

    @FXML
    private void onCampaignsClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/campaigns.fxml");
    }

    @FXML
    private void onLogoutClicked() {
        SessionManager.clearSession();
        SceneManager.switchTo("/com/ipos/pu/ui/login.fxml");
    }
}
