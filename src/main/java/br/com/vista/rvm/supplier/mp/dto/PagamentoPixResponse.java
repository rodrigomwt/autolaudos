package br.com.vista.rvm.supplier.mp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PagamentoPixResponse {
    private Long paymentId;
    private String status;
    private String qrCode;       // copia e cola
    private String qrCodeBase64; // imagem renderizável
}