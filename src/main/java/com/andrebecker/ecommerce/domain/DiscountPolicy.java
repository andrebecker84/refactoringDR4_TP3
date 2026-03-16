package com.andrebecker.ecommerce.domain;

public final class DiscountPolicy {

    private final double rate;

    public DiscountPolicy(double rate) {
        if (rate < 0 || rate >= 1) throw new IllegalArgumentException("taxa deve estar em [0, 1)");
        this.rate = rate;
    }

    public double rate() { return rate; }

    public double discountAmount(double subtotal) {
        return subtotal * rate;
    }

    public double finalTotal(double subtotal) {
        return subtotal * (1 - rate);
    }
}