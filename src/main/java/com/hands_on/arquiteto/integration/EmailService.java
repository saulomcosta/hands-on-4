package com.hands_on.arquiteto.integration;

import org.springframework.stereotype.Service;

/**
 * Serviço de integração responsável pelo envio de e-mails dentro da aplicação.
 *
 * 🔹 PAPEL NA ARQUITETURA: Esta classe representa uma camada de integração (integration layer),
 * responsável por interagir com sistemas externos ou simulações deles.
 *
 * Neste caso, ela simula o envio de e-mails para notificação de eventos importantes do sistema,
 * como: - criação de pedidos - confirmação de pagamento - finalização de processos de negócio
 *
 * 🔹 ANOTAÇÃO @Service: Indica ao Spring Framework que esta classe é um componente gerenciado pelo
 * container de injeção de dependência (IoC Container).
 *
 * Isso permite: - injeção automática em outras classes (@Autowired ou construtor) - gerenciamento
 * de ciclo de vida pelo Spring - reutilização como singleton por padrão
 *
 * 🔹 IMPORTÂNCIA NA APLICAÇÃO: Mesmo sendo uma simulação simples (System.out.println), esta classe
 * representa um ponto crítico de integração externa, que em um sistema real poderia se conectar a:
 * - SMTP (envio real de e-mails) - serviços como SendGrid, AWS SES ou Mailgun - filas de eventos
 * assíncronos
 */

@Service
public class EmailService {

    /**
     * Método responsável por simular o envio de um e-mail.
     *
     * 🔹 RESPONSABILIDADE: Executa a ação de notificação após eventos importantes do sistema, como
     * pagamento aprovado ou criação de pedidos.
     *
     * 🔹 COMPORTAMENTO ATUAL: Atualmente apenas imprime no console uma mensagem simulando o envio.
     *
     * Em uma implementação real, este método poderia: - montar template de e-mail - chamar API
     * externa de e-mail - enviar de forma síncrona ou assíncrona - tratar falhas de entrega
     *
     * 🔹 IMPACTO NO FLUXO DA APLICAÇÃO: Este método geralmente é chamado dentro de fluxos de
     * negócio (ex: OrderService), e portanto pode influenciar diretamente a experiência do usuário
     * final.
     *
     * Se falhar, pode gerar: - perda de notificação - inconsistência entre estado do sistema e
     * comunicação externa
     */
    public void sendEmail() {
        System.out.println("Sending email...");
    }
}
