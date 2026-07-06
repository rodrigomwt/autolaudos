package br.com.vista.rvm.supplier.checktudo.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckTudoAgregadosRequestDTO {

	private int querycode = 1;
	private Map<String, String> keys;
	private boolean duplicity = true;

	public CheckTudoAgregadosRequestDTO(String placa) {
		this.keys = Map.of("placa", placa);
	}

}
