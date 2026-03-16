/*
 * Avaliação Final de Design
 *
 * Encapsulamento e integridade dos dados:
 * Todos os atributos de domínio são privados e finais. Client, Item e DiscountPolicy
 * são imutáveis por construção — uma vez criados, seus valores não mudam. Order expõe
 * sua lista de itens exclusivamente via Collections.unmodifiableList, impedindo
 * qualquer modificação externa. A validação no construtor (Fail-Fast) garante que
 * nenhum objeto inválido existe em memória: quantidade zero, preço negativo ou
 * referência nula lançam IllegalArgumentException antes de qualquer estado ser gravado.
 *
 * SRP e separação de responsabilidades:
 * - Client: representa identidade do comprador; muda apenas se os dados do cliente mudam.
 * - Item: encapsula produto, quantidade, preço e subtotal; muda se a regra de item muda.
 * - Order: agrega itens e expõe totais calculados; muda se a composição do pedido muda.
 * - DiscountPolicy: isola o cálculo de desconto; muda se a política comercial muda.
 * - InvoicePrinter: único responsável pela formatação da fatura; muda se o layout muda.
 * - EmailNotifier: único responsável pelo envio de notificação; muda se o canal muda.
 * Cada classe tem exatamente uma razão para mudar, conforme exige o SRP.
 *
 * Violação de LSP:
 * Não há hierarquia de herança na arquitetura produzida. Todas as classes são finais
 * e compostas via referências diretas. LSP não se aplica — e sua ausência é intencional:
 * composição sobre herança elimina o risco de violação de contrato em subtipos.
 */
package com.andrebecker.ecommerce;

import com.andrebecker.ecommerce.domain.Client;
import com.andrebecker.ecommerce.domain.DiscountPolicy;
import com.andrebecker.ecommerce.domain.Item;
import com.andrebecker.ecommerce.domain.Order;
import com.andrebecker.ecommerce.service.EmailNotifier;
import com.andrebecker.ecommerce.service.InvoicePrinter;

public class App {

    public static void main(String[] args) {
        Client client = new Client("João", "joao@email.com");
        DiscountPolicy discount = new DiscountPolicy(0.1);
        Order order = new Order(client, discount);

        order.addItem(new Item("Notebook", 1, 3500.0));
        order.addItem(new Item("Mouse", 2, 80.0));

        new InvoicePrinter().print(order);
        new EmailNotifier().notifyOrderReceived(order);
    }
}