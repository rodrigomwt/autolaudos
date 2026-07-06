package br.com.vista.rvm.supplier.mp.dto;

import lombok.Data;

@Data
public class MercadoPagoWebhookDTO {
	private String type;
	private String action;
	private MercadoPagoWebhookDataDTO data;

	@Data
	public static class MercadoPagoWebhookDataDTO {
		private String id;
	}
}