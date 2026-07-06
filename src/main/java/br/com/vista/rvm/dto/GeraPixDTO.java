package br.com.vista.rvm.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class GeraPixDTO {
	private Long userId;
	private String userName;
	private String userEmail;
	private BigDecimal planValue;
	private String planDescription;
	private Long paymentId;
}
