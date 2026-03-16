package com.andrebecker.ecommerce.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        Client client = new Client("João", "joao@email.com");
        DiscountPolicy discount = new DiscountPolicy(0.1);
        order = new Order(client, discount);
        order.addItem(new Item("Notebook", 1, 3500.0));
        order.addItem(new Item("Mouse", 2, 80.0));
    }

    @Test
    void grossTotalSumsAllItemSubtotals() {
        // 3500.0 + (2 * 80.0) = 3500.0 + 160.0 = 3660.0
        assertThat(order.grossTotal(), is(3660.0));
    }

    @Test
    void discountAmountIsRateAppliedToGrossTotal() {
        // 3660.0 * 0.1 = 366.0
        assertThat(order.discountAmount(), is(closeTo(366.0, 0.001)));
    }

    @Test
    void finalTotalIsGrossTotalMinusDiscount() {
        // 3660.0 - 366.0 = 3294.0
        assertThat(order.finalTotal(), is(closeTo(3294.0, 0.001)));
    }

    @Test
    void finalTotalCoherentWithGrossTotalAndDiscountRate() {
        double expected = order.grossTotal() * (1 - order.discountPolicy().rate());
        assertThat(order.finalTotal(), is(closeTo(expected, 0.001)));
    }

    @Test
    void itemsListIsImmutable() {
        assertThrows(UnsupportedOperationException.class,
            () -> order.items().add(new Item("Teclado", 1, 200.0)));
    }

    @Test
    void constructorRejectsNullClient() {
        assertThrows(IllegalArgumentException.class,
            () -> new Order(null, new DiscountPolicy(0.0)));
    }

    @Test
    void constructorRejectsNullDiscountPolicy() {
        assertThrows(IllegalArgumentException.class,
            () -> new Order(new Client("Ana", "ana@email.com"), null));
    }
}