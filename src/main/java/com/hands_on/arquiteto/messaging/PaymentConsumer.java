package com.hands_on.arquiteto.messaging;

import java.util.Random;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import com.hands_on.arquiteto.config.RabbitConfig;
import com.hands_on.arquiteto.entity.Order;
import com.hands_on.arquiteto.repository.OrderRepository;


/**
 * ============ 📥 SERVICE: PAYMENT CONSUMER (PROCESSAMENTO DE PAGAMENTO) ============
 *
 * 🧠 RESPONSABILIDADE: Consumir eventos da fila do RabbitMQ e processar pagamentos de pedidos de
 * forma assíncrona e resiliente.
 *
 * ------------ 📡 CONTEXTO ARQUITETURAL (EVENT-DRIVEN):
 *
 * OrderService (HTTP) ↓ OrderPublisher (Producer) ↓ RabbitMQ (Exchange → Queue) ↓ PaymentConsumer
 * (este componente)
 *
 * ----------- 🔁 COMPORTAMENTO DO CONSUMER:
 *
 * 1. Escuta a fila "payment.queue" 2. Recebe evento com dados do pedido (Order) 3. Consulta o banco
 * para obter o estado atual 4. Aplica regra de idempotência 5. Processa pagamento (ou ignora)
 *
 * ---------- 🛡️ RESILIÊNCIA:
 *
 * - Retry automático (configurado no application.yml) - Backoff (Tempo de espera) exponencial entre
 * tentativas - Integração com DLQ (Dead Letter Queue)
 *
 * ---------- 🔁 IDPOTÊNCIA:
 *
 * Obs: Indepotência é crítica para evitar processamento duplicado, especialmente em cenários de
 * falha. Ex: Se a mensagem for entregue diuas vezes, o sistema deve garantir que o pagamento seja
 * processado apenas uma vez.
 *
 * ---------- 🔒 IDEMPOTÊNCIA (CRÍTICO):
 *
 * Evita que o mesmo pedido seja processado múltiplas vezes.
 *
 * Estratégia: - Buscar pedido no banco - Verificar status - Se já processado → IGNORA
 *
 * ✅ Evita: - Cobrança duplicada - Inconsistência de dados
 *
 * ---------- ⚠️ OBSERVAÇÕES DE PRODUÇÃO:
 *
 * - Substituir System.out por logs estruturados - Adicionar (Identificador Único) (MDC) -
 * Implementar tratamento de exceções mais refinado - Possível uso de lock ou versionamento
 * (optimistic locking - Bloqueio Otimista). É uma estratégia de concorrência que assume que
 * conflitos entre consumidores são raros e, portanto, não bloqueia os recursos imediatamente. Em
 * vez disso, ele permite que múltiplos consumidores acessem o mesmo recurso simultaneamente, mas
 * verifica se houve alterações antes de salvar as mudanças. Se detectar um conflito (ou seja, outro
 * consumidor modificou o recurso), ele pode lançar uma exceção ou tentar novamente.
 *
 */
@Service // Registra esta classe como um Bean gerenciado pelo Spring
public class PaymentConsumer {


    /**
     * ============== 🗄️ REPOSITORY (ACESSO AO BANCO) ==============
     *
     * Responsável por: - Buscar estado do pedido - Persistir alterações (status)
     *
     * 🧠 Papel crítico na idempotência: O banco é a "fonte da verdade". Refere-se a uma estratégia
     * arquitetural para garantir que, ao processar mensagens, um sistema não execute a mesma
     * operação mais de uma vez, mesmo que a mensagem seja entregue repetidas vezes.
     */

    private final OrderRepository orderRepository;

    /**
     * 📌 Injeção via construtor (boa prática)
     *
     * ✅ Facilita testes (mock) ✅ Garante imutabilidade
     */
    public PaymentConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }


    /**
     * ============== 📥 MÉTODO LISTENER (CONSUMIDOR DE EVENTOS) =============
     *
     * @RabbitListener: Define que este método escuta a fila configurada.
     *
     *                  ----------------- 📦 FILA:
     *
     *                  - Nome: payment.queue - Origem: Exchange "order.exchange" - Evento:
     *                  "order.created"
     *
     *                  ----------------- 🔄 CONVERSÃO AUTOMÁTICA:
     *
     *                  O Spring: - Recebe JSON da fila - Converte para objeto Order (Jackson)
     *
     *                  ---------------- 🔁 REGRAS DE EXECUÇÃO:
     *
     *                  ✅ Sucesso: → mensagem é confirmada (ACK automático)
     *
     *                  ❌ Exceção: → mensagem NÃO é confirmada → entra no retry automático
     *
     *                  ❌ Falha após retries: → enviada para DLQ
     *
     */
    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void process(Order order) {


        // ==========================================================
        // 🔍 BUSCA ESTADO ATUAL NO BANCO
        // ==========================================================
        //
        // 🧠 Nunca confiar apenas na mensagem:
        // Sempre validar o estado persistido
        //
        Order existing = orderRepository.findById(order.getId()).orElseThrow();


        // ==========================================================
        // 🔒 REGRA DE IDEMPOTÊNCIA
        // ==========================================================
        //
        // Se o pedido já foi processado anteriormente:
        // → NÃO processar novamente
        //
        // 🧠 Isso acontece quando:
        // - mensagem duplicada
        // - retry
        // - reentrega pelo broker
        //
        // Importante saber o que é um Broker de Mensagens:
        // é um software intermediário que permite que diferentes aplicações, sistemas ou serviços
        // se comuniquem e troquem informações de forma assíncrona.
        //
        if ("PROCESSED".equals(existing.getStatus())) {
            System.out.println("Pedido já processado: " + order.getId());

            // ✅ Importante:
            // NÃO lança erro → mensagem será ACK (descartada corretamente)
            return;
        }


        // ==========================================================
        // 📥 INÍCIO DO PROCESSAMENTO
        // ==========================================================

        System.out.println("📥 Recebido pedido para pagamento: " + order.getId());


        // ==========================================================
        // 🔥 SIMULAÇÃO DE FALHA
        // ==========================================================
        //
        // 🧠 Simula cenários reais:
        // - gateway de pagamento indisponível
        // - timeout externo
        // - erro inesperado
        //
        if (new Random().nextBoolean()) {

            System.out.println("❌ Falha ao processar pagamento: " + order.getId());

            // 🚨 Importante:
            // Lançar exceção faz o Spring:
            // → NÃO confirmar a mensagem (no ACK)
            // → Reenfileirar para retry
            //
            // Lançar exceção => ativa retry automático (configurado no application.yml) => se
            // falhar após retries => vai para DLQ
            throw new RuntimeException("Erro no processamento de pagamento");

        }

        // ==========================================================
        // ✅ ATUALIZA ESTADO (SUCESSO)
        // ==========================================================
        //
        // Marca pedido como processado
        // 🧠 O banco é a "fonte da verdade" para o estado do pedido
        existing.setStatus("PROCESSED");

        // Persiste alteração no banco
        orderRepository.save(existing);

        // ==========================================================
        // ✅ SUCESSO
        // ==========================================================
        //
        // Se chegou aqui:
        // → Processamento foi concluído
        // → Mensagem será confirmada automaticamente (ACK)
        //
        System.out.println("✅ Pagamento realizado com sucesso: " + order.getId());
    }


    /**
     * =========== ☠️ MÉTODO LISTENER DA DEAD LETTER QUEUE (DLQ) ==========
     *
     * 🧠 RESPONSABILIDADE: Este método consome mensagens que falharam no processamento principal
     * após todas as tentativas de retry.
     *
     * ------------- 📡 CONTEXTO:
     *
     * - Fila: payment.dlq - Origem: Dead Letter Exchange (dlx.exchange) - Mensagens chegam aqui
     * quando:
     *
     * ❌ O consumer principal falha ❌ O retry automático atinge o limite configurado
     *
     * ------------- 🔁 FLUXO DE ERRO:
     *
     * payment.queue (consumer falha) ↓ retry (3 tentativas) ↓ DLX (dlx.exchange) ↓ payment.dlq ↓
     * handleError()
     *
     * ------------- 📌 COMPORTAMENTO:
     *
     * ✅ Não há retry aqui (a menos que você configure explicitamente) ✅ Mensagem já considerada
     * falha definitiva
     *
     * ------------ 🎯 OBJETIVO:
     *
     * - Registrar erro - Permitir auditoria - Possibilitar reprocessamento manual
     *
     * ------------ ⚠️ IMPORTANTE:
     *
     * Este método NÃO deve tentar processar novamente automaticamente, pois pode causar:
     *
     * ❌ loop infinito ❌ duplicidade
     *
     * ------------- 🚀 MELHORIAS EM PRODUÇÃO:
     *
     * - Persistir erro em banco (tabela de falhas) - Enviar para ferramenta de observabilidade (ex:
     * ELK, Datadog) - Criar mecanismo de reprocessamento controlado - Incluir correlationId para
     * rastreabilidade
     *
     */
    @RabbitListener(queues = RabbitConfig.PAYMENT_DLQ)
    public void handleError(Order order) {

        // ==========================================================
        // 📥 RECEBIMENTO DE MENSAGEM COM ERRO
        // ==========================================================
        //
        // Mensagem já foi considerada falha após retries
        //
        System.out.println("☠️ Pedido enviado para DLQ: " + order.getId());

        // ==========================================================
        // 📌 POSSÍVEIS AÇÕES (FUTURO)
        // ==========================================================
        //
        // Aqui você poderia:
        //
        // - Salvar erro em banco
        // - Notificar times (Slack, email, etc.)
        // - Disparar reprocessamento manual
        // - Gerar métricas (monitoramento)
        //
        // Exemplo:
        // errorRepository.save(...)

    }
}
