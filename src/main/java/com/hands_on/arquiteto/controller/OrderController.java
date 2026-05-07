package com.hands_on.arquiteto.controller;

import java.math.BigDecimal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.hands_on.arquiteto.entity.Order;
import com.hands_on.arquiteto.service.OrderService;


/**
 * CAMADA: CONTROLLER (Camada de Apresentação / API REST)
 *
 * Responsabilidade principal: - Expor endpoints HTTP para o cliente (Postman, Bruno, HTTPie,
 * frontend, etc) - Receber requisições HTTP - Validar/parsing básico dos dados de entrada (query
 * params, body, path variables) - Delegar TODA regra de negócio para a camada Service
 *
 * IMPORTANTE: Esta classe NÃO deve conter regras de negócio. Ela apenas atua como "porta de
 * entrada" da aplicação.
 *
 * O Spring automaticamente registra esta classe como um bean REST devido a: - @RestController →
 * indica que retorna JSON diretamente (não views HTML) - @RequestMapping("/orders") → define o
 * prefixo base de todas as rotas
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    /**
     * Dependência da camada de serviço (regra de negócio).
     *
     * O Spring injeta automaticamente essa dependência via construtor (Dependency Injection).
     *
     * Vantagens: - Facilita testes (mock do service) - Reduz acoplamento - Segue princípio SOLID
     * (DIP - Dependency Inversion Principle)
     */
    private final OrderService orderService;

    /**
     * Construtor utilizado pelo Spring para injeção de dependência.
     *
     * Quando a aplicação sobe, o Spring: 1. Cria o OrderService 2. Cria este Controller 3. Injeta
     * automaticamente o OrderService aqui
     */
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * ENDPOINT: POST /orders
     *
     * Responsabilidade: - Criar um novo pedido (Order)
     *
     * Como funciona: - Recebe o valor do pedido via query param (?amount=100) - Converte
     * automaticamente para BigDecimal (Spring faz binding) - Chama a camada de serviço para
     * executar toda a lógica de negócio - Retorna o objeto Order convertido automaticamente em JSON
     *
     * Exemplo de chamada: POST http://localhost:8080/orders?amount=100
     *
     * Exemplo de resposta: { "id": "...", "amount": 100, "status": "COMPLETED" }
     *
     * Observação: - @RequestParam indica que o valor vem da URL (query string) - Não é ideal para
     * payloads complexos (nesse caso seria @RequestBody)
     */
    @PostMapping
    public Order createOrder(@RequestParam BigDecimal amount) {
        return orderService.createOrder(amount);
    }
}
