package com.ipos.pu.ui.controller;

import com.ipos.pu.model.Order;
import com.ipos.pu.model.OrderItem;
import com.ipos.pu.repository.OrderItemRepository;
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
    private final OrderItemRepository orderItemRepository;

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> colId;
    @FXML private TableColumn<Order, String> colDate;
    @FXML private TableColumn<Order, String> colTotal;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, String> colRef;
    @FXML private Label welcomeLabel;
    @FXML private Button cartNavButton;
    @FXML private Button campaignsNavBtn;
    @FXML private Button reportsNavBtn;
    @FXML private Button refreshButton;

    @FXML private TableView<OrderItem> itemsTable;
    @FXML private TableColumn<OrderItem, String> colItemProduct;
    @FXML private TableColumn<OrderItem, String> colItemQty;
    @FXML private TableColumn<OrderItem, String> colItemPrice;
    @FXML private TableColumn<OrderItem, String> colItemTotal;
    @FXML private Label orderDetailLabel;

    public TrackOrdersController(OrderService orderService, CartService cartService,
                                  OrderItemRepository orderItemRepository) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.orderItemRepository = orderItemRepository;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPlacedAt().toLocalDate().toString()));
        colTotal.setCellValueFactory(d -> new SimpleStringProperty(
                "\u00a3" + String.format("%.2f", d.getValue().getTotalAmount())));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getStatus().toString()));
        colRef.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPaymentReference()));

        colItemProduct.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getProduct().getName()));
        colItemQty.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getQuantity())));
        colItemPrice.setCellValueFactory(d -> new SimpleStringProperty(
                "\u00a3" + String.format("%.2f", d.getValue().getPriceAtTimeOfOrder())));
        colItemTotal.setCellValueFactory(d -> new SimpleStringProperty(
                "\u00a3" + String.format("%.2f",
                        d.getValue().getQuantity() * d.getValue().getPriceAtTimeOfOrder())));

        ordersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                List<OrderItem> items = orderItemRepository.findByOrder(newVal);
                itemsTable.setItems(FXCollections.observableArrayList(items));
                String address = newVal.getDeliveryAddress() == null ? "" :
                        "  |  Delivery: " + newVal.getDeliveryAddress().replace("\n", ", ");
                orderDetailLabel.setText("Order #" + newVal.getId() + " - "
                        + newVal.getStatus() + " - Placed: "
                        + newVal.getPlacedAt().toLocalDate() + address);
            } else {
                itemsTable.setItems(FXCollections.emptyObservableList());
                orderDetailLabel.setText("Select an order above to view its items.");
            }
        });

        Long memberId = SessionManager.getCurrentMember().getId();
        List<Order> orders = orderService.getOrdersForMember(memberId);
        ordersTable.setItems(FXCollections.observableArrayList(orders));
        if (SessionManager.isLoggedIn()) {
            welcomeLabel.setText(SessionManager.getCurrentMember().getFirstName());
            int count = cartService.getCartItemCount(memberId);
            cartNavButton.setText("My Cart" + (count > 0 ? "  (" + count + ")" : ""));
        }
        if (!SessionManager.isAdmin()) {
            campaignsNavBtn.setVisible(false); campaignsNavBtn.setManaged(false);
            reportsNavBtn.setVisible(false); reportsNavBtn.setManaged(false);
        }
    }

    @FXML
    private void onRefreshClicked() {
        Long memberId = SessionManager.getCurrentMember().getId();
        List<Order> orders = orderService.getOrdersForMember(memberId);
        ordersTable.setItems(FXCollections.observableArrayList(orders));
        itemsTable.setItems(FXCollections.emptyObservableList());
        orderDetailLabel.setText("Select an order above to view its items.");
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
