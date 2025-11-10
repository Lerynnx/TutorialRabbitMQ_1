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
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de RabbitMQ y beans relacionados.
 *
 * Esta clase registra:
 * - Un convertidor JSON basado en Jackson (para serializar/deserializar mensajes).
 * - Una fábrica de contenedores de listeners que usa ese convertidor.
 * - Un RabbitTemplate configurado con el convertidor para que los productores
 *   puedan llamar a convertAndSend(obj) y enviar JSON correctamente.
 * - Un bean Queue llamado "hello" y una exchange "delayed.exchange" de tipo
 *   x-delayed-message (plugin) que permite retrasar por mensaje mediante header x-delay.
 */
@Configuration
@EnableRabbit
public class RabbitConfig {

    /**
     * ObjectMapper compartido configurado con JavaTimeModule para LocalDateTime.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

    /**
     * MessageConverter que usa Jackson para convertir objetos <-> JSON.
     *
     * @return instancia de MessageConverter basada en Jackson
     */
    @Bean
    MessageConverter jackson2MessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
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

    /**
     * Declaración de una exchange compatible con delayed-message-exchange plugin.
     * Nombre: delayed.exchange
     * Tipo: x-delayed-message
     * x-delayed-type: direct (usa routing key para entregar a cola 'hello')
     */
    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange("delayed.exchange", "x-delayed-message", true, false, args);
    }

    /**
	 * Binding entre la cola "hello" y la exchange "delayed.exchange"
	 * usando la routing key "hello".
	 *
	 * @param delayedExchange la exchange retrasada
	 * @param helloQueue la cola "hello"
	 * @return Binding configurado
	 */
    @Bean
    public Binding bindingDelayedToHello(CustomExchange delayedExchange, Queue helloQueue) {
        return BindingBuilder.bind(helloQueue).to(delayedExchange).with("hello").noargs();
    }
}