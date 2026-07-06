package br.com.vista.rvm.supplier.conferi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.vista.rvm.supplier.conferi.client.ConferiClient;
import br.com.vista.rvm.supplier.conferi.dto.AgregadosDTO;
import br.com.vista.rvm.supplier.conferi.dto.ConferiPdfResponseDTO;
import br.com.vista.rvm.supplier.conferi.dto.ConferiRequestDTO;
import br.com.vista.rvm.supplier.conferi.dto.ConferiRequestPdfDTO;
import br.com.vista.rvm.supplier.conferi.dto.ConferiResponseDTO;
import br.com.vista.rvm.supplier.conferi.dto.Parametros;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConferiService {
	private final ConferiClient conferiClient;
	private final ObjectMapper objectMapper;

	@Value("${api.conferi.usuario}")
	private int usuario;

	@Value("${api.conferi.senha}")
	private String senha;

	public AgregadosDTO buscarDadosVeiculo(String placa) {
		var request = ConferiRequestDTO.builder().usuario(usuario).senha(senha)
				.parametros(Parametros.builder().placa(placa.replaceAll("[^a-zA-Z0-9]", "").toUpperCase()).build()).build();

		ConferiResponseDTO response = conferiClient.consultarAgregados(request);

		// Validação básica do retorno
		if (response != null && response.getConferi().getAgregados() != null) {
			return response.getConferi().getAgregados();
		}

		throw new RuntimeException("Veículo não encontrado ou erro na API");
	}

	public String buscaLaudo(String placa, String produto) {
	    var request = ConferiRequestDTO.builder().usuario(usuario).senha(senha)
	            .parametros(Parametros.builder()
	                    .placa(placa.replaceAll("[^a-zA-Z0-9]", "").toUpperCase())
	                    .produto(produto)
	                    .build())
	            .build();

	    JsonNode response = conferiClient.consultaLaudos(request);

	    if (response != null) {
	        try {
	            return objectMapper.writeValueAsString(response); // ✅ converte pra String
	        } catch (Exception e) {
	            throw new RuntimeException("Erro ao serializar resposta da Conferi", e);
	        }
	    }

	    throw new RuntimeException("Veículo não encontrado ou erro na API");
	}

	public ConferiPdfResponseDTO geraPdf(Long codigoConsulta) {
		var request = ConferiRequestPdfDTO.builder().usuario(usuario).senha(senha).codigoConsulta(codigoConsulta).build();

		var response = conferiClient.geraPdf(request);

		if (response != null) {
			return response;
		}

		throw new RuntimeException("Veículo não encontrado ou erro na API");
	}
}
