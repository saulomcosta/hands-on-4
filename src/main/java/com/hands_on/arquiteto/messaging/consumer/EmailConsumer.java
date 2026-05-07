package com.hands_on.arquiteto.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.hands_on.arquiteto.config.RabbitConfig;
import com.hands_on.arquiteto.event.PaymentProcessedEvent;

/**
 * ===================================================================================== 📧 EMAIL
 * CONSUMER — PROCESSADOR DE NOTIFICAÇÕES
 * =====================================================================================
 *
 * 🧠 VISÃO GERAL: Este componente é responsável por consumir eventos de pagamento processado e
 * executar o envio de e-mails de forma assíncrona.
 *
 * Faz parte de uma arquitetura orientada a eventos (EDA), onde cada etapa do fluxo reage a eventos
 * de negócio.
 *
 * ------------------------------------------------------------------------------------- 📡 FLUXO DO
 * SISTEMA -------------------------------------------------------------------------------------
 *
 * OrderCreatedEvent ↓ PaymentConsumer ↓ PaymentProcessedEvent ↓ EmailConsumer (este componente)
 *
 * ------------------------------------------------------------------------------------- 🎯
 * RESPONSABILIDADES
 * -------------------------------------------------------------------------------------
 *
 * ✅ Consumir evento PaymentProcessedEvent ✅ Executar envio de e-mail (notificação) ✅ Garantir
 * idempotência (não enviar e-mail duplicado) ✅ Tratar falhas via DLQ (Dead Letter Queue)
 *
 * ------------------------------------------------------------------------------------- 🔒
 * IDEMPOTÊNCIA (REGRA IMPORTANTE)
 * -------------------------------------------------------------------------------------
 *
 * Este consumer pode receber o mesmo evento mais de uma vez.
 *
 * Portanto:
 *
 * ✅ Um e-mail NÃO deve ser enviado duas vezes
 *
 * Estratégia recomendada: - Registrar envio no banco - Verificar antes de enviar
 *
 * Problemas evitados: ❌ E-mails duplicados ❌ Experiência ruim do usuário
 *
 * ------------------------------------------------------------------------------------- 🛡️
 * RESILIÊNCIA -------------------------------------------------------------------------------------
 *
 * - Retry automático configurado no Spring - Backoff entre tentativas - Encaminhamento para DLQ em
 * caso de falha
 *
 * Fluxo de erro:
 *
 * email.queue → retry → DLX → payment.dlq (ou email.dlq futuramente)
 *
 * ------------------------------------------------------------------------------------- ⚙️ BOAS
 * PRÁTICAS (PRODUÇÃO)
 * -------------------------------------------------------------------------------------
 *
 * - Usar logs estruturados (evitar System.out) - Incluir correlationId para rastreamento - Isolar
 * integração de e-mail em outra camada (ex: EmailService) - Tratar exceções externamente (SMTP,
 * API, etc.)
 *
 */
@Service
public class EmailConsumer {

    /**
     * 📤 RabbitTemplate
     *
     * ✅ Neste cenário não é obrigatório, mas pode ser usado para: - Publicar novos eventos (ex:
     * EmailSentEvent) - Integrar com outros fluxos
     */
    private final RabbitTemplate rabbitTemplate;

    public EmailConsumer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * ================================================================================= 📥
     * CONSUMIDOR PRINCIPAL — PAYMENT PROCESSED EVENT
     * =================================================================================
     *
     * 🧠 Reage ao evento:
     *
     * 👉 "O pagamento foi processado"
     *
     * --------------------------------------------------------------------------------- 🔁 FLUXO
     * EXECUTADO ---------------------------------------------------------------------------------
     *
     * 1. Recebe PaymentProcessedEvent 2. Executa envio de e-mail
     *
     * --------------------------------------------------------------------------------- 🎯 OBJETIVO
     * ---------------------------------------------------------------------------------
     *
     * Notificar o cliente que o pagamento do pedido foi concluído.
     *
     */
    @RabbitListener(queues = RabbitConfig.EMAIL_QUEUE)
    public void sendEmail(PaymentProcessedEvent event) {

        // ✅ Executa envio de e-mail
        send(event);

        // 👉 Ideal: log estruturado (ex: log.info)
        System.out.println("📧 E-mail enviado | orderId=" + event.orderId());
    }

    /**
     * --------------------------------------------------------------------------------- 📧 ENVIO DE
     * E-MAIL ---------------------------------------------------------------------------------
     *
     * 🧠 Contém a lógica de envio de e-mail.
     *
     * Em ambiente real:
     *
     * - Integração com SMTP / SendGrid / SES - Templates de e-mail - Personalização por usuário
     *
     * --------------------------------------------------------------------------------- ⚠️ REGRA
     * CRÍTICA ---------------------------------------------------------------------------------
     *
     * Este método deve ser IDEMPOTENTE:
     *
     * O mesmo evento NÃO pode gerar envio duplicado de e-mail.
     *
     */
    private void send(PaymentProcessedEvent event) {
        System.out.println("📨 Enviando e-mail para pedido: " + event.orderId());
    }

    /**
     * ================================================================================= ☠️
     * CONSUMIDOR DE ERROS (DLQ)
     * =================================================================================
     *
     * 🧠 Responsável por tratar mensagens que falharam definitivamente.
     *
     * --------------------------------------------------------------------------------- 📡 ORIGEM
     * ---------------------------------------------------------------------------------
     *
     * email.queue → falha → retry → DLX → payment.dlq (ou email.dlq)
     *
     * --------------------------------------------------------------------------------- 🎯 OBJETIVO
     * ---------------------------------------------------------------------------------
     *
     * - Registrar erro - Permitir auditoria - Evitar perda de mensagem
     *
     * --------------------------------------------------------------------------------- ⚠️ REGRAS
     * IMPORTANTES ---------------------------------------------------------------------------------
     *
     * ❌ NÃO reprocessar automaticamente ❌ NÃO gerar loop infinito
     *
     * ✅ Tratar como falha definitiva
     *
     * --------------------------------------------------------------------------------- 🚀
     * MELHORIAS FUTURAS
     * ---------------------------------------------------------------------------------
     *
     * - Persistir erro em banco - Notificar sistemas externos (Slack, alertas) - Criar
     * reprocessamento manual - Incluir correlationId
     *
     */
    @RabbitListener(queues = RabbitConfig.PAYMENT_DLQ)
    public void handleError(PaymentProcessedEvent event) {

        System.out.println("☠️ Falha ao enviar e-mail | orderId=" + event.orderId());

        // 👉 futuras melhorias:
        // errorRepository.save(...)
        // alertService.notify(...)
        // retryManualService.reprocess(...)
    }
}
