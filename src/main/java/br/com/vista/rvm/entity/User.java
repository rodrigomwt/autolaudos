package br.com.vista.rvm.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;
	
	@Column(nullable = false, length = 20)
	private String federalId;

	@Column(nullable = false, unique = true, length = 150)
	private String email;

	@Column(length = 20)
	private String phone;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createDate;

	@Column(nullable = false)
	private LocalDateTime updateDate;

	@PrePersist
	protected void onCreate() {
		createDate = LocalDateTime.now();
		updateDate = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updateDate = LocalDateTime.now();
	}
}