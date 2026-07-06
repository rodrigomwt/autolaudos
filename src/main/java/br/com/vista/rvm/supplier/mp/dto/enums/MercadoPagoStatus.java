package br.com.vista.rvm.supplier.mp.dto.enums;

import br.com.vista.rvm.entity.enums.PaymentStatus;

public enum MercadoPagoStatus {

	PENDING("pending"), APPROVED("approved"), AUTHORIZED("authorized"), IN_PROCESS("in_process"), IN_MEDIATION("in_mediation"), REJECTED("rejected"),
	CANCELLED("cancelled"), REFUNDED("refunded"), CHARGED_BACK("charged_back");

	private final String value;

	MercadoPagoStatus(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static MercadoPagoStatus fromValue(String value) {
		for (MercadoPagoStatus status : values()) {
			if (status.value.equalsIgnoreCase(value)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Unknown Mercado Pago status: " + value);
	}

	public PaymentStatus toPaymentStatus() {
		return switch (this) {
		case PENDING -> PaymentStatus.PENDING;
		case APPROVED -> PaymentStatus.PAID;
		case AUTHORIZED -> PaymentStatus.AUTHORIZED;
		case IN_PROCESS -> PaymentStatus.IN_REVIEW;
		case IN_MEDIATION -> PaymentStatus.DISPUTE;
		case REJECTED -> PaymentStatus.REJECTED;
		case CANCELLED -> PaymentStatus.CANCELLED;
		case REFUNDED -> PaymentStatus.REFUNDED;
		case CHARGED_BACK -> PaymentStatus.CHARGEDBACK;
		};
	}
}