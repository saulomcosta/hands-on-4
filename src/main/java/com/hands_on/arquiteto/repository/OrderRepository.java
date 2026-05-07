package com.hands_on.arquiteto.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hands_on.arquiteto.entity.Order;


/**
 * CAMADA: REPOSITORY (Acesso a Dados / Persistência)
 *
 * Responsabilidade principal: - Abstrair o acesso direto ao banco de dados - Fornecer operações
 * CRUD prontas (Create, Read, Update, Delete) - Eliminar a necessidade de SQL manual na maioria dos
 * casos
 *
 * IMPORTANTE: - Esta interface NÃO tem implementação manual - O Spring Data JPA gera
 * automaticamente uma implementação em runtime - Baseado no proxy do Hibernate + Spring Data
 *
 * Isso significa que: 👉 você não escreve SQL 👉 você não escreve implementação 👉 o Spring gera
 * tudo automaticamente
 */

/**
 * Esta interface herda JpaRepository, que já fornece automaticamente:
 *
 * - save(Order entity) → salva ou atualiza registro - findById(UUID id) → busca por ID - findAll()
 * → lista todos os registros - deleteById(UUID id) → remove registro - count() → conta registros
 *
 * E muitas outras funcionalidades prontas.
 */
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * PERSONALIZAÇÃO (opcional)
     *
     * Aqui você poderia declarar queries personalizadas, por exemplo:
     *
     * Exemplo: List<Order> findByStatus(String status);
     *
     * O Spring automaticamente gera a query SQL baseada no nome do método: SELECT * FROM orders
     * WHERE status = ?
     */
}
