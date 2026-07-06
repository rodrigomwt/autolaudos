package br.com.vista.rvm.supplier.conferi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConferiRequestPdfDTO {
	private int usuario;
	private String senha;
	
	@JsonProperty("codigo_consulta")
	private Long codigoConsulta;
}