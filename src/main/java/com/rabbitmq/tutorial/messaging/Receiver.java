package com.rabbitmq.tutorial.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.tutorial.model.Usuario;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import java.nio.charset.StandardCharsets;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.context.annotation.Profile;
import org.springframework.amqp.core.Message;
import java.util.Map;

/**
 * Componente receptor de mensajes desde RabbitMQ.
 *
 * Contiene la lógica de manejo de mensajes (String, Usuario, byte[]).
 * La parte de recepción se implementa en dos componentes internos:
 * - AutoListener: registra un @RabbitListener cuando NO está activo el
 *   perfil "manual-receiver" (comportamiento por defecto).
 * - ManualPoller: realiza polling con timeout cuando está activo el
 *   perfil "manual-receiver" para que puedas observar cómo se van
 *   acumulando mensajes en la cola.
 */
@Component
public class Receiver {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Inyectado para el polling manual
    @Autowired
    private RabbitTemplate rabbitTemplate;

    // Nombre de la cola a leer; por defecto "hello" y se puede sobrescribir con propiedad
    @Value("${tutorial.client.queue:hello}")
    private String queueName;

    // Timeout en milisegundos para receiveAndConvert. Si 0 -> llamada instantánea.
    @Value("${tutorial.client.receiveTimeout:2000}")
    private long receiveTimeout;

    /**
     * Método que realiza un intento de recepción (poll) usando RabbitTemplate
     * y el timeout configurado. Está pensado para ser llamado por el
     * componente `ManualPoller` cuando el perfil manual-receiver esté activo.
     */
    public void pollOnceManual() {
        Object msg;
        if (receiveTimeout > 0) {
            msg = rabbitTemplate.receiveAndConvert(queueName, receiveTimeout);
        } else {
            msg = rabbitTemplate.receiveAndConvert(queueName);
        }

        if (msg == null) {
            System.out.println("[manual] No message received (timed out after " + receiveTimeout + "ms) - queue may be empty or messages pending delivery.");
        } else {
            processIncoming(msg);
        }
    }

    /**
     * Manejador para payloads de tipo String.
     */
    public void receiveString(String in) {
        Usuario maybeUser = tryParseUsuario(in);
        if (maybeUser != null) {
            handleUsuario(maybeUser);
            return;
        }
        System.out.println("[x] Received String: '" + in + "'");
    }

    /**
     * Manejador para payloads ya deserializados a Usuario por el convertidor.
     */
    public void receiveUsuario(Usuario usuario) {
        handleUsuario(usuario);
    }

    /**
     * Fallback para mensajes recibidos como raw byte[].
     */
    public void receiveBytes(byte[] body) {
        if (body == null || body.length == 0) {
            System.out.println("[x] Received empty byte[] message");
            return;
        }
        String payload = new String(body, StandardCharsets.UTF_8).trim();
        System.out.println("[x] Received byte[] payload as String: '" + payload + "'");
        Usuario maybeUser = tryParseUsuario(payload);
        if (maybeUser != null) {
            handleUsuario(maybeUser);
            return;
        }
        System.out.println("[x] Received byte[] could not be parsed as Usuario; treating as text: '" + payload + "'");
    }

    private void handleUsuario(Usuario usuario) {
        System.out.println("[x] Received JSON usuario: id=" + usuario.getId()
                + ", nombre=" + usuario.getNombre());
    }

    private Usuario tryParseUsuario(String payload) {
        try {
            String trimmed = payload != null ? payload.trim() : null;
            if (trimmed == null || trimmed.isEmpty()) {
                return null;
            }
            return objectMapper.readValue(trimmed, Usuario.class);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            return null;
        }
    }

    private void processIncoming(Object msg) {
        if (msg instanceof String) {
            receiveString((String) msg);
        } else if (msg instanceof Usuario) {
            receiveUsuario((Usuario) msg);
        } else if (msg instanceof byte[]) {
            receiveBytes((byte[]) msg);
        } else if (msg instanceof Message) {
            Message rawMessage = (Message) msg;
            System.out.println("[x] Received raw AMQP Message instance: class=" + rawMessage.getClass().getName());
            // Intentar convertir el cuerpo del mensaje usando el convertidor de RabbitTemplate
            try {
                Object converted = rabbitTemplate.getMessageConverter().fromMessage(rawMessage);
                System.out.println("[x] Converted message content-type: " + rawMessage.getMessageProperties().getContentType());
                // Evitar bucles infinitos: si el convertidor devuelve el mismo objeto, imprimir y terminar
                if (converted == null || converted == rawMessage) {
                    System.out.println("[x] Message could not be converted by MessageConverter; raw body as bytes length=" + (rawMessage.getBody() != null ? rawMessage.getBody().length : 0));
                } else {
                    processIncoming(converted);
                }
            } catch (Exception e) {
                System.out.println("[x] Error converting raw AMQP message: " + e.getMessage());
            }
        } else {
            // Detectar Map (p. ej. LinkedHashMap cuando Jackson convierte JSON a map)
            if (msg instanceof Map) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String,Object> map = (Map<String,Object>) msg;
                    Usuario u = objectMapper.convertValue(map, Usuario.class);
                    if (u != null && (u.getId() != null || (u.getNombre() != null && !u.getNombre().isEmpty()))) {
                        handleUsuario(u);
                        return;
                    }
                } catch (Exception e) {
                    // ignore and continue to generic logging
                }
            }

            // Detectar org.springframework.messaging.Message (payload wrapper)
            if (msg instanceof org.springframework.messaging.Message) {
                try {
                    Object payload = ((org.springframework.messaging.Message<?>) msg).getPayload();
                    processIncoming(payload);
                    return;
                } catch (Exception e) {
                    // continue to generic handling
                }
            }

            String cls = msg != null ? msg.getClass().getName() : "null";
            System.out.println("[manual] Received (unknown type) from '" + queueName + "': class=" + cls + ", toString=" + msg);
         }
     }

    // -----------------------------------------
    // Inner components to control how messages are received
    // -----------------------------------------

    /**
     * Componente que actúa como listener automático. Se registra con
     * @RabbitListener solo cuando NO está activo el perfil 'manual-receiver'.
     */
    @Component
    @Profile("!manual-receiver")
    public static class AutoListener {
        private final Receiver receiver;

        @Autowired
        public AutoListener(Receiver receiver) {
            this.receiver = receiver;
        }

        @RabbitListener(queues = "${tutorial.client.queue:hello}")
        public void onMessage(Object msg) {
            receiver.processIncoming(msg);
        }
    }

    /**
     * Componente que realiza polling manual. Está activo solo cuando el
     * perfil 'manual-receiver' está habilitado.
     */
    @Component
    @Profile("manual-receiver")
    public static class ManualPoller {
        private final Receiver receiver;

        @Autowired
        public ManualPoller(Receiver receiver) {
            this.receiver = receiver;
        }

        @Scheduled(fixedDelayString = "${tutorial.client.pollIntervalMs:1000}", initialDelayString = "${tutorial.client.initialDelayMs:500}")
        public void poll() {
            receiver.pollOnceManual();
        }
    }
}