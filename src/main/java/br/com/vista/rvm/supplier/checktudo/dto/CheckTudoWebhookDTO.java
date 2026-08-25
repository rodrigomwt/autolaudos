package br.com.vista.rvm.supplier.checktudo.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class CheckTudoWebhookDTO {
	
	private Payload payload;

    @Data
    public static class Payload {
        private Keys keys;
        private JsonNode data;
    }

    @Data
    public static class Keys {
        private String placa;
    }

}
