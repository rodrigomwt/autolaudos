package br.com.vista.rvm.dto;

import lombok.Data;

@Data
public class DefinirSenhaRequest {
    private String token;
    private String senha;
}