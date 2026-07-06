package br.com.vista.rvm;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;

@EnableScheduling
@SpringBootApplication
@EnableFeignClients
public class RvmApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(RvmApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(RvmApplication.class, args);
	}
	
	@PostConstruct
	public void init() {
	    TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
	}

}