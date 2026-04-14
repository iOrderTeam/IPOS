package com.ipos.pu.controller;

import com.ipos.pu.model.Payment;
import com.ipos.pu.model.PaymentStatus;
import com.ipos.pu.repository.PaymentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.LinkedHashMap;

// POST /api/payments/ca-clearance
// Called by IPOS-CA when a customer pays by card at the pharmacy.
// CA sends card details and amount; PU approves/declines and records the transaction.
// Marking sheet: "Recording card payments coming from CA" (4 marks)
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostMapping("/ca-clearance")
    public ResponseEntity<Map<String, Object>> clearCardPayment(
            @RequestBody Map<String, Object> body) {

        String cardholderName   = (String) body.getOrDefault("cardholderName", "");
        String firstFourDigits  = (String) body.getOrDefault("firstFourDigits", "");
        String lastFourDigits   = (String) body.getOrDefault("lastFourDigits", "");
        String expiryDate       = (String) body.getOrDefault("expiryDate", "");
        double amount           = ((Number) body.getOrDefault("amount", 0.0)).doubleValue();

        Map<String, Object> response = new LinkedHashMap<>();

        // basic validation
        if (amount <= 0) {
            response.put("approved", false);
            response.put("transactionRef", null);
            response.put("message", "Invalid amount");
            return ResponseEntity.badRequest().body(response);
        }

        // simulate decline: first four "0000" = declined (matches CA's MockPuAdapter convention)
        boolean approved = !"0000".equals(firstFourDigits);

        String maskedCard = firstFourDigits + "-****-****-" + lastFourDigits;
        String txRef      = approved ? "PU-TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() : null;

        // record the payment regardless of outcome
        Payment payment = new Payment();
        payment.setOrder(null); // CA in-person sale — no PU order linked
        payment.setMember(null);
        payment.setCardholderName(cardholderName);
        payment.setMaskedCardNumber(maskedCard);
        payment.setExpiryDate(expiryDate);
        payment.setAmount(amount);
        payment.setPaidAt(LocalDateTime.now());
        payment.setStatus(approved ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        paymentRepository.save(payment);

        response.put("approved", approved);
        response.put("transactionRef", txRef);
        response.put("message", approved ? "Payment approved" : "Card declined by payment processor");

        System.out.println("[CA->PU] Card clearance: " + (approved ? "APPROVED" : "DECLINED")
                + " £" + String.format("%.2f", amount)
                + (txRef != null ? " ref=" + txRef : ""));

        return ResponseEntity.ok(response);
    }
}
