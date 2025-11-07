package com.rabbitmq.tutorial.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.rabbitmq.tutorial.model.Transaccion;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import java.nio.charset.StandardCharsets;

/**
 * Componente receptor de mensajes desde RabbitMQ que trabaja con Transaccion.
 * Ya no introduce un retardo en el cliente: ahora se confía en el retardo broker-side
 * configurado mediante la cola `hello_delay` (TTL + DLX).
 */
@Component
@RabbitListener(queues = "hello")
public class Receiver {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Manejador para payloads de tipo String.
     * Si el contenido String contiene JSON que corresponde a Transaccion, se
     * intenta deserializar y delegar al manejador de Transaccion.
     *
     * @param in payload recibido como String
     */
    @RabbitHandler
    public void receiveString(String in) {
        // Intento de deserializar si el String es JSON que corresponde a Transaccion
        Transaccion maybe = tryParseTransaccion(in);
        if (maybe != null) {
            handleTransaccion(maybe);
            return;
        }

        // Si no es JSON/Transaccion, tratar como texto plano
        System.out.println("[x] Received String: '" + in + "'");
    }

    /**
     * Manejador para payloads ya deserializados a Transaccion por el convertidor.
     *
     * @param transaccion instancia de Transaccion deserializada
     */
    @RabbitHandler
    public void receiveTransaccion(Transaccion transaccion) {
        handleTransaccion(transaccion);
    }

    /**
     * Fallback para mensajes recibidos como raw byte[]. Intenta convertir a UTF-8
     * y parsear JSON a Transaccion; si falla, trata los bytes como texto.
     *
     * @param body payload en bytes
     */
    @RabbitHandler
    public void receiveBytes(byte[] body) {
        if (body == null || body.length == 0) {
            System.out.println("[x] Received empty byte[] message");
            return;
        }
        String payload = new String(body, StandardCharsets.UTF_8).trim();
        // Primero imprimimos info mínima y el payload
        System.out.println("[x] Received byte[] payload as String: '" + payload + "'");
        // Intentamos parsear a Transaccion
        Transaccion maybe = tryParseTransaccion(payload);
        if (maybe != null) {
            handleTransaccion(maybe);
            return;
        }
        // Si no es JSON válido de Transaccion, se queda como texto
        System.out.println("[x] Received byte[] could not be parsed as Transaccion; treating as text: '" + payload + "'");
    }

    /**
     * Manejo centralizado del objeto Transaccion (lógica de negocio mínima de ejemplo).
     *
     * @param t objeto Transaccion ya validado/deserializado
     */
    private void handleTransaccion(Transaccion t) {
        System.out.println("[x] Received Transaccion: id=" + t.getId()
                + ", cantidad=" + t.getCantidad()
                + ", emisor=" + t.getId_cuenta_emisor()
                + ", receptor=" + t.getId_cuenta_receptor());
    }

    /**
     * Intenta parsear un String JSON a Transaccion. Devuelve null si falla.
     *
     * @param payload String que puede representar un JSON de Transaccion
     * @return Transaccion si el parseo es correcto; null en caso contrario
     */
    private Transaccion tryParseTransaccion(String payload) {
        try {
            String trimmed = payload != null ? payload.trim() : null;
            if (trimmed == null || trimmed.isEmpty()) {
                return null;
            }
            return objectMapper.readValue(trimmed, Transaccion.class);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            // No es JSON válido o no corresponde a Transaccion
            return null;
        }
    }
}