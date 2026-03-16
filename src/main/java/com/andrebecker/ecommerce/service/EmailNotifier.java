package com.andrebecker.ecommerce.service;

import com.andrebecker.ecommerce.domain.Order;
import com.andrebecker.ecommerce.infrastructure.EmailService;

public final class EmailNotifier {

    public void notifyOrderReceived(Order order) {
        EmailService.sendEmail(
            order.client().email(),
            "Pedido recebido! Obrigado pela compra."
        );
    }
}