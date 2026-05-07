package com.hands_on.arquiteto.entity;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * ========== 📦 ENTITY: ORDER (PEDIDO) ==========
 *
 * 🧠 RESPONSABILIDADE: Representa o modelo de dados de um pedido no sistema, sendo mapeado
 * diretamente para a tabela "orders" no banco.
 *
 * ----------------- 📌 PAPEL NA ARQUITETURA:
 *
 * - Camada de persistência (JPA / Hibernate) - Armazena o estado do pedido ao longo do fluxo - Base
 * para implementação de idempotência
 *
 * ---------------- 🚨 IMPORTANTE:
 *
 * Esta classe: ✅ Representa dados ❌ NÃO contém regras de negócio complexas
 *
 * A lógica de processamento deve ficar nos Services/Consumers.
 *
 * ---------------- � IDEMPOTÊNCIA (PONTO CRÍTICO)
 *
 * Esta entidade é peça-chave para idempotência.
 *
 * 🧠 Como funciona:
 *
 * - Cada pedido possui um ID único (UUID) - O campo "status" representa o estado do processamento
 *
 * Exemplo de fluxo:
 *
 * 1. Pedido criado → status = "CREATED" 2. Consumer processa pagamento → status = "PROCESSED"
 *
 * 👉 Se a mesma mensagem chegar novamente:
 *
 * - O sistema consulta o banco - Verifica o status - Se já estiver "PROCESSED" → ignora
 *
 * ✅ Isso evita: - Cobrança duplicada - Processamento duplicado - Inconsistência financeira
 *
 * --------------- ⚠️ CONCLUSÃO:
 *
 * A idempotência NÃO está aqui diretamente, mas esta entidade torna ela possível.
 *
 * A regra de idempotência deve ser implementada no Consumer:
 *
 * if (order.status == PROCESSED) → NÃO processa novamente
 *
 */

@Entity
/**
 * Define explicitamente o nome da tabela no banco de dados.
 *
 * Sem isso, o Hibernate usaria "order" por padrão (o que causaria erro, pois ORDER é palavra
 * reservada no PostgreSQL).
 *
 * Aqui foi definido como "orders" para evitar conflito.
 */
@Table(name = "orders")
/**
 * Lombok: gera automaticamente getters e setters Evita código boilerplate repetitivo
 */
@Getter
@Setter
/**
 * Gera construtor vazio (necessário para JPA)
 */
@NoArgsConstructor
/**
 * Gera construtor com todos os campos
 */
@AllArgsConstructor
/**
 * Builder pattern: Permite criar objetos de forma mais legível e flexível
 *
 * Exemplo: Order.builder() .amount(BigDecimal.valueOf(100)) .status("CREATED") .build();
 */
@Builder(toBuilder = true)
public class Order {



    /**
     * ============= 🔑 IDENTIFICADOR ÚNICO (UUID) =============
     *
     * - Identifica o pedido de forma única - Usado para rastreabilidade em sistemas distribuídos
     *
     * ✅ Benefícios: - Evita colisão entre serviços - Permite idempotência baseada em ID
     *
     * ⚠️ IMPORTANTE: Este ID é a base para evitar duplicidade
     */

    @Id
    @GeneratedValue
    private UUID id;

    /**
     * ============= 💰 VALOR DO PEDIDO =============
     *
     * Representa o valor monetário do pedido.
     *
     * ✅ Uso de BigDecimal: - Evita erro de precisão (ponto flutuante) - Essencial para sistemas
     * financeiros
     */

    private BigDecimal amount;

    /**
     * ============ 📊 STATUS DO PEDIDO (CONTROLE DE PROCESSAMENTO) =============
     *
     * Representa o estado atual do pedido ao longo do sistema.
     *
     * 🧠 Papel fundamental para idempotência
     *
     * ------------- POSSÍVEIS VALORES:
     *
     * - CREATED → Pedido criado, ainda não processado - PROCESSED → Pagamento já realizado - FAILED
     * → Falha no processamento
     *
     * ------------- 🔒 USO NA IDEMPOTÊNCIA:
     *
     * Antes de processar um pedido, o sistema deve verificar:
     *
     * if (status == PROCESSED) → não processar novamente
     *
     * ✅ Isso garante: - Consistência - Segurança financeira - Processamento exatamente uma vez (na
     * prática)
     *
     * ------------- 🚀 MELHORIA FUTURA:
     *
     * Em produção, recomenda-se usar ENUM:
     *
     * OrderStatus { CREATED, PROCESSED, FAILED }
     *
     */

    private String status;

}
