package com.ipos.pu.ui.controller;

import com.ipos.pu.service.CartService;
import com.ipos.pu.service.OrderService;
import com.ipos.pu.ui.SceneManager;
import com.ipos.pu.ui.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class CheckoutController {

    private final OrderService orderService;
    private final CartService cartService;

    @FXML private Label totalLabel;
    @FXML private TextArea deliveryAddressField;
    @FXML private TextField cardNameField;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private TextField cvvField;
    @FXML private Label messageLabel;
    @FXML private Label welcomeLabel;
    @FXML private Button cartNavButton;
    @FXML private Button campaignsNavBtn;
    @FXML private Button reportsNavBtn;

    public CheckoutController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @FXML
    public void initialize() {
        Long memberId = SessionManager.getCurrentMember().getId();
        double total = cartService.getCartTotal(memberId);
        boolean loyalty = cartService.isNextOrderLoyalty(memberId);
        if (SessionManager.isLoggedIn()) {
            welcomeLabel.setText(SessionManager.getCurrentMember().getFirstName());
            int count = cartService.getCartItemCount(memberId);
            cartNavButton.setText("My Cart" + (count > 0 ? "  (" + count + ")" : ""));
        }
        if (!SessionManager.isAdmin()) {
            campaignsNavBtn.setVisible(false); campaignsNavBtn.setManaged(false);
            reportsNavBtn.setVisible(false); reportsNavBtn.setManaged(false);
        }
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
        String deliveryAddress = deliveryAddressField.getText().trim();
        String name = cardNameField.getText().trim();
        String number = cardNumberField.getText().trim().replaceAll("\\s+", "");
        String expiry = expiryField.getText().trim();
        String cvv = cvvField.getText().trim();

        if (deliveryAddress.isBlank()) {
            showError("Please enter a delivery address.");
            return;
        }
        if (deliveryAddress.length() < 10) {
            showError("Please include street, city and postcode.");
            return;
        }

        // Validate all fields filled
        if (name.isBlank() || number.isBlank() || expiry.isBlank() || cvv.isBlank()) {
            showError("Please fill in all payment details.");
            return;
        }

        // Validate cardholder name (letters and spaces only)
        if (!name.matches("[a-zA-Z ]+")) {
            showError("Cardholder name must contain only letters and spaces.");
            return;
        }

        // Validate card number (16 digits)
        if (!number.matches("\\d{16}")) {
            showError("Card number must be 16 digits.");
            return;
        }

        // Validate expiry format MM/YY and not expired
        if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            showError("Expiry must be in MM/YY format (e.g. 03/27).");
            return;
        }
        try {
            String[] parts = expiry.split("/");
            int month = Integer.parseInt(parts[0]);
            int year = 2000 + Integer.parseInt(parts[1]);
            java.time.YearMonth expiryYM = java.time.YearMonth.of(year, month);
            if (expiryYM.isBefore(java.time.YearMonth.now())) {
                showError("Card has expired.");
                return;
            }
        } catch (Exception e) {
            showError("Invalid expiry date.");
            return;
        }

        // Validate CVV (3 or 4 digits)
        if (!cvv.matches("\\d{3,4}")) {
            showError("CVV must be 3 or 4 digits.");
            return;
        }

        try {
            Long memberId = SessionManager.getCurrentMember().getId();
            String paymentRef = UUID.randomUUID().toString().substring(0, 12).toUpperCase();
            String maskedCard = "****-****-****-" + number.substring(12);
            orderService.placeOrder(memberId, paymentRef, name, maskedCard, expiry, deliveryAddress);
            SceneManager.switchTo("/com/ipos/pu/ui/track-orders.fxml");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void showError(String msg) {
        messageLabel.setText(msg);
        messageLabel.setStyle("-fx-text-fill: red;");
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

    @FXML
    private void onBackClicked() {
        SceneManager.switchTo("/com/ipos/pu/ui/cart.fxml");
    }
}
