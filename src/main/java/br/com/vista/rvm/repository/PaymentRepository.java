package br.com.vista.rvm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.vista.rvm.entity.Payment;
import br.com.vista.rvm.entity.enums.PaymentStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	List<Payment> findByUserId(Long userId);

	List<Payment> findByPlanId(Long planId);

	Optional<Payment> findByExternalId(String externalId);

	List<Payment> findByStatus(PaymentStatus status);
}