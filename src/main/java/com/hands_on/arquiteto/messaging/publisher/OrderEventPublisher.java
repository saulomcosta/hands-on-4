package com.hands_on.arquiteto.messaging.publisher;

// Classe responsável por enviar mensagens para o RabbitMQ
// utilizando o RabbitTemplate do Spring
import org.springframework.amqp.rabbit.core.RabbitTemplate;
// Indica que essa classe é um componente gerenciado pelo Spring
// permitindo injeção de dependências e uso em outros serviços
import org.springframework.stereotype.Service;
import com.hands_on.arquiteto.config.RabbitConfig;
// Importa a entidade Order (objeto que será enviado como mensagem)
import com.hands_on.arquiteto.entity.Order;
import com.hands_on.arquiteto.event.OrderCreatedEvent;


/**
 * ==========📤 ORDER EVENT PUBLISHER — PUBLICADOR DE EVENTOS DE DOMÍNIO ==========
 *
 * 🧠 VISÃO GERAL: Este componente é responsável por publicar eventos de domínio no RabbitMQ,
 * representando mudanças de estado relevantes dentro do sistema.
 *
 * Ele faz parte da arquitetura orientada a eventos (Event-Driven Architecture), onde serviços não
 * se comunicam diretamente, mas reagem a eventos.
 *
 * ------------------------------------------------------------------------------------- 🎯
 * RESPONSABILIDADE
 * -------------------------------------------------------------------------------------
 *
 * ✅ Converter entidade de domínio (Order) em evento de negócio ✅ Publicar evento no broker
 * (RabbitMQ) ✅ Garantir desacoplamento entre serviços
 *
 * ------------------------------------------------------------------------------------------ 📡
 * CONTEXTO ARQUITETURAL
 * -------------------------------------------------------------------------------------
 *
 * OrderService ↓ OrderEventPublisher (este componente) ↓ RabbitMQ (Exchange → Queue) ↓ Consumers
 * (Payment, Email, etc.)
 *
 * ------------------------------------------------------------------------------------- ⚠️
 * PRINCÍPIO IMPORTANTE
 * -------------------------------------------------------------------------------------
 *
 * ❌ NÃO enviar entidades (Order) ✅ SEMPRE enviar eventos de domínio (OrderCreatedEvent)
 *
 * Isso garante: - Baixo acoplamento - Independência entre serviços - Evolução segura do sistema
 *
 * ------------------------------------------------------------------------------------------- ✅
 * BENEFÍCIOS
 * ----------------------------------------------------------------------------------------
 *
 * - Arquitetura desacoplada - Facilidade de escalar consumidores - Clareza semântica (eventos de
 * negócio) - Base para microservices e Kafka
 *
 */
@Service
public class OrderEventPublisher {


    /**
     * 📤 RabbitTemplate
     *
     * 🧠 Componente responsável por comunicação com o RabbitMQ.
     *
     * Permite: - Converter objetos Java em JSON automaticamente - Enviar mensagens para exchanges -
     * Trabalhar como produtor (Producer)
     */
    private final RabbitTemplate rabbitTemplate;


    /**
     * 🔧 Injeção de dependência via construtor
     *
     * O Spring injeta automaticamente: - RabbitTemplate (cliente do RabbitMQ)
     *
     * ✅ Boa prática: - Facilita testes - Torna a classe imutável
     */
    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }


    /**
     * ================================================================================= 📤
     * PUBLICAÇÃO DE EVENTO: ORDER CREATED
     * =================================================================================
     *
     * 🧠 Este método representa o fato de negócio:
     *
     * 👉 "Um pedido foi criado"
     *
     * -------------------------------------------------------------------------------------------
     * 🔁 FLUXO EXECUTADO
     * ------------------------------------------------------------------------------------
     *
     * 1. Recebe entidade Order 2. Converte para OrderCreatedEvent 3. Publica evento no RabbitMQ
     *
     * -------------------------------------------------------------------------------------------
     * 📦 POR QUE CONVERTER PARA EVENTO?
     * ---------------------------------------------------------------------------------
     *
     * ❌ Entidade (Order) possui: - muitos campos - regras internas - acoplamento com banco
     *
     * ✅ Evento (OrderCreatedEvent) possui: - apenas dados necessários - sem dependência de
     * infraestrutura - significado claro de negócio
     *
     * -------------------------------------------------------------------------------------------
     * 📡 DESTINO
     * ------------------------------------------------------------------------------------
     *
     * Exchange: order.exchange Routing Key: order.created
     *
     * ------------------------------------------------------------------------------------------ 🎯
     * RESULTADO
     * --------------------------------------------------------------------------------------
     *
     * Outros serviços passam a reagir de forma assíncrona:
     *
     * - PaymentConsumer → processa pagamento - EmailConsumer → envia notificação
     *
     * -------------------------------------------------------------------------------------------
     * ⚠️ BOAS PRÁTICAS
     * ---------------------------------------------------------------------------------
     *
     * ✅ Publicar apenas após persistência no banco ✅ Garantir consistência (ideal: Outbox Pattern)
     * ✅ Evitar lógica de negócio no publisher
     *
     */
    public void publish(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(order.getId(), order.getAmount());
        rabbitTemplate.convertAndSend(RabbitConfig.ORDER_EXCHANGE, RabbitConfig.ORDER_CREATED,
                event);
    }
}
