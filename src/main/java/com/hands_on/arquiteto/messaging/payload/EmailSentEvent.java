package com.hands_on.arquiteto.messaging.payload;

import java.util.UUID;

/**
 * ============ 📧 EVENTO DE DOMÍNIO: EMAIL SENT ============
 *
 * 🧠 VISÃO GERAL: Este evento representa um fato de negócio concluído no sistema:
 *
 * 👉 "O e-mail de notificação do pedido foi enviado com sucesso"
 *
 * Assim como os outros eventos, este NÃO é uma entidade nem um DTO técnico. É um EVENTO DE DOMÍNIO,
 * que representa algo relevante que aconteceu.
 *
 * ----------- 🎯 OBJETIVO -----------
 *
 * Permitir que outros sistemas ou componentes reajam ao envio do e-mail, mantendo o sistema
 * desacoplado.
 *
 * Exemplos de uso:
 *
 * EmailSentEvent ↓ - Auditoria (registro de envio) - Analytics (métricas de notificação) -
 * Integração com CRM
 *
 *
 * ------------- 📡 CONTEXTO NO FLUXO (EVENT-DRIVEN) -------------
 *
 * OrderCreatedEvent ↓ PaymentProcessedEvent ↓ EmailConsumer ↓ ✅ EmailSentEvent (este evento)
 *
 *
 * --------------- ✅ BENEFÍCIOS ------------
 *
 * - Desacoplamento total entre serviços - Possibilidade de expansão sem alterar fluxo existente -
 * Clareza semântica (evento representa um resultado final) - Base para observabilidade e auditoria
 *
 *
 * ------------- 📦 POR QUE USAR RECORD? ---------------
 *
 * - Imutável (não pode ser alterado após criação) - Thread-safe (seguro em concorrência) - Simples
 * e objetivo
 *
 * 👉 Ideal para transporte de eventos
 *
 *
 * -------------- 📊 CAMPO DO EVENTO ------------
 *
 * orderId:
 *
 * - Identificador do pedido relacionado ao e-mail enviado - Permite rastrear o fluxo completo do
 * pedido - Usado para correlação entre eventos
 *
 *
 * ------------- 🔒 IDEMPOTÊNCIA (CONTEXTO) ---------------
 *
 * Consumidores deste evento (se existirem) devem garantir:
 *
 * ✅ O mesmo evento NÃO deve gerar efeitos duplicados
 *
 * Exemplo: - Evitar registrar envio duplicado - Evitar múltiplas notificações externas
 *
 *
 * ----------------- ⚠️ BOAS PRÁTICAS --------------
 *
 * ✅ Manter evento simples e enxuto ✅ Não incluir lógica de negócio ✅ Não depender de entidades
 *
 * ❌ NÃO FAZER: - Não incluir campos desnecessários - Não acoplar com infraestrutura
 *
 *
 * -------------- 🚀 EVOLUÇÃO FUTURA ---------------
 *
 * Este evento pode evoluir para incluir:
 *
 * - timestamp (quando foi enviado) - correlationId (rastreamento distribuído) - status (ex:
 * SUCCESS, FAILED)
 *
 * Exemplo:
 *
 * public record EmailSentEvent( UUID orderId, Instant sentAt, String correlationId ) {}
 *
 */
public record EmailSentEvent(UUID orderId) {
}
