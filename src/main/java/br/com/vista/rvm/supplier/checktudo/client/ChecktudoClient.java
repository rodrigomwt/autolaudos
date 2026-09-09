package br.com.vista.rvm.supplier.checktudo.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import br.com.vista.rvm.config.FeignConfig;
import br.com.vista.rvm.supplier.checktudo.dto.CheckTudoRequestDTO;
import br.com.vista.rvm.supplier.checktudo.dto.CheckTudoAgregadosResponseDTO;
import br.com.vista.rvm.supplier.checktudo.dto.CheckTudoLoginRequestDTO;
import br.com.vista.rvm.supplier.checktudo.dto.CheckTudoLoginResponseDTO;
import br.com.vista.rvm.supplier.checktudo.dto.CheckTudoReportResponseDTO;

@FeignClient(name = "checktudo-api", url = "${api.checktudo.url}", configuration = FeignConfig.class)
public interface ChecktudoClient {

	@PostMapping(value = "/auth/login", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	CheckTudoLoginResponseDTO login(@RequestBody CheckTudoLoginRequestDTO request);

	@PostMapping(value = "/api/vehicle/{userId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	CheckTudoAgregadosResponseDTO requestAgregados(@RequestHeader("authorization") String token, @PathVariable String userId, @RequestBody CheckTudoRequestDTO request);
	
	@PostMapping(value = "/api/query/order", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	CheckTudoReportResponseDTO requestReport(@RequestHeader("authorization") String token, @RequestBody CheckTudoRequestDTO request);

}