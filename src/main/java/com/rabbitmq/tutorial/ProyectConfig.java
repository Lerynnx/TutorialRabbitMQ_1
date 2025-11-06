package com.rabbitmq.tutorial;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile({"tutorial","hello-world"})
@Configuration
public class ProyectConfig {

    @Bean
    Queue hello() {
        return new Queue("hello");
    }

    @Profile("receiver")
    @Bean
    Receiver receiver() {
        return new Receiver();
    }

    @Profile("sender")
    @Bean
    Sender sender() {
        return new Sender();
    }
}
