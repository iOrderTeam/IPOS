package com.ipos.pu.ui.controller;

import com.ipos.pu.model.Order;
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

    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, String> colId;
    @FXML private TableColumn<Order, String> colDate;
    @FXML private TableColumn<Order, String> colTotal;
    @FXML private TableColumn<Order, String> colStatus;
    @FXML private TableColumn<Order, String> colRef;

    public TrackOrdersController(OrderService orderService) {
        this.orderService = orderService;
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
    }

    @FXML
    private void onBackClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/main.fxml");
    }
}
