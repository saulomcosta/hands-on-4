package com.hands_on.arquiteto.config;

// Importações do Spring AMQP (integração com RabbitMQ)
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
// Importa anotação que indica que esta classe contém configurações do Spring
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 📡 Configuração central do RabbitMQ
 *
 * 🧠 Responsável por definir a infraestrutura de mensageria da aplicação, incluindo Exchange,
 * Filas, Bindings e estratégia de tratamento de erros.
 *
 * ----------------- 📦 COMPONENTES CONFIGURADOS:
 *
 * ✅ Exchange (order.exchange) → Recebe mensagens dos produtores
 *
 * ✅ Queue principal (payment.queue) → Onde as mensagens ficam armazenadas para consumo
 *
 * ✅ Dead Letter Queue (payment.dlq) → Recebe mensagens que falharam após várias tentativas
 *
 * ✅ Binding → Define como mensagens são roteadas entre exchange e filas
 *
 * ----------------- 🔁 FLUXO PRINCIPAL:
 *
 * Producer (OrderService) ↓ Exchange (order.exchange) ↓ (routing key: order.created) Queue
 * (payment.queue) ↓ Consumer (PaymentConsumer)
 *
 * ----------------- ❌ FLUXO DE ERRO (RESILIÊNCIA):
 *
 * Se o consumer falhar após retries:
 *
 * payment.queue ↓ DLX (dlx.exchange) ↓ (payment.dlq) Dead Letter Queue
 *
 * ----------------- 🚀 BENEFÍCIOS:
 *
 * - Desacoplamento entre serviços - Processamento assíncrono - Tolerância a falhas - Não perde
 * mensagens (DLQ) - Escalabilidade
 */

@Configuration
public class RabbitConfig {


    // ================================
    // CONSTANTES (PADRONIZAÇÃO)
    // ================================

    // 📡 Exchange principal (roteamento de eventos de negócio)
    public static final String EXCHANGE = "order.exchange";
    // 📥 Fila principal de processamento de pagamento
    public static final String QUEUE = "payment.queue";
    // 🔀 Routing key usada para direcionar mensagens de pedidos criado
    public static final String ROUTING_KEY = "order.created";
    // ☠️ Nome da Dead Letter Queue (fila de erro)
    public static final String PAYMENT_DLQ = "payment.dlq";
    // 🔁 Exchange de Dead Letter (DLX)
    public static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";
    // ⚙️ Chave padrão usada pelo RabbitMQ para definir exchange de Dead Letter (DLX)
    public static final String DLX_EXCHANGE = "dlx.exchange";
    // ⚙️ Chave padrão usada pelo RabbitMQ para definir routing key da DLQ
    public static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";


    @Bean
    public MessageConverter jsonMessageConverter() {

        /*
         * 🔄 Conversor de mensagem (JSON)
         *
         * 🧠 Responsável por transformar objetos Java em JSON ao enviar mensagens e JSON em objetos
         * Java ao receber.
         *
         * ✅ Permite trabalhar com objetos diretamente, sem precisar construir JSON manualmente ✅
         * Facilita integração entre serviços desacoplados
         */
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {

        /*
         * 📤 RabbitTemplate (Produtor de mensagens)
         *
         * 🧠 Classe principal usada para enviar mensagens ao RabbitMQ.
         *
         * 🔧 Configurações: - ConnectionFactory: define a conexão com o broker - MessageConverter:
         * define como os dados são serializados (JSON neste caso)
         *
         * ✅ Utilizado em services (ex: OrderService) para publicar eventos ✅ Abstrai complexidade
         * de envio de mensagens
         *
         * Exemplo de uso: rabbitTemplate.convertAndSend(exchange, routingKey, objeto);
         */
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);

        return template;
    }

    // ================================
    // EXCHANGES
    // ================================

    /**
     * 📡 Exchange principal
     *
     * Tipo: DIRECT
     *
     * 🧠 Direciona mensagens com base na routing key exata.
     */
    @Bean
    public DirectExchange exchange() {
        /*
         * Cria uma Exchange do tipo DIRECT.
         *
         * 🧠 O que é uma Exchange? É o componente responsável por receber mensagens do produtor e
         * decidir para qual fila elas devem ir.
         *
         * 🧠 Tipo DIRECT: Envia a mensagem para a fila que tiver a routing key EXATA.
         *
         * Exemplo: Se enviar mensagem com routing key "order.created", ela será enviada para a fila
         * associada com essa mesma key.
         */
        return new DirectExchange(EXCHANGE);
    }

    /**
     * ☠️ Dead Letter Exchange (DLX)
     *
     * 🧠 Recebe mensagens que falharam no processamento.
     */
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    // ================================
    // QUEUES
    // ================================

    /**
     * 📥 Fila principal de pagamento
     *
     * 🧠 Características: - Durável (não perde mesmo com restart) - Configurada com DLQ
     *
     * 🔁 Se falhar: → mensagem vai para DLX → depois para payment.dlq
     */

    @Bean
    public Queue paymentQueue() {

        return QueueBuilder
                // Mensagem não é apagada mesmo se o RabbitMQ for reiniciado
                .durable(QUEUE)
                // Define exchange de erro (DLX)
                .withArgument(X_DEAD_LETTER_EXCHANGE, DLX_EXCHANGE)
                // Define routing key da fila de erro
                .withArgument(X_DEAD_LETTER_ROUTING_KEY, PAYMENT_DLQ).build();

    }

    /**
     * ☠️ Dead Letter Queue (DLQ)
     *
     * 🧠 Armazena mensagens que falharam após todas as tentativas.
     *
     * 📌 Usos: - Auditoria - Reprocessamento manual - Debug
     */
    @Bean
    public Queue deadLetterQueue() {
        return new Queue("payment.dlq");
    }

    // ================================
    // BINDINGS
    // ================================

    /**
     * 🔗 Binding principal
     *
     * Liga: Exchange → Queue
     *
     * 🧠 Regra: Se routing key = "order.created" → vai para payment.queue
     */
    @Bean
    public Binding binding() {
        /*
         * Cria a ligação entre: - Exchange - Queue - Routing Key
         *
         * 🧠 O que é Binding? É a regra que conecta uma fila a uma exchange.
         *
         * Aqui estamos dizendo:
         *
         * "Toda mensagem enviada para a exchange 'order.exchange' com routing key 'order.created'
         * deve ir para a fila 'payment.queue'"
         *
         * Fluxo completo:
         *
         * Producer (OrderService) ↓ Exchange (order.exchange) ↓ (routing key: order.created) Queue
         * (payment.queue) ↓ Consumer (PaymentService)
         */

        return BindingBuilder.bind(paymentQueue()).to(exchange()).with(ROUTING_KEY);

    }

    /**
     * ☠️ Binding da DLQ
     *
     * Liga: DLX → Dead Letter Queue
     *
     * 🧠 Regra: Mensagens com routing key "payment.dlq" → vão para fila de erro
     */
    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(dlxExchange()).with(PAYMENT_DLQ);
    }

}
