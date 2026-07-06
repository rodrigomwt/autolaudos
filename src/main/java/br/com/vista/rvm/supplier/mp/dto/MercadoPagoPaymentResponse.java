package br.com.vista.rvm.supplier.mp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MercadoPagoPaymentResponse {

    private Long id;
    private String status;

    @JsonProperty("status_detail")
    private String statusDetail;

    @JsonProperty("point_of_interaction")
    private PointOfInteraction pointOfInteraction;

    @Data
    public static class PointOfInteraction {

        @JsonProperty("transaction_data")
        private TransactionData transactionData;

        @Data
        public static class TransactionData {

            @JsonProperty("qr_code")
            private String qrCode; // copia e cola

            @JsonProperty("qr_code_base64")
            private String qrCodeBase64; // imagem do QR
        }
    }
}