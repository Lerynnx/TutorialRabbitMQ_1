package com.rabbitmq.tutorial.messaging;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.tutorial.model.Usuario;

/**
 * Componente que envía mensajes a la cola RabbitMQ.
 *
 * Esta clase se ejecuta periódicamente (gracias a @Scheduled) y demuestra
 * dos formas de envío:
 *  - enviar un String simple (texto plano)
 *  - enviar un objeto Usuario que será serializado a JSON por Jackson
 *
 * El envío se realiza mediante el RabbitTemplate que tiene configurado un
 * MessageConverter (Jackson) en la configuración `RabbitConfig`.
 */
@Component
public class Sender {

    @Autowired
    private RabbitTemplate template;

    // Se califica explícitamente el bean Queue a inyectar para evitar ambigüedad
    @Autowired
    @Qualifier("helloQueue")
    private Queue queue;

    /**
     * Tarea periódica que envía mensajes cada segundo (configurado en el método)
     * Envía primero un texto y después un objeto Usuario serializado a JSON.
     */
    @Scheduled(fixedDelay = 1000, initialDelay = 500)
    public void send() {
        // Enviar un mensaje de texto simple
        String message = sendStringMessage();
        System.out.println(" [x] Sent '" + message + "'");

        // Enviar un objeto Usuario (serializado a JSON por el RabbitTemplate)
        String jsonMessage;
        try {
            jsonMessage = sendUsuarioMessage();
            System.out.println(" [x] Sent Usuario JSON '" + jsonMessage + "'");
        } catch (JsonProcessingException e) {
            // Imprimir traza en caso de error de serialización
            e.printStackTrace();
        }
    }

    /**
     * Envía un mensaje String simple a la cola.
     *
     * @return el texto enviado (para logging)
     */
    private String sendStringMessage() {
        String message = "Hello World!";
        this.template.convertAndSend(queue.getName(), message);
        return message;
    }

    /**
     * Envía un objeto Usuario. RabbitTemplate utiliza el MessageConverter
     * para serializar el objeto a JSON y establecer el header content-type.
     *
     * @return la representación JSON del Usuario (para logging)
     * @throws JsonProcessingException si falla la serialización manual usada para el log
     */
    private String sendUsuarioMessage() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Usuario user = new Usuario(1, "John");
        // El RabbitTemplate serializa el objeto a JSON automáticamente
        this.template.convertAndSend(queue.getName(), user);
        // Devolvemos la cadena JSON para mostrarla en logs
        return objectMapper.writeValueAsString(user);
    }
}