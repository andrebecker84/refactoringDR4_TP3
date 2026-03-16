package com.andrebecker.ecommerce.domain;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.IntRange;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;

class OrderPropertyTest {

    @Property
    void grossTotalEqualsSum(
        @ForAll @IntRange(min = 1, max = 10) int qty1,
        @ForAll @DoubleRange(min = 0.01, max = 9999.0) double price1,
        @ForAll @IntRange(min = 1, max = 10) int qty2,
        @ForAll @DoubleRange(min = 0.01, max = 9999.0) double price2
    ) {
        Order order = buildOrder(0.0);
        order.addItem(new Item("Produto A", qty1, price1));
        order.addItem(new Item("Produto B", qty2, price2));

        double expected = qty1 * price1 + qty2 * price2;
        assertThat(order.grossTotal(), is(closeTo(expected, 0.01)));
    }

    @Property
    void finalTotalLessThanGrossTotalWhenDiscountPositive(
        @ForAll @DoubleRange(min = 0.01, max = 0.99) double rate,
        @ForAll @IntRange(min = 1, max = 10) int qty,
        @ForAll @DoubleRange(min = 1.0, max = 9999.0) double price
    ) {
        Order order = buildOrder(rate);
        order.addItem(new Item("Produto", qty, price));

        assertThat(order.finalTotal(), lessThan(order.grossTotal()));
    }

    @Property
    void itemSubtotalNeverNegative(
        @ForAll @IntRange(min = 1, max = 100) int qty,
        @ForAll @DoubleRange(min = 0.0, max = 99999.0) double price
    ) {
        Item item = new Item("Produto", qty, price);
        assertThat(item.subtotal(), greaterThanOrEqualTo(0.0));
    }

    private Order buildOrder(double rate) {
        return new Order(
            new Client("Teste", "teste@email.com"),
            new DiscountPolicy(rate)
        );
    }

    // necessário para resolução do matcher 'is' com Hamcrest
    private static <T> org.hamcrest.Matcher<T> is(org.hamcrest.Matcher<T> matcher) {
        return org.hamcrest.Matchers.is(matcher);
    }
}