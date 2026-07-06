package br.com.vista.rvm.supplier.mp.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.vista.rvm.dto.StatusPagamentoResponse;
import br.com.vista.rvm.supplier.mp.client.MercadoPagoClient;
import br.com.vista.rvm.supplier.mp.dto.CriarPagamentoRequest;
import br.com.vista.rvm.supplier.mp.dto.MercadoPagoPaymentRequest;
import br.com.vista.rvm.supplier.mp.dto.MercadoPagoPaymentResponse;
import br.com.vista.rvm.supplier.mp.dto.PagamentoPixResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoService {

    private final MercadoPagoClient mercadoPagoClient;
    
    @Value("${app.url}")
    private String appUrl;

    public PagamentoPixResponse criarPagamentoPix(CriarPagamentoRequest request) {
        log.info("Criando pagamento PIX para o usuário: {}", request.getEmail());

        MercadoPagoPaymentRequest paymentRequest = MercadoPagoPaymentRequest.builder()
                .transactionAmount(request.getValor())
                .description(request.getDescricao())
                .paymentMethodId("pix")
                .notificationUrl(appUrl + "/pagamento/notification") // ✅
                .payer(MercadoPagoPaymentRequest.PayerRequest.builder()
                        .email(request.getEmail())
                        .firstName(request.getNome())
                        .lastName(request.getSobrenome())
                        .build())
                .build();

        String idempotencyKey = UUID.randomUUID().toString();

        MercadoPagoPaymentResponse response = mercadoPagoClient.criarPagamento(idempotencyKey, paymentRequest);

        log.info("Pagamento criado com sucesso. ID: {}, Status: {}", response.getId(), response.getStatus());

        return mapearParaPixResponse(response);
    }

    public StatusPagamentoResponse consultarStatusPagamento(Long paymentId) {
        log.info("Consultando status do pagamento ID: {}", paymentId);

        MercadoPagoPaymentResponse response = mercadoPagoClient.consultarPagamento(paymentId);

        return StatusPagamentoResponse.builder()
                .paymentId(response.getId())
                .status(response.getStatus())
                .statusDetail(response.getStatusDetail())
                .aprovado("approved".equals(response.getStatus()))
                .build();
    }

    private PagamentoPixResponse mapearParaPixResponse(MercadoPagoPaymentResponse response) {
        var transactionData = response.getPointOfInteraction().getTransactionData();

        return PagamentoPixResponse.builder()
                .paymentId(response.getId())
                .status(response.getStatus())
                .qrCode(transactionData.getQrCode())
                .qrCodeBase64(transactionData.getQrCodeBase64())
                .build();
    }
}