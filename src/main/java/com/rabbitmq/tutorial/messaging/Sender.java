package com.rabbitmq.tutorial.messaging;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.tutorial.model.Transaccion;

import java.time.LocalDateTime;

/**
 * Emisor de mensajes (activo solo con el perfil 'sender').
 *
 * Envía periódicamente objetos Transaccion a la exchange "delayed.exchange"
 * estableciendo el header "x-delay" para controlar el retardo por mensaje.
 */
@Profile("sender")
@Component
public class Sender {

    @Autowired
    private RabbitTemplate template;

    @Autowired
    @Qualifier("helloQueue")
    private Queue queue;

    @Autowired
    private ObjectMapper objectMapper; // bean configurado en RabbitConfig con JavaTimeModule

    // Retardo configurable desde application.yml (tutorial.client.delayMs)
    @Value("${tutorial.client.delayMs:10000}")
    private Integer delayMs;

    private static final String DELAYED_EXCHANGE = "delayed.exchange";
    private static final String HELLO_ROUTING_KEY = "hello";

    /**
     * Tarea periódica que envía un objeto Transaccion cada segundo.
     */
    @Scheduled(fixedDelay = 1000, initialDelay = 500)
    public void send() {
    	
        //Enviar un mensaje de texto simple a la cola final
        //String message = sendStringMessage();
        //System.out.println(" [x] Sent '" + message + "' to '" + queue.getName() + "'");

        // Enviar un objeto Transaccion a la exchange delayed con header x-delay
        String jsonMessage;
        try {
            jsonMessage = sendTransaccionMessage();
            System.out.println(" [x] Sent Transaccion JSON '" + jsonMessage + "' to exchange '" + DELAYED_EXCHANGE + "' (delayed, " + delayMs + "ms)");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
//
//    /**
//     * Envía un mensaje String simple a la cola.
//     *
//     * @return el texto enviado (para logging)
//     */
//    private String sendStringMessage() {
//        String message = "Hello World!";
//        this.template.convertAndSend(queue.getName(), message);
//        return message;
//    }

    /**
     * Envía un objeto Transaccion. RabbitTemplate utiliza el MessageConverter
     * para serializar el objeto a JSON y establecer el header content-type.
     *
     * @return la representación JSON de la Transaccion (para logging)
     * @throws JsonProcessingException si falla la serialización manual usada para el log
     */
    private String sendTransaccionMessage() throws JsonProcessingException {
        Transaccion t = new Transaccion(
                1,
                1001,
                2002,
                250.75f,
                LocalDateTime.now(),
                LocalDateTime.now(),
                (short) 1
        );
        // Mensaje post-processor para establecer header x-delay (en ms) usando la propiedad configurable
        MessagePostProcessor mpp = message -> {
            message.getMessageProperties().setHeader("x-delay", delayMs != null ? delayMs : 10000);
            return message;
        };

        this.template.convertAndSend(DELAYED_EXCHANGE, HELLO_ROUTING_KEY, t, mpp);

        return objectMapper.writeValueAsString(t);
    }
}