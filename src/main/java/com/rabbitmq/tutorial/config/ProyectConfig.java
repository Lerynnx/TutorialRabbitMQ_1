package com.rabbitmq.tutorial.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.rabbitmq.tutorial.messaging.Receiver;
import com.rabbitmq.tutorial.messaging.Sender;

/**
 * Esta clase define beans específicos para los perfiles de ejemplo
 * "tutorial" y "hello-world". Proporciona una cola llamada "hello"
 * y beans que se activan según el perfil (sender/receiver).
 *
 * Notas:
 * - Se usa @Profile para que estas definiciones estén activas solo en
 *   ciertos perfiles de Spring Boot (evita colisiones en otros entornos).
 */
@Profile({"tutorial","hello-world"})
@Configuration
public class ProyectConfig {

    /**
     * Bean que declara la cola "hello" en el broker. La visibilidad package-private
     * es suficiente para Spring pero se documenta su propósito.
     *
     * @return Queue con nombre "hello" y propiedades por defecto (no durable)
     */
    @Bean
    Queue hello() {
        return new Queue("hello");
    }

    /**
     * Bean para crear un Receiver cuando el perfil "receiver" esté activo.
     * Esto facilita ejecutar solo la parte receptora en el tutorial.
     */
    @Profile("receiver")
    @Bean
    Receiver receiver() {
        return new Receiver();
    }

    /**
     * Bean para crear un Sender cuando el perfil "sender" esté activo.
     * Facilita ejecutar solo la parte emisora en el tutorial.
     */
    @Profile("sender")
    @Bean
    Sender sender() {
        return new Sender();
    }
}