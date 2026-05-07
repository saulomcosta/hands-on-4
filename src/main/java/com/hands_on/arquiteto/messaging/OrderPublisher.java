package com.hands_on.arquiteto.messaging;

// Classe responsável por enviar mensagens para o RabbitMQ
// utilizando o RabbitTemplate do Spring
import org.springframework.amqp.rabbit.core.RabbitTemplate;
// Indica que essa classe é um componente gerenciado pelo Spring
// permitindo injeção de dependências e uso em outros serviços
import org.springframework.stereotype.Service;
import com.hands_on.arquiteto.config.RabbitConfig;
// Importa a entidade Order (objeto que será enviado como mensagem)
import com.hands_on.arquiteto.entity.Order;

@Service
public class OrderPublisher {

    // ================================
    // DEPENDÊNCIA PRINCIPAL
    // ================================
    private final RabbitTemplate rabbitTemplate;

    /*
     * 🔧 Injeção de dependência via construtor
     *
     * O Spring injeta automaticamente o RabbitTemplate, que é o componente responsável por: -
     * Conectar com o RabbitMQ - Enviar mensagens (Producer)
     *
     * 🧠 RabbitTemplate funciona como um "client" do RabbitMQ
     */
    public OrderPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // ================================
    // PUBLICAÇÃO DE EVENTO
    // ================================

    /*
     * 🧠 Este método representa um EVENTO de negócio: "Um pedido foi criado"
     *
     * Ele será chamado geralmente dentro do OrderService, após salvar um pedido no banco.
     *
     * Em vez de chamar diretamente PaymentService ou EmailService, enviamos uma mensagem para o
     * RabbitMQ (arquitetura assíncrona).
     */
    public void publishOrderCreated(Order order) {
        // Log simples para acompanhamento (debug)
        System.out.println("Publicando mensagem de pedido criado: " + order.getId());
        /*
         * 📤 Envio da mensagem para o RabbitMQ
         *
         * convertAndSend: - Converte automaticamente o objeto Order para JSON - Envia para a
         * exchange configurada
         *
         * Parâmetros:
         *
         * 1️⃣ Exchange: RabbitConfig.EXCHANGE → "order.exchange"
         *
         * 2️⃣ Routing Key: RabbitConfig.ROUTING_KEY → "order.created"
         *
         * 3️⃣ Payload: order → objeto que será enviado na mensagem
         *
         * 🧠 O que acontece internamente:
         *
         * OrderPublisher ↓ Exchange (order.exchange) ↓ (routing key: order.created) Queue
         * (payment.queue) ↓ Consumer (ex: PaymentService)
         */
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, order);
    }
}
