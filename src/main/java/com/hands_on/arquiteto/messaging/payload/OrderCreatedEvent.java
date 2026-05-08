package com.hands_on.arquiteto.messaging.payload;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 📌 Evento de Domínio: OrderCreatedEvent
 *
 * 🧠 Representa um fato de negócio que já aconteceu no sistema: "Um pedido foi criado".
 *
 * ⚠️ IMPORTANTE: Este NÃO é um DTO qualquer e nem uma entidade. É um EVENTO DE DOMÍNIO — ou seja,
 * ele comunica uma mudança de estado relevante.
 *
 * ----------- 🎯 OBJETIVO ----------
 *
 * Permitir que outros componentes/serviços reajam a este evento de forma desacoplada, sem depender
 * diretamente do OrderService.
 *
 * Exemplo:
 *
 * OrderCreatedEvent → → PaymentService processa pagamento → NotificationService envia email
 *
 *
 * ------------ ✅ BENEFÍCIOS --------
 *
 * - Desacoplamento entre serviços - Fácil expansão (novos consumidores podem surgir) - Arquitetura
 * orientada a eventos (EDA) - Escalabilidade e resiliência
 *
 *
 * ------------ 📦 POR QUE USAR RECORD? ----------
 *
 * - Imutável (não pode ser alterado depois de criado) - Thread-safe (segurança de thread) por
 * natureza - Menos código (sem getters, setters, equals, hashCode)
 *
 * 👉 Ideal para eventos!
 *
 *
 * ------------📊 CAMPOS DO EVENTO -------------
 *
 * orderId: - Identificador único do pedido - Usado para rastrear e correlacionar eventos ao longo
 * do fluxo
 *
 * amount: - Valor do pedido - Necessário para processamento de pagamento
 *
 *
 * --------------⚠️ BOAS PRÁTICAS -------------
 *
 * ✅ Enviar apenas o necessário (não enviar entidade completa) ✅ Não incluir lógica dentro do evento
 * ✅ Manter compatibilidade (pensar em versionamento futuro)
 *
 * ❌ NÃO FAZER: - Colocar regras de negócio aqui - Depender de outras classes
 *
 *
 * --------------- 🔁 PAPEL NO FLUXO ---------------
 *
 * OrderService cria o evento:
 *
 * OrderCreatedEvent ↓ PaymentConsumer recebe ↓ Gera PaymentProcessedEvent
 *
 *
 * --------------- 🚀 EVOLUÇÃO FUTURA -------------
 *
 * Pode evoluir para:
 *
 * - versionamento (eventVersion) - correlationId (rastreamento distribuído) - timestamp (auditoria)
 *
 * Exemplo:
 *
 * public record OrderCreatedEvent( UUID orderId, BigDecimal amount, String version, Instant
 * createdAt ) {}
 *
 */

public record OrderCreatedEvent(UUID orderId, BigDecimal amount) {
}

