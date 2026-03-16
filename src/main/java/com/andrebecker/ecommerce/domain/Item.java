package com.andrebecker.ecommerce.domain;

public final class Item {

    private final String product;
    private final int quantity;
    private final double price;

    public Item(String product, int quantity, double price) {
        if (product == null || product.isBlank()) throw new IllegalArgumentException("produto é obrigatório");
        if (quantity <= 0) throw new IllegalArgumentException("quantidade deve ser positiva");
        if (price < 0) throw new IllegalArgumentException("preço não pode ser negativo");
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    public String product() { return product; }
    public int quantity() { return quantity; }
    public double price() { return price; }

    public double subtotal() {
        return quantity * price;
    }
}