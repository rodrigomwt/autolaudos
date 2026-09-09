package br.com.vista.rvm.supplier.mp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import feign.RequestInterceptor;

public class MercadoPagoFeignConfig {

	@Value("${mercadopago.access-token}")
	private String accessToken;

	@Bean
	public RequestInterceptor mercadoPagoAuthInterceptor() {
		return requestTemplate -> requestTemplate.header("Authorization", "Bearer " + accessToken);
	}
}
