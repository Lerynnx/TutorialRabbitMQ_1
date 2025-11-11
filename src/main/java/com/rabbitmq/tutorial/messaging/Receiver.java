package com.rabbitmq.tutorial.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.rabbitmq.tutorial.model.Transaccion;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import java.nio.charset.StandardCharsets;

/**
 * Receptor de mensajes de RabbitMQ (activo solo con el perfil 'receiver').
 *
 * Procesa mensajes de la cola "hello". Acepta payloads como JSON (string/bytes)
 * o como instancias ya deserializadas de Transaccion.
 */
@Profile("receiver")
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
        Transaccion maybe = tryParseTransaccion(in);
        if (maybe != null) {
            handleTransaccion(maybe);
            return;
        }
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
        System.out.println("[x] Received byte[] payload as String: '" + payload + "'");
        Transaccion maybe = tryParseTransaccion(payload);
        if (maybe != null) {
            handleTransaccion(maybe);
            return;
        }
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
                + ", emisor=" + t.getCuenta_emisor_id()
                + ", receptor=" + t.getCuenta_receptor_id());
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