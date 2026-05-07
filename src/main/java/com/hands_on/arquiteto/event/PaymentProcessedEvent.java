package com.hands_on.arquiteto.event;

import java.util.UUID;

/**
 * ===================================================================================== 💳 EVENTO
 * DE DOMÍNIO: PAYMENT PROCESSED
 * =====================================================================================
 *
 * 🧠 VISÃO GERAL: Este evento representa um fato de negócio que já aconteceu no sistema:
 *
 * 👉 "O pagamento de um pedido foi processado com sucesso"
 *
 * Este NÃO é um DTO e nem uma entidade de banco. É um EVENTO DE DOMÍNIO, utilizado para comunicação
 * assíncrona entre componentes.
 *
 * ------------------------------------------------------------------------------------- 🎯 OBJETIVO
 * -------------------------------------------------------------------------------------
 *
 * Permitir que outros serviços reajam ao processamento de pagamento, sem acoplamento direto ao
 * PaymentConsumer.
 *
 * Exemplo:
 *
 * PaymentProcessedEvent ↓ EmailConsumer → envia e-mail ↓ NotificationService → envia push
 *
 *
 * ------------------------------------------------------------------------------------- 📡 CONTEXTO
 * NO FLUXO (EVENT-DRIVEN)
 * -------------------------------------------------------------------------------------
 *
 * OrderCreatedEvent ↓ PaymentConsumer ↓ ✅ PaymentProcessedEvent (este evento) ↓ EmailConsumer
 *
 *
 * ------------------------------------------------------------------------------------- ✅
 * BENEFÍCIOS -------------------------------------------------------------------------------------
 *
 * - Desacoplamento entre serviços - Alta escalabilidade - Facilidade de extensão (novos
 * consumidores podem surgir) - Clareza semântica (eventos representam fatos reais do domínio)
 *
 *
 * ------------------------------------------------------------------------------------- 📦 POR QUE
 * USAR RECORD?
 * -------------------------------------------------------------------------------------
 *
 * - Imutável (dados não podem ser alterados após criação) - Thread-safe (seguro em ambientes
 * concorrentes) - Menos código (não precisa de getters, setters, equals, hashCode)
 *
 * 👉 Ideal para eventos de domínio
 *
 *
 * ------------------------------------------------------------------------------------- 📊 CAMPO DO
 * EVENTO -------------------------------------------------------------------------------------
 *
 * orderId:
 *
 * - Identificador único do pedido - Permite correlacionar eventos ao longo do fluxo - Usado por
 * consumidores (ex: EmailConsumer)
 *
 *
 * ------------------------------------------------------------------------------------- ⚠️ BOAS
 * PRÁTICAS -------------------------------------------------------------------------------------
 *
 * ✅ Manter o evento enxuto (apenas dados necessários) ✅ Não adicionar lógica de negócio ✅ Evitar
 * dependência de outras classes
 *
 * ❌ NÃO FAZER: - Não enviar entidade completa (Order) - Não incluir regras ou validações
 *
 *
 * ------------------------------------------------------------------------------------- 🔒
 * IDEMPOTÊNCIA (CONTEXTO)
 * -------------------------------------------------------------------------------------
 *
 * Consumidores deste evento devem garantir:
 *
 * ✅ O mesmo evento NÃO pode gerar efeitos duplicados
 *
 * Exemplo: - Email não pode ser enviado duas vezes
 *
 *
 * ------------------------------------------------------------------------------------- 🚀 EVOLUÇÃO
 * FUTURA -------------------------------------------------------------------------------------
 *
 * Este evento pode evoluir para incluir metadados:
 *
 * - version (controle de compatibilidade) - correlationId (rastreamento distribuído) - createdAt
 * (auditoria)
 *
 * Exemplo:
 *
 * public record PaymentProcessedEvent( UUID orderId, String version, Instant createdAt ) {}
 *
 */
public record PaymentProcessedEvent(UUID orderId) {
}
