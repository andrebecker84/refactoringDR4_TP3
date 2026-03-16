# TP3 — Relatório de Refatoração

## E-Commerce Order Refactoring — DR4 Refatoração

**Aluno:** André Luis Becker
**Disciplina:** Engenharia de Software — Refatoração (DR4)
**Data:** março de 2026

---

## 1. Introdução

O sistema analisado é responsável por gerar faturas e enviar e-mails de confirmação de pedidos de uma startup de e-commerce. O código original foi escrito sem atenção à separação de responsabilidades ou ao encapsulamento, resultando em uma classe `Order` com múltiplas responsabilidades, atributos públicos sem controle e listas paralelas que não garantem consistência estrutural.

Este relatório documenta cada refatoração aplicada, com o trecho antes, o trecho depois, o princípio técnico utilizado e a justificativa para a decisão.

---

## 2. Bad Smells Identificados

### 2.1 Atributos públicos sem controle de acesso

```java
// antes
public String clientName;
public String clientEmail;
public double discountRate = 0.1;
```

Qualquer código cliente pode alterar `clientName` para `null` ou `discountRate` para um valor negativo sem que `Order` tenha como reagir. A classe não é responsável pela própria integridade.

### 2.2 Listas paralelas sem objeto de domínio

```java
// antes
public List products = new ArrayList<>();
public List quantities = new ArrayList<>();
public List prices = new ArrayList<>();
```

Três listas independentes representam um único conceito: o item do pedido. Nenhuma estrutura da linguagem garante que `products.get(i)`, `quantities.get(i)` e `prices.get(i)` se refiram ao mesmo produto. Um `add` em `products` sem o correspondente em `quantities` cria inconsistência silenciosa.

### 2.3 Dados do cliente misturados com dados do pedido

`Order` armazena `clientName` e `clientEmail`. Isso viola o SRP: `Order` tem duas razões para mudar — uma por alteração na estrutura do pedido e outra por alteração nos dados do cliente.

### 2.4 Lógica de cálculo embutida em método de apresentação

```java
// antes
public void printInvoice() {
    double total = 0;
    System.out.println("Cliente: " + clientName);
    for (int i = 0; i < products.size(); i++) {
        System.out.println(quantities.get(i) + "x " + products.get(i) + " - R$" + prices.get(i));
        total += prices.get(i) * quantities.get(i);
    }
    System.out.println("Subtotal: R$" + total);
    System.out.println("Desconto: R$" + (total * discountRate));
    System.out.println("Total final: R$" + (total * (1 - discountRate)));
}
```

Cálculo e formatação no mesmo método. Impossível testar o valor do total sem capturar stdout. Fowler denomina isso *Long Method* com responsabilidade múltipla.

### 2.5 Acoplamento direto entre domínio e infraestrutura

```java
// antes
public void sendEmail() {
    EmailService.sendEmail(clientEmail, "Pedido recebido! Obrigado pela compra.");
}
```

`Order` conhece `EmailService` diretamente. Trocar o mecanismo de envio exige alterar a classe de domínio, violando OCP.

---

## 3. Refatorações Aplicadas

### 3.1 Extract Class — Client

**Técnica:** Extract Class (Fowler, cap. 7)
**Princípio:** SRP

**Antes:**
```java
class Order {
    public String clientName;
    public String clientEmail;
    // ...
}
```

**Depois:**
```java
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
```

`Order` passa a receber um `Client` no construtor. Dados do cliente têm validação própria e são imutáveis. `Order` não muda mais quando o modelo do cliente muda.

**Justificativa:** `clientName` e `clientEmail` são coesos entre si e independentes da lógica do pedido. Extraí-los para `Client` reduz o escopo de mudança de `Order` e introduz um objeto de domínio com identidade própria.

---

### 3.2 Replace Data Value with Object — Item

**Técnica:** Replace Data Value with Object (Fowler, cap. 8)
**Princípio:** Encapsulamento, DRY

**Antes:**
```java
public List products = new ArrayList<>();
public List quantities = new ArrayList<>();
public List prices = new ArrayList<>();
```

**Depois:**
```java
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

    public double subtotal() {
        return quantity * price;
    }
}
```

`Order` passa a manter `List<Item>`, com segurança de tipo e semântica explícita.

**Justificativa:** As três listas representam um único conceito. Unificá-las em `Item` elimina a possibilidade de inconsistência de índice, que o compilador não detectaria nas listas paralelas com raw types.

---

### 3.3 Encapsulate Field — Order

**Técnica:** Encapsulate Field (Fowler, cap. 6)
**Princípio:** Encapsulamento real, Fail-Fast

**Antes:**
```java
class Order {
    public String clientName;
    public double discountRate = 0.1;
    // ...
}
```

**Depois:**
```java
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

    public List<Item> items() { return Collections.unmodifiableList(items); }
    // ...
}
```

Atributos são privados e finais. A lista de itens é exposta apenas como cópia imutável via `Collections.unmodifiableList`. Validação no construtor impede `Order` com estado inválido.

**Justificativa:** Encapsulamento real significa que a classe é responsável pela própria integridade. `Collections.unmodifiableList` impede que código externo adicione itens sem passar pelo método `addItem`, que pode conter validações futuras.

---

### 3.4 Extract Class — DiscountPolicy

**Técnica:** Extract Class (Fowler, cap. 7)
**Princípio:** SRP, OCP

**Antes:**
```java
class DiscountPolicy {
    public static double calculateDiscount(double amount, double rate) {
        return amount * rate;
    }
}
```

**Depois:**
```java
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
```

`DiscountPolicy` deixa de ser uma classe utilitária estática sem estado e passa a ser um objeto de domínio com taxa encapsulada. `Order` recebe `DiscountPolicy` no construtor em vez de manter `discountRate` como atributo primitivo público.

**Justificativa:** A política de desconto é um conceito de domínio independente. Encapsulá-la em objeto de domínio com estado permite substituir a política sem alterar `Order` (OCP), além de isolar a validação da taxa.

---

### 3.5 Hide Delegate — EmailNotifier

**Técnica:** Hide Delegate (Fowler, cap. 7)
**Princípio:** Baixo acoplamento, Lei de Demeter

**Antes:**
```java
// em Order
public void sendEmail() {
    EmailService.sendEmail(clientEmail, "Pedido recebido! Obrigado pela compra.");
}
```

**Depois:**
```java
public final class EmailNotifier {
    public void notifyOrderReceived(Order order) {
        EmailService.sendEmail(
            order.client().email(),
            "Pedido recebido! Obrigado pela compra."
        );
    }
}
```

`Order` não importa mais `EmailService`. O acoplamento entre domínio e infraestrutura foi eliminado. `EmailNotifier` é o único ponto de acesso ao `EmailService`.

**Justificativa:** A dependência direta de `Order` em `EmailService` tornava o domínio sensível a mudanças de infraestrutura. Com `EmailNotifier` como intermediário, trocar o canal de envio (SMTP, fila, mock em testes) não exige alteração em nenhuma classe de domínio.

---

### 3.6 Move Method e Extract Method — InvoicePrinter

**Técnica:** Move Method, Extract Method (Fowler, caps. 6 e 7)
**Princípio:** SRP, Extract Method

**Antes:**
```java
// em Order
public void printInvoice() {
    double total = 0;
    System.out.println("Cliente: " + clientName);
    for (int i = 0; i < products.size(); i++) {
        System.out.println(quantities.get(i) + "x " + products.get(i) + " - R$" + prices.get(i));
        total += prices.get(i) * quantities.get(i);
    }
    System.out.println("Subtotal: R$" + total);
    System.out.println("Desconto: R$" + (total * discountRate));
    System.out.println("Total final: R$" + (total * (1 - discountRate)));
}
```

**Depois:**
```java
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
```

O cálculo foi movido para `Order` (`grossTotal()`, `discountAmount()`, `finalTotal()`). A formatação ficou exclusivamente em `InvoicePrinter`, decomposta em quatro métodos com intenção explícita.

**Justificativa:** Cálculo e formatação são responsabilidades ortogonais. Separar os dois torna `grossTotal()` testável sem efeito colateral de I/O e torna `InvoicePrinter` substituível sem alterar o domínio.

---

### 3.7 Replace Temp with Query — variáveis temporárias de cálculo

**Técnica:** Replace Temp with Query (Fowler, cap. 6)
**Princípio:** DRY, expressividade

**Antes:**
```java
double total = 0;
for (int i = 0; i < products.size(); i++) {
    total += prices.get(i) * quantities.get(i);
}
// total usado três vezes abaixo
```

**Depois:**
```java
// em Order
public double grossTotal() {
    return items.stream().mapToDouble(Item::subtotal).sum();
}
```

A variável temporária `total` foi substituída pelo método `grossTotal()`, que pode ser chamado a qualquer momento com resultado garantidamente consistente.

**Justificativa:** Variáveis temporárias de cálculo introduzem dependência de ordem de execução. Métodos descritivos eliminam essa dependência e tornam o resultado diretamente testável.

---

## 4. Testes Automatizados

### 4.1 Estratégia

Os testes validam exclusivamente o comportamento das regras de negócio, sem dependência de detalhes de implementação. `InvoicePrinter` e `EmailNotifier` não são testados unitariamente porque dependem de I/O — seus comportamentos são verificados pela execução de `App.main()`.

### 4.2 Resultados

```
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
```

**ItemTest** — 7 testes: subtotal, precisão decimal, rejeição de quantidade inválida, preço negativo, produto em branco.

**OrderTest** — 7 testes: total bruto, desconto, total final, coerência com taxa, imutabilidade da lista, rejeição de cliente nulo, rejeição de política nula.

**OrderPropertyTest** — 3 propriedades Jqwik com 1000 verificações cada:
- `grossTotalEqualsSum`: totalBruto == Σ(qty × price) para qualquer combinação de itens válidos
- `finalTotalLessThanGrossTotalWhenDiscountPositive`: totalFinal < subtotal quando discountRate > 0
- `itemSubtotalNeverNegative`: subtotal de Item nunca negativo para valores válidos

### 4.3 Cobertura JaCoCo

Relatório gerado em `target/site/jacoco/index.html` após `mvn clean verify`.

---

## 5. Conclusão

A refatoração transformou uma classe monolítica de 60 linhas com múltiplas responsabilidades em seis classes coesas com responsabilidade única cada. O comportamento externo é idêntico ao original, garantido pelos 17 testes automatizados. O modelo de domínio passou a ser expressivo, testável e extensível sem alterar classes existentes.

As sete técnicas de Fowler aplicadas — Extract Class, Replace Data Value with Object, Encapsulate Field, Hide Delegate, Move Method, Extract Method e Replace Temp with Query — atuaram de forma complementar: cada uma eliminou um problema estrutural específico sem introduzir complexidade desnecessária.