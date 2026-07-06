package br.com.vista.rvm.supplier.checktudo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckTudoLoginResponseDTO {

	private Body body;
	
	@Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private String token;
        private User user;
    }
	
	@Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        @JsonProperty("_id")
        private String id;
    }
}
