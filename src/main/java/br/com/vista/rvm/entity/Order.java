package br.com.vista.rvm.entity;

import java.time.LocalDateTime;

import br.com.vista.rvm.entity.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "orders")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "plan_id", nullable = false)
	private Plan plan;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id", nullable = false)
	private Payment payment;
	
	@Column(name = "license_plate", nullable = false)
	private String licensePlate;

	@Column(nullable = false)
	private String gateway;

	@Column(nullable = false)
	private String product;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus status;

	@Column(name = "query_code")
	private String queryCode;

	@Column(name = "query_result", columnDefinition = "TEXT")
	private String queryResult;

	@Column(name = "create_date", nullable = false, updatable = false)
	private LocalDateTime createDate;

	@Column(name = "update_date", nullable = false)
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