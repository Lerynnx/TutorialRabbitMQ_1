package com.rabbitmq.tutorial;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Queue;

@Configuration
@EnableRabbit
public class RabbitConfig {

    @Bean
    MessageConverter jackson2MessageConverter() {
        // Create the converter. In this Spring AMQP version the
        // method setStrictContentTypeMatch(boolean) is not available,
        // so we keep the default behaviour. For permissive behaviour
        // we added a byte[] fallback handler in the Receiver.
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        return converter;
    }

    @Bean
    SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    // Define the "hello" queue so Sender can autowire it and the broker will create it if missing
    @Bean
    Queue helloQueue() {
        // The broker already has a queue named "hello" declared as durable=true.
        // Use durable=true here to avoid PRECONDITION_FAILED errors when declaring the queue.
        return new Queue("hello", true);
    }
}