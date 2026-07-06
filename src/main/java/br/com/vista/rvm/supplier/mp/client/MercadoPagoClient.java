package br.com.vista.rvm.supplier.mp.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import br.com.vista.rvm.supplier.mp.config.MercadoPagoFeignConfig;
import br.com.vista.rvm.supplier.mp.dto.MercadoPagoPaymentRequest;
import br.com.vista.rvm.supplier.mp.dto.MercadoPagoPaymentResponse;

@FeignClient(name = "mercadoPagoClient", url = "${mercadopago.api-url}", configuration = MercadoPagoFeignConfig.class)
public interface MercadoPagoClient {

	@PostMapping(value = "/v1/payments", consumes = MediaType.APPLICATION_JSON_VALUE)
	MercadoPagoPaymentResponse criarPagamento(@RequestHeader("X-Idempotency-Key") String idempotencyKey,
			@RequestBody MercadoPagoPaymentRequest request);

	@GetMapping("/v1/payments/{id}")
	MercadoPagoPaymentResponse consultarPagamento(@PathVariable("id") Long paymentId);
}