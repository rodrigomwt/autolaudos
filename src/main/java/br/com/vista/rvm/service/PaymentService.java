package br.com.vista.rvm.service;

import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import br.com.vista.rvm.entity.Payment;
import br.com.vista.rvm.entity.Plan;
import br.com.vista.rvm.entity.User;
import br.com.vista.rvm.entity.enums.PaymentStatus;
import br.com.vista.rvm.event.PaymentApprovedEvent;
import br.com.vista.rvm.repository.PaymentRepository;
import br.com.vista.rvm.supplier.mp.service.MercadoPagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserService userService;
    private final PlanService planService;
    private final MercadoPagoService mercadoPagoService;
    private final ApplicationEventPublisher eventPublisher;

    public Payment create(Long userId, Long planId, String externalId, String qrCode, String qrCodeBase64) {
        User user = userService.findById(userId);
        Plan plan = planService.findById(planId);

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setPlan(plan);
        payment.setGateway("mercado_pago");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setExternalId(externalId);
        payment.setQrCode(qrCode);
        payment.setQrCodeBase64(qrCodeBase64);

        log.info("Criando payment para userId={}, planId={}", userId, planId);

        return paymentRepository.save(payment);
    }

    public Payment updateExternalId(Long paymentId, String externalId, String qrCode, String qrCodeBase64) {
        Payment payment = findById(paymentId);
        payment.setExternalId(externalId);
        payment.setQrCode(qrCode);
        payment.setQrCodeBase64(qrCodeBase64);
        return paymentRepository.save(payment);
    }

    public Payment updateStatus(String externalId, PaymentStatus status) {
        Payment payment = paymentRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Payment não encontrado: " + externalId));
        payment.setStatus(status);
        log.info("Status do payment externalId={} atualizado para {}", externalId, status);
        return paymentRepository.save(payment);
    }

    // ✅ Orquestra: busca pendentes no banco → consulta MP → atualiza status
    public void syncPendingPayments() {
        List<Payment> pendings = paymentRepository.findByStatus(PaymentStatus.PENDING);

        if (pendings.isEmpty()) {
            log.info("Nenhum pagamento pendente para sincronizar.");
            return;
        }

        log.info("Sincronizando {} pagamento(s) pendente(s)...", pendings.size());

        for (Payment payment : pendings) {
            try {
                if (payment.getExternalId() == null) continue;

                var statusResponse = mercadoPagoService.consultarStatusPagamento(
                        Long.parseLong(payment.getExternalId())
                );
                
                log.info("🔍 Payment id={} | status MP bruto='{}'", payment.getId(), statusResponse.getStatus());

                PaymentStatus newStatus = mapStatus(statusResponse.getStatus());

                if (newStatus != payment.getStatus()) {
                    log.info("Payment id={} atualizado: {} → {}", payment.getId(), payment.getStatus(), newStatus);
                    payment.setStatus(newStatus);
                    paymentRepository.save(payment);
                    
                    if (newStatus == PaymentStatus.PAID) {
                        eventPublisher.publishEvent(
                            new PaymentApprovedEvent(this, payment.getId())
                        );
                    }
                }

            } catch (Exception e) {
                log.error("Erro ao sincronizar payment id={}: {}", payment.getId(), e.getMessage());
            }
        }
    }

    private PaymentStatus mapStatus(String gatewayStatus) {
        return switch (gatewayStatus) {
            case "approved"              -> PaymentStatus.PAID;
            case "rejected", "cancelled" -> PaymentStatus.REJECTED;
            case "refunded"              -> PaymentStatus.REFUNDED;
            case "in_process", "pending" -> PaymentStatus.PENDING;
            default                      -> PaymentStatus.PENDING;
        };
    }

    public Payment findById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + id));
    }

    public Payment findByExternalId(String externalId) {
        return paymentRepository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Payment not found for externalId: " + externalId));
    }

    public List<Payment> findByUserId(Long userId) {
        userService.findById(userId);
        return paymentRepository.findByUserId(userId);
    }

    public List<Payment> findByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    public void delete(Long id) {
        findById(id);
        paymentRepository.deleteById(id);
    }
}