package br.com.vista.rvm.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import br.com.vista.rvm.dto.GeraPixDTO;
import br.com.vista.rvm.dto.UserDTO;
import br.com.vista.rvm.dto.enums.Plans;
import br.com.vista.rvm.entity.enums.OrderStatus;
import br.com.vista.rvm.entity.enums.PaymentStatus;
import br.com.vista.rvm.service.OrderService;
import br.com.vista.rvm.service.PaymentService;
import br.com.vista.rvm.service.PlanService;
import br.com.vista.rvm.service.UserService;
import br.com.vista.rvm.supplier.mp.dto.CriarPagamentoRequest;
import br.com.vista.rvm.supplier.mp.dto.MercadoPagoWebhookDTO;
import br.com.vista.rvm.supplier.mp.dto.enums.MercadoPagoStatus;
import br.com.vista.rvm.supplier.mp.service.MercadoPagoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/pagamento")
@RequiredArgsConstructor
@Slf4j
public class PagamentoController {

	private final MercadoPagoService mercadoPagoService;
	private final UserService userService;
	private final PlanService planService;
	private final PaymentService paymentService;
	private final OrderService orderService;

	@Value("${gateway.report}")
	private String gatewayReport;

	@PostMapping("/gerar-pix")
	public ResponseEntity<?> gerarPix(HttpSession session, @RequestBody UserDTO userDto) {
		var planSession = Plans.findById((String) session.getAttribute("plano"));
		var user = userService.create(userDto.getFullName(), userDto.getFederalId(), userDto.getEmail(), userDto.getPhone());
		String licensePlate = (String) session.getAttribute("placa");

		var request = CriarPagamentoRequest.builder().usuarioId(user.getId()).email(user.getEmail()).nome(user.getName()).sobrenome(user.getName())
				.valor(BigDecimal.valueOf(planSession.getPreco())).descricao(planSession.getDescription()).build();

		try {
			var pixResponse = mercadoPagoService.criarPagamentoPix(request);
			var plan = planService.create(user.getId(), planSession.getNome(), planSession.getNome(), BigDecimal.valueOf(planSession.getPreco()));
			var payment = paymentService.create(user.getId(), plan.getId(), String.valueOf(pixResponse.getPaymentId()), pixResponse.getQrCode(), pixResponse.getQrCodeBase64());
			orderService.create(user.getId(), plan.getId(), payment.getId(), licensePlate, gatewayReport, planSession.getNome(), planSession.getCode());

			return ResponseEntity.ok(Map.of("qrCodeBase64", pixResponse.getQrCodeBase64(), "qrCode", pixResponse.getQrCode(), "externalId", String.valueOf(pixResponse.getPaymentId())));

		} catch (Exception e) {
			log.error("Erro ao gerar PIX: ", e);
			return ResponseEntity.badRequest().body(Map.of("error", "Erro ao gerar pagamento"));
		}

	}

	@PostMapping("/gerar-pix-dash")
	public ResponseEntity<?> gerarPixDash(GeraPixDTO userpix) {

		var request = CriarPagamentoRequest.builder().usuarioId(userpix.getUserId()).email(userpix.getUserEmail()).nome(userpix.getUserName())
				.sobrenome(userpix.getUserName()).valor(userpix.getPlanValue()).descricao(userpix.getPlanDescription()).build();

		try {
			var pixResponse = mercadoPagoService.criarPagamentoPix(request);

			// Salva externalId + QR Code no Payment existente
			paymentService.updateExternalId(userpix.getPaymentId(), String.valueOf(pixResponse.getPaymentId()), pixResponse.getQrCode(),
					pixResponse.getQrCodeBase64());

			return ResponseEntity.ok(Map.of("qrCodeBase64", pixResponse.getQrCodeBase64(), "qrCode", pixResponse.getQrCode(), "externalId",
					String.valueOf(pixResponse.getPaymentId())));

		} catch (Exception e) {
			log.error("Erro ao gerar PIX: ", e);
			return ResponseEntity.badRequest().body(Map.of("error", "Erro ao gerar pagamento"));
		}
	}

	@PostMapping("/notification")
	public ResponseEntity<Void> receberNotificacao(@RequestBody MercadoPagoWebhookDTO payload) {
	    log.info("Webhook recebido: type={}, data={}", payload.getType(), payload.getData());

	    try {
	        if (!"payment".equals(payload.getType())) {
	            return ResponseEntity.ok().build();
	        }

	        String externalId = payload.getData().getId();

	        var statusResponse = mercadoPagoService.consultarStatusPagamento(Long.parseLong(externalId));
	        var paymentStatus = MercadoPagoStatus.fromValue(statusResponse.getStatus()).toPaymentStatus();
	        var payment = paymentService.updateStatus(externalId, paymentStatus);

	        // ✅ só gera laudo se APPROVED e ainda não foi processado
	        if (paymentStatus == PaymentStatus.PAID) {
	            var order = orderService.findByPaymentId(payment.getId());

	            if (order.isEmpty()) {
	                log.warn("Nenhuma order encontrada para paymentId={}", payment.getId());
	                return ResponseEntity.ok().build();
	            }

	            if (order.get().getStatus() == OrderStatus.FINISHED) {
	                log.info("Order id={} já foi processada, ignorando webhook.", order.get().getId());
	                return ResponseEntity.ok().build(); // ✅ idempotência
	            }

	            orderService.requestReport(order.get().getId());
	        }

	    } catch (Exception e) {
	        // ✅ SEMPRE retorna 200 pro MP, senão ele fica retentando
	        log.error("Erro ao processar webhook: {}", e.getMessage(), e);
	    }

	    return ResponseEntity.ok().build();
	}
}