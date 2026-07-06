package br.com.vista.rvm.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatusPagamentoResponse {
    private Long paymentId;
    private String status;
    private String statusDetail;
    private boolean aprovado;
}