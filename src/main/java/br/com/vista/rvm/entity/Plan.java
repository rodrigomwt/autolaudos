package br.com.vista.rvm.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "plans")
public class Plan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "userId", nullable = false)
	private User user;

	@Column(nullable = false, length = 100)
	private String plan;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal planValue;

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