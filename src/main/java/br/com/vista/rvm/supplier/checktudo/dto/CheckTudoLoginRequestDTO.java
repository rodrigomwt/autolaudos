package br.com.vista.rvm.supplier.checktudo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckTudoLoginRequestDTO {
	private String username;
	private String password;
}
