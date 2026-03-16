package com.andrebecker.ecommerce.domain;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemTest {

    @Test
    void subtotalIsQuantityTimesPrice() {
        Item item = new Item("Notebook", 2, 1500.0);
        assertThat(item.subtotal(), is(3000.0));
    }

    @Test
    void subtotalWithUnitQuantityEqualsPrice() {
        Item item = new Item("Mouse", 1, 80.0);
        assertThat(item.subtotal(), is(80.0));
    }

    @Test
    void subtotalWithDecimalPrice() {
        Item item = new Item("Cabo USB", 3, 29.99);
        assertThat(item.subtotal(), is(closeTo(89.97, 0.001)));
    }

    @Test
    void constructorRejectsZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new Item("Produto", 0, 10.0));
    }

    @Test
    void constructorRejectsNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> new Item("Produto", -1, 10.0));
    }

    @Test
    void constructorRejectsNegativePrice() {
        assertThrows(IllegalArgumentException.class, () -> new Item("Produto", 1, -0.01));
    }

    @Test
    void constructorRejectsBlankProduct() {
        assertThrows(IllegalArgumentException.class, () -> new Item("  ", 1, 10.0));
    }
}