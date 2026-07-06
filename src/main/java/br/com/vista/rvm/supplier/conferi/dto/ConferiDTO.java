package br.com.vista.rvm.supplier.conferi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConferiDTO {
	private AgregadosDTO agregados;
}
