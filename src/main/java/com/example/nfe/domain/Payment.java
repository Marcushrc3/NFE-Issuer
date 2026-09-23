package com.example.nfe.domain;

import java.util.List;

/**
 * Payment information of the emission (pag), composed of one or more
 * payment form details. Data only: no calculations, no defaults, no
 * relation to {@code Totals.totalValue} is enforced.
 */
public record Payment(List<PaymentDetail> details) {

    public Payment {
        details = List.copyOf(details);
        if (details.isEmpty()) {
            throw new IllegalArgumentException("payment details must not be empty");
        }
    }
}
