package com.andrebecker.ecommerce.domain;

public final class Client {

    private final String name;
    private final String email;

    public Client(String name, String email) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("nome do cliente é obrigatório");
        if (email == null || email.isBlank()) throw new IllegalArgumentException("e-mail do cliente é obrigatório");
        this.name = name;
        this.email = email;
    }

    public String name() { return name; }
    public String email() { return email; }
}