package com.rabbitmq.tutorial.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Queue;

/**
 * Configuración de RabbitMQ para la aplicación.
 *
 * Esta clase registra:
 * - Un convertidor JSON basado en Jackson (para serializar/deserializar mensajes).
 * - Una fábrica de contenedores de listeners que usa ese convertidor.
 * - Un RabbitTemplate configurado con el convertidor para que los productores
 *   puedan llamar a convertAndSend(obj) y enviar JSON correctamente.
 * - Un bean Queue llamado "hello" (durable) para asegurar compatibilidad con
 *   la cola existente en el broker.
 *
 */
@Configuration
@EnableRabbit
public class RabbitConfig {

    /**
     * MessageConverter que usa Jackson para convertir objetos <-> JSON.
     *
     * @return instancia de MessageConverter basada en Jackson
     */
    @Bean
    MessageConverter jackson2MessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        return converter;
    }

    /**
     * Fábrica de contenedores para listeners Rabbit. Se configura con el
     * ConnectionFactory y el MessageConverter para que los @RabbitListener
     * reciban objetos ya deserializados.
     *
     * @param connectionFactory la factoría de conexiones de RabbitMQ
     * @param messageConverter el convertidor de mensajes (Jackson)
     * @return SimpleRabbitListenerContainerFactory configurada
     */
    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }

    /**
     * RabbitTemplate configurado con el MessageConverter para que los productores
     * serialicen objetos a JSON automáticamente.
     *
     * @param connectionFactory la factoría de conexiones
     * @param messageConverter el convertidor de mensajes
     * @return RabbitTemplate configurado
     */
    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    /**
     * Declaración de la cola "hello". Se marca como durable=true para
     * coincidir con la cola existente en el broker y evitar errores de
     * PRECONDITION_FAILED si la cola ya está declarada con durable=true.
     *
     * @return Queue llamada "hello" y durable
     */
    @Bean
    Queue helloQueue() {
        return new Queue("hello", true);
    }
}