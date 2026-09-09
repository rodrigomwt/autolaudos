package br.com.vista.rvm.service;

import java.util.List;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.vista.rvm.entity.Order;
import br.com.vista.rvm.entity.enums.OrderStatus;
import br.com.vista.rvm.event.PaymentApprovedEvent;
import br.com.vista.rvm.repository.OrderRepository;
import br.com.vista.rvm.supplier.checktudo.service.CheckTudoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final UserService userService;
	private final PlanService planService;
	private final PaymentService paymentService;
	private final ObjectMapper objectMapper;
	private final CheckTudoService checkTudoService;
	private final EmailService emailService;

	public Order create(Long userId, Long planId, Long paymentId, String licensePlate, String gateway, String product, String code) {
		if (!orderRepository.findByUserIdAndLicensePlateAndStatus(userId, licensePlate, OrderStatus.REQUESTED_REPORT).isEmpty()) {
			throw new RuntimeException("Já existe um pedido com essa placa em processamento. Placa: " + licensePlate);
		}

		var user = userService.findById(userId);
		var plan = planService.findById(planId);
		var payment = paymentService.findById(paymentId);

		Order order = new Order();
		order.setUser(user);
		order.setPlan(plan);
		order.setPayment(payment);
		order.setLicensePlate(licensePlate);
		order.setGateway(gateway);
		order.setProduct(product);
		order.setStatus(OrderStatus.CREATED);
		order.setQueryCode(code);

		return orderRepository.save(order);
	}

	public Optional<Order> findById(Long id) {
		return orderRepository.findById(id);
	}

	public Optional<Order> findByPaymentId(Long paymentId) {
		return orderRepository.findByPaymentId(paymentId);
	}

	public Page<Order> findByUserId(Long userId, Pageable pageable) {
		return orderRepository.findByUserId(userId, pageable);
	}

	public Page<Order> findByUserIdAndLicensePlate(Long userId, String licensePlate, Pageable pageable) {
		return orderRepository.findByUserIdAndLicensePlateContainingIgnoreCase(userId, licensePlate, pageable);
	}

	public void finishedOrderByExternalOrderId(String orderId, String queryResult) {
		var order = orderRepository.findByQueryResultOrderIdAndStatus(orderId, OrderStatus.REQUESTED_REPORT.toString());
    	if(order.isEmpty()) {
    		throw new RuntimeException("Order não encontrada: " + orderId);
    	}
    	
    	order.get().setStatus(OrderStatus.FINISHED);
    	order.get().setQueryResult(queryResult);
		
    	orderRepository.save(order.get());
    	
    	var user = userService.findById(order.get().getUser().getId());
    	emailService.sendOrderFinished(user.getEmail(), order.get().getLicensePlate());
	}

	public void requestReport(Long orderId) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order não encontrada: " + orderId));

		try {
			var report = checkTudoService.requestReport(order.getLicensePlate().replaceAll("[^a-zA-Z0-9]", ""), Integer.parseInt(order.getQueryCode()));

			if (report.getStatus().getCod() == 200) {
				order.setStatus(OrderStatus.REQUESTED_REPORT);
			} else { 
				order.setStatus(OrderStatus.ERROR);
			}
			
			order.setQueryResult(toJson(report));
			orderRepository.save(order);
		} catch (Exception e) {
			log.error("Erro ao gerar laudo para orderId={}: {}", orderId, e.getMessage(), e);

			throw new RuntimeException("Erro ao buscar laudo", e);
		}
	}

	public String showReport(Long orderId) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order não encontrada: " + orderId));

//		if (order.getQueryCode() == null || order.getQueryCode().isBlank()) {
//			throw new RuntimeException("Order sem código de consulta: " + orderId);
//		}

//		if(order.getQueryResult() == null) { 
//			var report = checkTudoService.requestReport(order.getLicensePlate().replace("-", ""), Integer.parseInt(order.getQueryCode()));
//			order.setQueryResult(report);
//			orderRepository.save(order);
//		}

		return order.getQueryResult();
	}
	
	public String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Erro ao converter objeto para JSON: {}", e.getMessage(), e);
            return null;
        }
    }

	@EventListener
	public void onPaymentApproved(PaymentApprovedEvent event) {
		var order = findByPaymentId(event.getPaymentId());

		if (!order.isEmpty()) {
			requestReport(order.get().getId());
		}
	}

	public List<Order> findAll() {
		return orderRepository.findAll();
	}

	public void deleteById(Long id) {
		orderRepository.deleteById(id);
	}
	
	public Optional<Order> findByQueryResultOrderIdAndStatus(String externalOderId, String status) {
		return orderRepository.findByQueryResultOrderIdAndStatus(externalOderId, status);
	}
	
}