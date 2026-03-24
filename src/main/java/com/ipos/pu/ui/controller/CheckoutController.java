package com.ipos.pu.ui.controller;

import com.ipos.pu.service.CartService;
import com.ipos.pu.service.OrderService;
import com.ipos.pu.ui.SceneManager;
import com.ipos.pu.ui.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class CheckoutController {

    private final OrderService orderService;
    private final CartService cartService;

    @FXML private Label totalLabel;
    @FXML private TextField cardNameField;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private TextField cvvField;
    @FXML private Label messageLabel;

    public CheckoutController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @FXML
    public void initialize() {
        Long memberId = SessionManager.getCurrentMember().getId();
        double total = cartService.getCartTotal(memberId);
        boolean loyalty = cartService.isNextOrderLoyalty(memberId);
        if (loyalty) {
            double discounted = total * 0.90;
            totalLabel.setText("Total: £" + String.format("%.2f", discounted));
            messageLabel.setText("10% loyalty discount applied to this order!");
            messageLabel.setStyle("-fx-text-fill: green;");
        } else {
            totalLabel.setText("Total: £" + String.format("%.2f", total));
        }
    }

    @FXML
    private void onConfirmClicked() {
        if (cardNameField.getText().isBlank() || cardNumberField.getText().isBlank()
                || expiryField.getText().isBlank() || cvvField.getText().isBlank()) {
            messageLabel.setText("Please fill in all payment details.");
            return;
        }

        try {
            Long memberId = SessionManager.getCurrentMember().getId();
            String paymentRef = UUID.randomUUID().toString().substring(0, 12).toUpperCase();
            orderService.placeOrder(memberId, paymentRef);
            SceneManager.switchTo("/com/ipos/pu/ui/track-orders.fxml");
        } catch (Exception e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onBackClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/cart.fxml");
    }
}
