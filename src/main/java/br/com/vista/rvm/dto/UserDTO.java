package br.com.vista.rvm.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {
	private String fullName;
	private String federalId;
	private String phone;
	private String email;
	private String password;
}
