package br.com.vista.rvm.supplier.conferi.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.fasterxml.jackson.databind.JsonNode;

import br.com.vista.rvm.config.FeignConfig;
import br.com.vista.rvm.supplier.conferi.dto.ConferiPdfResponseDTO;
import br.com.vista.rvm.supplier.conferi.dto.ConferiRequestDTO;
import br.com.vista.rvm.supplier.conferi.dto.ConferiRequestPdfDTO;
import br.com.vista.rvm.supplier.conferi.dto.ConferiResponseDTO;

@FeignClient(name = "conferi-api", url = "${api.conferi.url}", configuration = FeignConfig.class)
public interface ConferiClient {

	@PostMapping(value = "${api.conferi.agregados.url}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	ConferiResponseDTO consultarAgregados(@RequestBody ConferiRequestDTO request);

	@PostMapping(value = "${api.conferi.laudos.url}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	JsonNode consultaLaudos(@RequestBody ConferiRequestDTO request);

	@PostMapping(value = "${api.conferi.pdf.url}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	ConferiPdfResponseDTO geraPdf(@RequestBody ConferiRequestPdfDTO request);
}