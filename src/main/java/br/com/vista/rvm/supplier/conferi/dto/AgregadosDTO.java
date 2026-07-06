package br.com.vista.rvm.supplier.conferi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AgregadosDTO {
	private String marca;
	private String modelo;
	private String anoFabricacao;
	private String anoModelo;
	private String cor;
	private String renavam;
	private String chassi;
	private String placa;
}
