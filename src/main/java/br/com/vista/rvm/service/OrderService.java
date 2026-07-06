package br.com.vista.rvm.service;

import java.util.List;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.vista.rvm.entity.Order;
import br.com.vista.rvm.entity.enums.OrderStatus;
import br.com.vista.rvm.event.PaymentApprovedEvent;
import br.com.vista.rvm.repository.OrderRepository;
import br.com.vista.rvm.supplier.conferi.service.ConferiService;
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
	private final ConferiService conferiService;
	private final ObjectMapper objectMapper;

	public Order create(Long userId, Long planId, Long paymentId, String licensePlate, String gateway, String product) {
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

	public Order updateQueryResult(Long orderId, String queryResult) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order não encontrada: " + orderId));
		order.setQueryResult(queryResult);
		return orderRepository.save(order);
	}

	public void gerarLaudo(Long orderId) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order não encontrada: " + orderId));

		try {
			String json = conferiService.buscaLaudo(order.getLicensePlate(), order.getProduct());
			JsonNode root = objectMapper.readTree(json);
			var queryCode = root.path("conferi").path("solicitacao").path("codigoConsulta").asText();

			order.setQueryCode(queryCode);
			order.setQueryResult(json);
			order.setStatus(OrderStatus.FINISHED);
			orderRepository.save(order);
		} catch (Exception e) {
			log.error("Erro ao gerar laudo para orderId={}: {}", orderId, e.getMessage(), e);

			throw new RuntimeException("Erro ao buscar laudo", e);
		}
	}

	public String gerarLaudoPdf(Long orderId) {
		Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order não encontrada: " + orderId));

		if (order.getQueryCode() == null || order.getQueryCode().isBlank()) {
			throw new RuntimeException("Order sem código de consulta: " + orderId);
		}

		var response = conferiService.geraPdf(Long.parseLong(order.getQueryCode()));

		var conferi = response.getConferi();
		if (conferi == null || conferi.getPdf() == null || conferi.getPdf().getLinkPdf() == null) {
			return null; // laudo ainda não disponível
		}

		return conferi.getPdf().getLinkPdf();
	}

	@EventListener
	public void onPaymentApproved(PaymentApprovedEvent event) {
		var order = findByPaymentId(event.getPaymentId());

		if (!order.isEmpty()) {
			gerarLaudo(order.get().getId());
		}
	}

	public List<Order> findAll() {
		return orderRepository.findAll();
	}

	public void deleteById(Long id) {
		orderRepository.deleteById(id);
	}
}