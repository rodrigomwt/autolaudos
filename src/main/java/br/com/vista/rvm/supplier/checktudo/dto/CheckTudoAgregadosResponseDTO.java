package br.com.vista.rvm.supplier.checktudo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckTudoAgregadosResponseDTO {
	private Body body;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Body {
		private VehicleData data;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class VehicleData {
		private String placa;
		private String chassi;
		private String anoFabricacao;
		private String anoModelo;
		private String marca;
		private String modelo;
		private String corVeiculo;
	}
}
