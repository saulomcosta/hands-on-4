package com.hands_on.arquiteto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CLASSE PRINCIPAL DA APLICAÇÃO SPRING BOOT
 *
 * Responsabilidade: - Ponto de entrada da aplicação Java - Inicializa o Spring Boot e todo o
 * Application Context - Realiza o bootstrap (inicialização) de toda a aplicação
 *
 * O que o Spring faz automaticamente ao iniciar: 1. Cria o ApplicationContext (container de
 * dependências) 2. Faz o scan de componentes (@Component, @Service, @Repository, @Controller) 3.
 * Configura auto-configurations (Web, JPA, DataSource, etc) 4. Inicializa servidor embutido
 * (Tomcat)
 *
 * RESULTADO: Sua aplicação sobe como um servidor web rodando em: http://localhost:8080
 */
@SpringBootApplication
/**
 * @SpringBootApplication = combinação de 3 anotações:
 *
 *                        1. @Configuration → indica que esta classe pode declarar beans Spring
 *
 *                        2. @EnableAutoConfiguration → ativa configuração automática do Spring Boot
 *                        (ex: configura banco, Tomcat, JPA automaticamente)
 *
 *                        3. @ComponentScan → faz varredura no pacote atual e subpacotes (encontra
 *                        controllers, services, repositories, etc)
 */
public class ArquitetoApplication {

	/**
	 * MÉTODO PRINCIPAL (ENTRY POINT)
	 *
	 * Este método é executado quando você roda: → mvn spring-boot:run → ou executa a classe no IDE
	 *
	 * Aqui o Spring Boot é iniciado.
	 *
	 * @param args argumentos de linha de comando (opcional)
	 */
	public static void main(String[] args) {
		/**
		 * Inicia toda a aplicação Spring Boot:
		 *
		 * - Cria o container de injeção de dependência - Sobe servidor Tomcat embutido - Inicializa
		 * banco de dados (DataSource) - Cria beans (Service, Repository, Controller) - Configura
		 * JPA/Hibernate
		 */
		SpringApplication.run(ArquitetoApplication.class, args);
	}

}
