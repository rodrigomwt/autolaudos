package br.com.vista.rvm.dto.enums;

public enum Plans {
	PREMIUM("premium", "Auto Laudos Premium", "65", 67.00, true),
	COMPLETA("completa", "Auto Laudos Completa", "66", 57.00, false),
	ESSENCIAL("essencial", "Auto Laudos Essencial", "67", 47.00, false);

	private final String id;
	private final String nome;
	private final String code;
	private final double preco;
	private final boolean recomendado;

	Plans(String id, String nome, String code, double preco, boolean recomendado) {
		this.id = id;
		this.nome = nome;
		this.code = code;
		this.preco = preco;
		this.recomendado = recomendado;
	}

	public static Plans findById(String id) {
		for (Plans p : values()) {
			if (p.getId().equalsIgnoreCase(id)) {
				return p;
			}
		}
		return null;
	}

	public String getDescription() {
		return String.format("%s - R$ %.2f", nome, preco);
	}

	// getters
	public String getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public String getCode() {
		return code;
	}

	public double getPreco() {
		return preco;
	}

	public boolean isRecomendado() {
		return recomendado;
	}
}
