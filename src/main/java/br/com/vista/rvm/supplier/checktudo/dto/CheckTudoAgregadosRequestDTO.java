package br.com.vista.rvm.supplier.checktudo.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckTudoAgregadosRequestDTO {

	private int querycode;
	private Map<String, String> keys;
	private boolean duplicity = true;

	public CheckTudoAgregadosRequestDTO(String placa, int querycode) {
		this.keys = Map.of("placa", placa);
		this.querycode = querycode;
	}

}
