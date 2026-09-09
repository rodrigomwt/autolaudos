package br.com.vista.rvm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.vista.rvm.entity.Order;
import br.com.vista.rvm.entity.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findByUserId(Long userId);

	Optional<Order> findByPaymentId(Long paymentId);

	Optional<Order> findByQueryCode(String queryCode);

	Page<Order> findByUserId(Long userId, Pageable pageable);

	Page<Order> findByUserIdAndLicensePlateContainingIgnoreCase(Long userId, String licensePlate, Pageable pageable);
	
	List<Order> findByUserIdAndLicensePlateAndStatus(Long userId, String licensePlate, OrderStatus status);
	
	@Query(value = "SELECT * FROM orders o WHERE o.query_result->>'$.body.orderId' = :orderId AND o.status = :status", nativeQuery = true)
	Optional<Order> findByQueryResultOrderIdAndStatus(@Param("orderId") String externalOderId, @Param("status") String status);
}