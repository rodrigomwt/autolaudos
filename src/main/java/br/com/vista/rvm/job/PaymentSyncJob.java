package br.com.vista.rvm.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.vista.rvm.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSyncJob {

    private final PaymentService paymentService;

    @Scheduled(fixedDelay = 120000) // 2 minutos
    public void syncPendingPayments() {
        log.info("⏰ Job iniciado: sincronização de pagamentos pendentes");
        paymentService.syncPendingPayments();
        log.info("✅ Job concluído");
    }
}