package br.com.vista.rvm.supplier.mp.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MercadoPagoPaymentRequest {

    @JsonProperty("transaction_amount")
    private BigDecimal transactionAmount;

    private String description;

    @JsonProperty("payment_method_id")
    private String paymentMethodId;
    
    @JsonProperty("notification_url")
    private String notificationUrl;

    private PayerRequest payer;

    @Data
    @Builder
    public static class PayerRequest {
        private String email;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;
    }
}