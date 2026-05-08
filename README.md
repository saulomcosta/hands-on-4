# 🚀 Arquitetura Orientada a Eventos com RabbitMQ

## 📖 Visão Geral da Fase 4

A Fase 4 representa uma evolução importante da arquitetura da aplicação.

Até as fases anteriores, o sistema possuía um fluxo mais linear e acoplado, onde uma única operação executava várias responsabilidades ao mesmo tempo.

Agora, a aplicação passa a operar utilizando:

* comunicação assíncrona,
* eventos de negócio,
* mensageria,
* filas,
* processamento desacoplado,
* tolerância a falhas,
* resiliência.

---

# 🎯 Objetivo da Fase 4

O objetivo desta fase é ensinar como sistemas modernos distribuídos funcionam em empresas de grande escala.

Aqui o projeto deixa de ser apenas uma API REST tradicional e começa a se comportar como uma arquitetura enterprise baseada em eventos.

---

# 🧠 O que foi aprendido nesta fase

Nesta etapa foram introduzidos conceitos extremamente utilizados em:

* fintechs,
* marketplaces,
* e-commerces,
* bancos,
* sistemas de delivery,
* aplicações distribuídas,
* microsserviços,
* plataformas de alta escala.

---

## ✅ Conceitos aprendidos

### 📡 RabbitMQ

Broker responsável por trafegar mensagens entre serviços.

---

### 📨 Filas (Queues)

Local onde mensagens ficam armazenadas até serem consumidas.

---

### 🔀 Exchanges

Responsáveis por receber mensagens e decidir para qual fila enviar.

---

### 🧭 Routing Keys

Regras de roteamento utilizadas para direcionar eventos.

---

### 👂 Consumers

Serviços que escutam filas e executam ações.

---

### 📤 Publishers

Componentes responsáveis por publicar eventos.

---

### ☠️ Dead Letter Queue (DLQ)

Fila de erro utilizada quando uma mensagem falha várias vezes.

---

### 🔁 Retry automático

Reprocessamento automático de mensagens em caso de falha.

---

### ⚡ Processamento assíncrono

A aplicação não precisa esperar todas as operações terminarem para responder ao usuário.

---

# 🏗️ Evolução da Arquitetura

---

## 🔹 Antes (Fluxo síncrono e acoplado)

```text
Cliente
   ↓
API REST
   ↓
Salvar pedido
   ↓
Processar pagamento
   ↓
Enviar email
   ↓
Resposta final
```

### ❌ Problemas deste modelo

* Alto acoplamento
* Lentidão
* Difícil escalabilidade
* Falha em um serviço derruba o fluxo inteiro
* Maior consumo de recursos
* Difícil manutenção

---

# ✅ Agora (Arquitetura orientada a eventos)

```text
Cliente
   ↓
OrderController
   ↓
OrderService
   ↓
Banco de Dados
   ↓
RabbitMQ (Exchange)
   ↓
────────────────────────────
↓                          ↓
payment.queue          email.queue
↓                          ↓
PaymentConsumer       EmailConsumer
↓                          ↓
Pagamento             Envio de Email
```

---

# 🧠 Organograma Completo da Aplicação

```text
┌────────────────────┐
│     Cliente API    │
└─────────┬──────────┘
          │ HTTP Request
          ▼
┌────────────────────┐
│  OrderController   │
└─────────┬──────────┘
          │
          ▼
┌────────────────────┐
│    OrderService    │
└─────────┬──────────┘
          │
          ├──────────────► Salva no PostgreSQL
          │
          ▼
┌────────────────────┐
│ OrderEventPublisher│
└─────────┬──────────┘
          │
          ▼
┌────────────────────┐
│ RabbitMQ Exchange  │
│   order.exchange   │
└──────┬───────┬─────┘
       │       │
       ▼       ▼
┌──────────┐ ┌──────────┐
│payment.q │ │ email.q  │
└────┬─────┘ └────┬─────┘
     │            │
     ▼            ▼
┌────────────┐ ┌────────────┐
│PaymentCons │ │EmailCons   │
└────┬───────┘ └────┬───────┘
     │              │
     ▼              ▼
Pagamento       Envio Email
```

---

# 🔥 Fluxo completo da aplicação

---

## 1️⃣ Cliente cria pedido

O cliente envia um POST para:

```http
POST /orders
```

---

## 2️⃣ Pedido é salvo

O `OrderService` salva os dados no PostgreSQL.

Exemplo de log:

```text
Hibernate: insert into orders...
```

---

## 3️⃣ Evento é publicado

Após salvar o pedido:

```text
OrderCreatedEvent
```

é enviado ao RabbitMQ.

---

## 4️⃣ RabbitMQ distribui mensagens

A exchange:

```text
order.exchange
```

roteia os eventos para:

* `payment.queue`
* `email.queue`

---

## 5️⃣ Consumers executam ações

Cada consumer possui responsabilidade única:

### 💳 PaymentConsumer

Responsável por processar pagamentos.

### 📧 EmailConsumer

Responsável por envio de emails.

---

# 🚨 Tratamento de falhas

A aplicação foi preparada para suportar falhas sem perder mensagens.

---

# 🔁 Retry automático

Quando ocorre erro no consumer:

```text
RuntimeException
```

o Spring RabbitMQ executa novas tentativas automaticamente.

---

## Exemplo:

```yaml
retry:
  enabled: true
  max-attempts: 3
```

Fluxo:

```text
1ª tentativa ❌
2ª tentativa ❌
3ª tentativa ❌
```

---

# ☠️ Dead Letter Queue (DLQ)

Após exceder as tentativas:

```text
email.queue
        ↓
dlx.exchange
        ↓
email.dlq
```

A mensagem vai para fila de erro.

---

# 🧠 Benefícios da DLQ

* Não perde mensagens
* Permite auditoria
* Permite reprocessamento
* Facilita debug
* Aumenta resiliência

---

# 📈 Ganhos reais desta arquitetura

## ✅ Escalabilidade

Cada consumer pode crescer separadamente.

Exemplo:

```text
10 consumidores de pagamento
2 consumidores de email
```

---

## ✅ Resiliência

Se email falhar:

* pagamento continua funcionando,
* sistema não para.

---

## ✅ Desacoplamento

O produtor não conhece os consumidores.

Ele apenas publica eventos.

---

## ✅ Performance

A API responde rapidamente sem esperar todo processamento terminar.

---

## ✅ Facilidade de manutenção

Novos serviços podem ser adicionados sem alterar o produtor.

Exemplo:

* SMSConsumer
* NotificationConsumer
* FraudConsumer
* AnalyticsConsumer

---

# 🛠️ Tecnologias utilizadas

## Backend

* Java 21
* Spring Boot
* Spring AMQP
* Spring Data JPA

---

## Banco

* PostgreSQL

---

## Mensageria

* RabbitMQ

---

## Observabilidade

* Prometheus
* Grafana
* Zipkin

---

## Cache

* Redis

---

## Containerização

* Docker
* Docker Compose

---

# 📂 Estrutura do Projeto

```text
src/main/java/com/hands_on/arquiteto
│
├── config
│   └── RabbitConfig.java
│
├── controller
│   └── OrderController.java
│
├── entity
│   └── Order.java
│
├── repository
│   └── OrderRepository.java
│
├── service
│   └── OrderService.java
│
├── messaging
│   ├── publisher
│   │   └── OrderEventPublisher.java
│   │
│   ├── consumer
│   │   ├── PaymentConsumer.java
│   │   └── EmailConsumer.java
│   │
│   └── payload
│       └── PaymentProcessedEvent.java
```

---

# ⚙️ Serviços utilizados atualmente

Neste momento da aplicação, os serviços necessários são:

```bash
docker compose up -d postgres rabbitmq
```

---

# 🚀 Como executar a aplicação

## 1️⃣ Subir infraestrutura

Na pasta:

```text
arquitetura-enterprise
```

execute:

```bash
docker compose up -d postgres rabbitmq
```

---

## 2️⃣ Executar aplicação Spring Boot

Via Maven:

```bash
./mvnw spring-boot:run
```

ou pela IDE.

---

## 3️⃣ Enviar requisição

Exemplo no Postman:

```http
POST /orders
```

Body:

```json
{
  "amount": 100
}
```

---

# 📊 Resultado esperado

## Logs esperados

```text
Hibernate: insert into orders...

Processando pagamento para pedido...

PaymentProcessedEvent publicado...

Enviando e-mail...

E-mail enviado...
```

---

# 🧪 Simulação de falhas

A aplicação também permite simular falhas para estudar:

* retry,
* redelivery,
* DLQ,
* comportamento resiliente.

---

# 🧠 O que esta fase ensina na prática

Esta fase ensina exatamente como sistemas enterprise modernos funcionam internamente.

Você aprende:

* arquitetura desacoplada,
* comunicação assíncrona,
* resiliência,
* tolerância a falhas,
* mensageria,
* processamento distribuído,
* observabilidade,
* padrões utilizados em Big Techs.

---

# 🎓 Conclusão

A Fase 4 marca a transição de uma aplicação CRUD tradicional para uma arquitetura orientada a eventos.

Agora o sistema possui:

* maior robustez,
* melhor escalabilidade,
* separação de responsabilidades,
* capacidade de crescimento,
* tolerância a falhas,
* processamento assíncrono real.

Essa é a base fundamental para evoluir posteriormente para:

* microsserviços,
* saga pattern,
* event sourcing,
* Kafka,
* arquiteturas distribuídas de alta escala.
