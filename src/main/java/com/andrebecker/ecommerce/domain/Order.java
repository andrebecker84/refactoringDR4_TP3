package com.andrebecker.ecommerce.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Order {

    private final Client client;
    private final DiscountPolicy discountPolicy;
    private final List<Item> items = new ArrayList<>();

    public Order(Client client, DiscountPolicy discountPolicy) {
        if (client == null) throw new IllegalArgumentException("cliente é obrigatório");
        if (discountPolicy == null) throw new IllegalArgumentException("política de desconto é obrigatória");
        this.client = client;
        this.discountPolicy = discountPolicy;
    }

    public void addItem(Item item) {
        if (item == null) throw new IllegalArgumentException("item não pode ser nulo");
        items.add(item);
    }

    public Client client() { return client; }
    public DiscountPolicy discountPolicy() { return discountPolicy; }
    public List<Item> items() { return Collections.unmodifiableList(items); }

    public double grossTotal() {
        return items.stream().mapToDouble(Item::subtotal).sum();
    }

    public double discountAmount() {
        return discountPolicy.discountAmount(grossTotal());
    }

    public double finalTotal() {
        return discountPolicy.finalTotal(grossTotal());
    }
}