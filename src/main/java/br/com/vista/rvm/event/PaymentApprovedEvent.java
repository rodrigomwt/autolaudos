package br.com.vista.rvm.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PaymentApprovedEvent extends ApplicationEvent {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Long paymentId;

    public PaymentApprovedEvent(Object source, Long paymentId) {
        super(source);
        this.paymentId = paymentId;
    }
}