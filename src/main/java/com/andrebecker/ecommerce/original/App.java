package com.andrebecker.ecommerce.original;

import java.util.*;

public class App {
    public static void main(String[] args) {
        Order order = new Order();
        order.clientName = "João";
        order.clientEmail = "joao@email.com";
        order.products.add("Notebook");
        order.quantities.add(1);
        order.prices.add(3500.0);
        order.products.add("Mouse");
        order.quantities.add(2);
        order.prices.add(80.0);
        order.printInvoice();
        order.sendEmail();
    }
}

class Order {
    public String clientName;
    public String clientEmail;
    public List products = new ArrayList<>();
    public List quantities = new ArrayList<>();
    public List prices = new ArrayList<>();
    public double discountRate = 0.1;

    public void printInvoice() {
        double total = 0;
        System.out.println("Cliente: " + clientName);
        for (int i = 0; i < products.size(); i++) {
            System.out.println(quantities.get(i) + "x " + products.get(i) + " - R$" + prices.get(i));
            total += (double) prices.get(i) * (int) quantities.get(i);
        }
        System.out.println("Subtotal: R$" + total);
        System.out.println("Desconto: R$" + (total * discountRate));
        System.out.println("Total final: R$" + (total * (1 - discountRate)));
    }

    public void sendEmail() {
        EmailService.sendEmail(clientEmail, "Pedido recebido! Obrigado pela compra.");
    }
}

class EmailService {
    public static void sendEmail(String to, String message) {
        System.out.println("Enviando e-mail para " + to + ": " + message);
    }
}

class DiscountPolicy {
    public static double calculateDiscount(double amount, double rate) {
        return amount * rate;
    }
}