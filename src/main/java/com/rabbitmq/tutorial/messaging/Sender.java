package com.rabbitmq.tutorial.messaging;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.tutorial.model.Transaccion;

import java.time.LocalDateTime;

/**
 * Componente que envía mensajes a la cola RabbitMQ.
 *
 * Ahora envía objetos Transaccion usando la exchange delayed (plugin) y el header
 * x-delay para especificar el retardo por mensaje. También usa el ObjectMapper
 * compartido (bean) para serializar LocalDateTime correctamente.
 */
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

    // Nombre de la exchange configurada para delayed messages
    private static final String DELAYED_EXCHANGE = "delayed.exchange";
    private static final String HELLO_ROUTING_KEY = "hello";

    /**
     * Tarea periódica que envía mensajes cada segundo (configurado en el método)
     * Envía primero un texto y después un objeto Transaccion serializado a JSON.
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
                "clave-123",
                "corr-1",
                1001,
                2002,
                250.75f,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "CREADA"
        );
        // Mensaje post-processor para establecer header x-delay (en ms) usando la propiedad configurable
        MessagePostProcessor mpp = message -> {
            message.getMessageProperties().setHeader("x-delay", delayMs != null ? delayMs : 10000);
            return message;
        };

        // Enviar a la exchange delayed; la binding con routing key 'hello' entregará a la cola 'hello' tras el delay
        this.template.convertAndSend(DELAYED_EXCHANGE, HELLO_ROUTING_KEY, t, mpp);

        // Usar el ObjectMapper compartido para el log (tiene JavaTimeModule registrado)
        return objectMapper.writeValueAsString(t);
    }
}