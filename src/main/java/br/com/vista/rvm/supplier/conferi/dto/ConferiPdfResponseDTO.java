package br.com.vista.rvm.supplier.conferi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConferiPdfResponseDTO {

	private Conferi conferi;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Conferi {
		private Solicitacao solicitacao;
		private Pdf pdf;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Solicitacao {
		private int acao;
		private String mensagem;

		@JsonProperty("data_hora")
		private String dataHora;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Pdf {
		@JsonProperty("codigo_consulta")
		private Long codigoConsulta;

		private String parametro;

		@JsonProperty("tipo_parametro")
		private String tipoParametro;

		@JsonProperty("hash_pesquisa")
		private String hashPesquisa;

		@JsonProperty("link_pdf")
		private String linkPdf;
	}
}