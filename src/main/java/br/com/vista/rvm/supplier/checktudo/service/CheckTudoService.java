package br.com.vista.rvm.supplier.checktudo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.vista.rvm.supplier.checktudo.client.ChecktudoClient;
import br.com.vista.rvm.supplier.checktudo.dto.CheckTudoAgregadosResponseDTO;
import br.com.vista.rvm.supplier.checktudo.dto.CheckTudoLoginRequestDTO;
import br.com.vista.rvm.supplier.checktudo.dto.CheckTudoReportResponseDTO;
import br.com.vista.rvm.supplier.checktudo.dto.CheckTudoRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckTudoService {
	
	private final ChecktudoClient checkTudoClient;
	
	@Value("${api.checktudo.usuario}")
	private String usuario;

	@Value("${api.checktudo.senha}")
	private String senha;
	
	@Value("${api.checktudo.url}")
	private String baseUrl;
	
	public CheckTudoAgregadosResponseDTO requestAgregados(String placa) {
        var login = checkTudoClient.login(
                CheckTudoLoginRequestDTO.builder().username(usuario).password(senha).build()
        );

        String token = login.getBody().getToken();
        String userId = login.getBody().getUser().getId();
        
        return checkTudoClient.requestAgregados(token, userId, new CheckTudoRequestDTO(placa, 1));
    }
	
	public CheckTudoReportResponseDTO requestReport(String placa, int queryCode) {
        var login = checkTudoClient.login(CheckTudoLoginRequestDTO.builder().username(usuario).password(senha).build());

        String token = login.getBody().getToken();
        //String userId = login.getBody().getUser().getId();
        
        return checkTudoClient.requestReport(token, new CheckTudoRequestDTO(placa, queryCode));
    }

}
