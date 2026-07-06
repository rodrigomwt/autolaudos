package br.com.vista.rvm.supplier.conferi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Parametros {
	private String placa;
	private String produto;
}
