package br.com.vista.rvm.supplier.mp.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriarPagamentoRequest {
    private Long usuarioId;
    private String email;
    private String nome;
    private String sobrenome;
    private BigDecimal valor;
    private String descricao;
}