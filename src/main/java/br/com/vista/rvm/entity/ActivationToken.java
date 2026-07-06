package br.com.vista.rvm.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "activation_tokens")
public class ActivationToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String token;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private LocalDateTime expiration;

	@Column(nullable = false)
	private Boolean used = false;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createDate;

	@PrePersist
	public void prePersist() {
		createDate = LocalDateTime.now();
	}
}