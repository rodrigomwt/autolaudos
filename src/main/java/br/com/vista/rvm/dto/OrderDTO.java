package br.com.vista.rvm.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.vista.rvm.entity.Order;
import br.com.vista.rvm.entity.enums.OrderStatus;
import lombok.Getter;

@Getter
public class OrderDTO {

    private final Long id;
    private final String licensePlate;
    private final String plan;
    private final String paymentStatus;
    private final String paymentStatusLabel;
    private final String paymentStatusBadgeClass;
    private final OrderStatus orderStatus;
    private final String orderStatusLabel;
    private final String orderStatusBadgeClass;
    private final LocalDateTime createDate;

    // Campos para geração do PIX
    private final Long paymentId;
    private final Long userId;
    private final String userEmail;
    private final String userName;
    private final BigDecimal planValue;
    private final String planDescription;

    public OrderDTO(Order order) {
        this.id = order.getId();
        this.licensePlate = order.getLicensePlate();
        this.plan = order.getPlan().getPlan();
        this.orderStatus = order.getStatus();
        this.createDate = order.getCreateDate();

        // PIX fields
        this.paymentId = order.getPayment().getId();
        this.userId = order.getUser().getId();
        this.userEmail = order.getUser().getEmail();
        this.userName = order.getUser().getName();
        this.planValue = order.getPlan().getPlanValue();
        this.planDescription = order.getPlan().getPlan();

        // Payment
        String ps = order.getPayment().getStatus().name();
        this.paymentStatus = ps;
        this.paymentStatusLabel = switch (ps) {
            case "APPROVED" -> "Aprovado";
            case "PENDING"  -> "Pendente";
            case "PAID"     -> "Pago";
            case "REJECTED" -> "Rejeitado";
            case "CANCELLED"-> "Cancelado";
            default -> ps;
        };
        this.paymentStatusBadgeClass = switch (ps) {
            case "APPROVED" -> "badge-success";
            case "PENDING"  -> "badge-warning";
            case "REJECTED", "CANCELLED" -> "badge-danger";
            default -> "badge-info";
        };

        // Order
        this.orderStatusLabel = switch (order.getStatus()) {
            case CREATED          -> "Criado";
            case REQUESTED_REPORT -> "Processando";
            case FINISHED         -> "Finalizado";
            case ERROR            -> "Erro";
        };
        this.orderStatusBadgeClass = switch (order.getStatus()) {
            case CREATED          -> "badge-info";
            case REQUESTED_REPORT -> "badge-warning";
            case FINISHED         -> "badge-success";
            case ERROR            -> "badge-danger";
        };
    }
}