package br.com.vista.rvm.supplier.checktudo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckTudoReportResponseDTO {
	private Status status;
	private Body body;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Status {
		private Integer cod;
		private String msg;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Body {
		private String orderId;
		private String queryId;
		private String requesterId;
		private String status;
		private String createdAt;
	}
}
