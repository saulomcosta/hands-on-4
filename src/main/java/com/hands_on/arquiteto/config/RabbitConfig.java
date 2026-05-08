package com.hands_on.arquiteto.config;

// =============================================================
// IMPORTAÇÕES SPRING AMQP / RABBITMQ
// =============================================================

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ===========📡 RABBIT CONFIG ===========
 *
 * Configuração central da infraestrutura de mensageria.
 *
 * Esta classe é responsável por criar:
 *
 * ✅ Exchanges ✅ Queues ✅ Dead Letter Queues ✅ Bindings ✅ Conversão JSON ✅ RabbitTemplate
 *
 * ============ 🧠 OBJETIVO ARQUITETURAL ============
 *
 * Transformar o sistema em:
 *
 * ✅ Event Driven Architecture ✅ Processamento assíncrono ✅ Sistema desacoplado ✅ Fluxo baseado em
 * domínio ✅ Arquitetura resiliente
 *
 * =========== 🔥 FLUXO COMPLETO ===========
 *
 * OrderService ↓ OrderCreatedEvent ↓ order.exchange ↓ payment.queue ↓ PaymentConsumer ↓
 * PaymentProcessedEvent ↓ payment.exchange ↓ email.queue ↓ EmailConsumer
 *
 * ============ ❌ FLUXO DE ERRO ============
 *
 * Se um consumer falhar:
 *
 * Queue Principal ↓ DLX (Dead Letter Exchange) ↓ Dead Letter Queue
 *
 * Exemplo:
 *
 * payment.queue ↓ payment.dlq
 *
 * =============== 🚀 BENEFÍCIOS ===============
 *
 * ✅ Desacoplamento ✅ Escalabilidade ✅ Resiliência ✅ Retry ✅ Tolerância a falhas ✅ Reprocessamento ✅
 * Microsserviços preparados
 *
 * ================
 */

@Configuration
public class RabbitConfig {

    // =============
    // EXCHANGES
    // =============

    /**
     * 📡 Exchange responsável pelos eventos de pedidos.
     *
     * Exemplo: - OrderCreatedEvent
     */
    public static final String ORDER_EXCHANGE = "order.exchange";

    /**
     * 📡 Exchange responsável pelos eventos de pagamento.
     *
     * Exemplo: - PaymentProcessedEvent
     */
    public static final String PAYMENT_EXCHANGE = "payment.exchange";

    /**
     * 📡 Exchange de Dead Letter.
     *
     * Recebe mensagens que falharam.
     */
    public static final String DLX_EXCHANGE = "dlx.exchange";

    // =============
    // QUEUES
    // =============

    /**
     * 📥 Fila responsável por processar pagamentos.
     */
    public static final String PAYMENT_QUEUE = "payment.queue";

    /**
     * 📥 Fila responsável por envio de e-mails.
     */
    public static final String EMAIL_QUEUE = "email.queue";

    /**
     * ☠️ Dead Letter Queue do pagamento.
     */
    public static final String PAYMENT_DLQ = "payment.dlq";

    /**
     * ☠️ Dead Letter Queue do e-mail.
     */
    public static final String EMAIL_DLQ = "email.dlq";

    // ================
    // ROUTING KEYS
    // ================

    /**
     * 🔀 Evento disparado quando um pedido é criado.
     */
    public static final String ORDER_CREATED = "order.created";

    /**
     * 🔀 Evento disparado quando pagamento é processado.
     */
    public static final String PAYMENT_PROCESSED = "payment.processed";

    /**
     * 🔀 Evento disparado quando e-mail é enviado.
     */
    public static final String EMAIL_SENT = "email.sent";

    // ============
    // ARGUMENTOS DLQ
    // ============

    /**
     * 🔁 Exchange para mensagens com falha.
     */
    public static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";

    /**
     * 🔁 Routing key usada pela DLQ.
     */
    public static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

    // ==============
    // JSON CONVERTER
    // ==============

    /**
     * 🔄 Conversor JSON.
     *
     * Responsável por:
     *
     * ✅ Converter objeto Java → JSON ✅ Converter JSON → objeto Java
     *
     * Permite enviar records/eventos diretamente.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    // ================
    // RABBIT TEMPLATE
    // ===============

    /**
     * 📤 RabbitTemplate
     *
     * Classe responsável por publicar mensagens.
     *
     * Utilizada pelos publishers:
     *
     * - OrderEventPublisher - PaymentEventPublisher
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {

        RabbitTemplate template = new RabbitTemplate(connectionFactory);

        template.setMessageConverter(messageConverter);

        return template;
    }

    // ==================
    // EXCHANGES
    // ==================

    /**
     * 📡 Exchange de pedidos.
     */
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    /**
     * 📡 Exchange de pagamentos.
     */
    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(PAYMENT_EXCHANGE);
    }

    /**
     * ☠️ Dead Letter Exchange.
     */
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    // =================
    // PAYMENT QUEUE
    // =================

    /**
     * 📥 Fila de processamento de pagamento.
     *
     * Recebe:
     *
     * OrderCreatedEvent
     *
     * Consumer:
     *
     * PaymentConsumer
     *
     * Possui:
     *
     * ✅ Persistência ✅ DLQ ✅ Resiliência
     */
    @Bean
    public Queue paymentQueue() {

        return QueueBuilder.durable(PAYMENT_QUEUE)

                // Exchange para mensagens com falha
                .withArgument(X_DEAD_LETTER_EXCHANGE, DLX_EXCHANGE)

                // Routing key da DLQ
                .withArgument(X_DEAD_LETTER_ROUTING_KEY, PAYMENT_DLQ)

                .build();
    }

    // ==================
    // EMAIL QUEUE
    // ==================

    /**
     * 📥 Fila de envio de e-mail.
     *
     * Recebe:
     *
     * PaymentProcessedEvent
     *
     * Consumer:
     *
     * EmailConsumer
     *
     * ================== O QUE ESTA FILA ENTREGA ==================
     *
     * ✅ Desacoplamento do envio de e-mail ✅ Processamento assíncrono ✅ Retry automático ✅
     * Resiliência ✅ Não bloquear fluxo principal
     *
     * ================== EXEMPLO REAL ===================
     *
     * Usuário cria pedido ↓ Pagamento aprovado ↓ Evento enviado ↓ Email processado separadamente
     *
     * Mesmo se email falhar:
     *
     * ✅ Pedido continua funcionando
     */
    @Bean
    public Queue emailQueue() {

        return QueueBuilder.durable(EMAIL_QUEUE)

                // Exchange de erro
                .withArgument(X_DEAD_LETTER_EXCHANGE, DLX_EXCHANGE)

                // Routing key DLQ email
                .withArgument(X_DEAD_LETTER_ROUTING_KEY, EMAIL_DLQ)

                .build();
    }

    // ================
    // DEAD LETTER QUEUES
    // ================

    /**
     * ☠️ DLQ do pagamento.
     */
    @Bean
    public Queue paymentDeadLetterQueue() {
        return new Queue(PAYMENT_DLQ);
    }

    /**
     * ☠️ DLQ do email.
     */
    @Bean
    public Queue emailDeadLetterQueue() {
        return new Queue(EMAIL_DLQ);
    }

    // ================
    // BINDINGS
    // ================

    /**
     * 🔗 Binding:
     *
     * order.exchange ↓ payment.queue
     *
     * Routing key:
     *
     * order.created
     */
    @Bean
    public Binding paymentBinding() {

        return BindingBuilder.bind(paymentQueue()).to(orderExchange()).with(ORDER_CREATED);
    }

    /**
     * 🔗 Binding:
     *
     * payment.exchange ↓ email.queue
     *
     * Routing key:
     *
     * payment.processed
     */
    @Bean
    public Binding emailBinding() {

        return BindingBuilder.bind(emailQueue()).to(paymentExchange()).with(PAYMENT_PROCESSED);
    }

    /**
     * ☠️ Binding da DLQ do pagamento.
     */
    @Bean
    public Binding paymentDlqBinding() {

        return BindingBuilder.bind(paymentDeadLetterQueue()).to(dlxExchange()).with(PAYMENT_DLQ);
    }

    /**
     * ☠️ Binding da DLQ do email.
     */
    @Bean
    public Binding emailDlqBinding() {

        return BindingBuilder.bind(emailDeadLetterQueue()).to(dlxExchange()).with(EMAIL_DLQ);
    }
}
