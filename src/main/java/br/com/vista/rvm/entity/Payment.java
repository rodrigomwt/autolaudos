package br.com.vista.rvm.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

import br.com.vista.rvm.entity.enums.PaymentStatus;

@Data
@Entity
@Table(name = "payments")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "planId", nullable = false)
	private Plan plan;

	@Column(nullable = false, length = 50)
	private String gateway;

	@Column(length = 255)
	private String externalId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private PaymentStatus status;

	@Column(columnDefinition = "TEXT")
	private String qrCode;

	@Column(columnDefinition = "TEXT")
	private String qrCodeBase64;

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
