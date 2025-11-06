package com.rabbitmq.tutorial;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class Sender {

    @Autowired
    private RabbitTemplate template;

    // Specify which Queue bean to inject to avoid ambiguity when multiple Queue beans exist
    @Autowired
    @Qualifier("helloQueue")
    private Queue queue;

    @Scheduled(fixedDelay = 1000, initialDelay = 500)
    public void send() {
        // Send a String message
        String message = sendStringMessage();
        System.out.println(" [x] Sent '" + message + "'");

        // Send a Usuario object (will be serialized to JSON by RabbitTemplate's converter)
        String jsonMessage;
        try {
            jsonMessage = sendUsuarioMessage();
            System.out.println(" [x] Sent Usuario JSON '" + jsonMessage + "'");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private String sendStringMessage() {
        String message = "Hello World!";
        this.template.convertAndSend(queue.getName(), message);
        return message;
    }

    private String sendUsuarioMessage() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Usuario user = new Usuario(1, "John");
        // Send the Usuario object directly; RabbitTemplate has Jackson converter configured
        this.template.convertAndSend(queue.getName(), user);
        // Return the JSON string for logging
        return objectMapper.writeValueAsString(user);
    }
}