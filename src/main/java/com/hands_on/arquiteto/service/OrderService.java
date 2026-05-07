package com.hands_on.arquiteto.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import com.hands_on.arquiteto.entity.Order;
import com.hands_on.arquiteto.messaging.publisher.OrderEventPublisher;
import com.hands_on.arquiteto.repository.OrderRepository;

/**
 * ======== CAMADA: SERVICE (Regra de Negócio / Domínio) =========
 *
 * Esta classe representa a camada de serviço da aplicação.
 *
 * RESPONSABILIDADES PRINCIPAIS: - Implementar regras de negócio - Orquestrar o fluxo da aplicação -
 * Coordenar comunicação entre camadas: → Controller (entrada HTTP) → Repository (persistência no
 * banco) → Messaging (RabbitMQ)
 *
 * IMPORTANTE: - NÃO deve conter lógica de transporte (HTTP, JSON, etc.) - NÃO deve acessar banco
 * diretamente via SQL - NÃO deve conhecer detalhes de infraestrutura externa
 *
 * Em termos arquiteturais: → Essa é a camada central da aplicação (coração do sistema)
 *
 * Fluxo geral: Controller → Service → Repository + Messaging
 */
@Service
public class OrderService {

    /**
     * Repositório de persistência (Spring Data JPA)
     *
     * Função: - Salvar, buscar e manipular entidades no banco - Abstrai completamente SQL
     *
     * Aqui usamos: → PostgreSQL via Hibernate (JPA)
     */
    private final OrderRepository orderRepository;
    /**
     * Componente responsável por publicar eventos no RabbitMQ
     *
     * Função: - Enviar mensagens para outras partes do sistema - Permitir arquitetura orientada a
     * eventos (event-driven)
     *
     * Neste caso: → Publica evento "pedido criado"
     */
    private final OrderEventPublisher orderPublisher;

    /**
     * Injeção de dependências via construtor (boa prática)
     *
     * O Spring automaticamente injeta: - OrderRepository - OrderPublisher
     */
    public OrderService(OrderRepository orderRepository, OrderEventPublisher orderPublisher) {
        this.orderRepository = orderRepository;
        this.orderPublisher = orderPublisher;
    }

    /**
     * ========= CASO DE USO: Criar um novo pedido (Order) =========
     *
     * Este método representa um fluxo de negócio completo.
     *
     * ETAPAS DO PROCESSO:
     *
     * 1) Criação da entidade Order em memória - status inicial: "CREATED" - ainda NÃO foi
     * persistida no banco
     *
     * 2) Persistência no banco de dados - o pedido passa a existir no PostgreSQL
     *
     * 3) Publicação de evento no RabbitMQ - envia mensagem para exchange - outros sistemas podem
     * reagir (ex: pagamento, envio, etc.)
     *
     * RESULTADO: - Pedido criado e armazenado - Evento disparado para integração assíncrona
     *
     * ========== CONCEITOS IMPORTANTES ENVOLVIDOS ==========
     *
     * ✔ Arquitetura orientada a eventos: - desacoplamento entre serviços - comunicação via
     * mensageria
     *
     * ✔ Persistência com JPA: - entidade salva automaticamente
     *
     * ✔ Separação de responsabilidades: - Service → regra de negócio - Repository → banco -
     * Messaging → integração
     *
     * ========== PONTOS DE ATENÇÃO (MUNDO REAL) ==========
     *
     * ⚠ Falta de transação (@Transactional): - se falhar após salvar, pode gerar inconsistência
     *
     * ⚠ Ordem das operações: - salva no banco antes de publicar evento - pode gerar problemas se a
     * publicação falhar
     *
     * ✔ Solução comum: - usar padrão "Outbox Pattern" - garantir consistência entre banco e
     * mensageria
     *
     * ⚠ Validação de entrada: - aqui não temos validação (ex: valor negativo) - importante para
     * robustez do sistema
     *
     * @param amount valor monetário do pedido
     * @return Order criada e persistida
     */
    public Order createOrder(BigDecimal amount) {
        /**
         * 1. Criação da entidade em memória
         *
         * - Ainda NÃO está no banco - Apenas objeto Java
         */
        Order savedOrder = Order.builder().amount(amount).status("CREATED").build();
        /**
         * 2. Persistência no banco de dados
         *
         * - Aqui o Hibernate executa um INSERT - O ID (UUID) é gerado automaticamente
         */
        orderRepository.save(savedOrder);
        /**
         * 3. Publicação de evento no RabbitMQ
         *
         * - Dispara um evento de domínio: "Order Created"
         *
         * - Outros serviços podem consumir: → pagamento → envio → faturamento
         */
        orderPublisher.publish(savedOrder);
        /**
         * Retorno do objeto persistido
         *
         * - Já contém ID gerado - Status ainda é "CREATED"
         */
        return savedOrder;
    }
}
