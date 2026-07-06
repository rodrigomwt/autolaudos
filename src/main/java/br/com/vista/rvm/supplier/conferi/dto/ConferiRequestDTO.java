package br.com.vista.rvm.supplier.conferi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConferiRequestDTO {
	private int usuario;
	private String senha;
	private Parametros parametros;
}