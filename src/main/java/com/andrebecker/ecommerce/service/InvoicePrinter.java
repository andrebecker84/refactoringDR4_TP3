package com.andrebecker.ecommerce.service;

import com.andrebecker.ecommerce.domain.Item;
import com.andrebecker.ecommerce.domain.Order;

public final class InvoicePrinter {

    public void print(Order order) {
        printHeader(order);
        printLineItems(order);
        printTotals(order);
    }

    private void printHeader(Order order) {
        System.out.println("Cliente: " + order.client().name());
    }

    private void printLineItems(Order order) {
        order.items().forEach(this::printLineItem);
    }

    private void printLineItem(Item item) {
        System.out.printf("%dx %s - R$%.2f%n", item.quantity(), item.product(), item.price());
    }

    private void printTotals(Order order) {
        System.out.printf("Subtotal: R$%.2f%n", order.grossTotal());
        System.out.printf("Desconto: R$%.2f%n", order.discountAmount());
        System.out.printf("Total final: R$%.2f%n", order.finalTotal());
    }
}