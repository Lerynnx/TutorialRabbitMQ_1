package com.rabbitmq.tutorial;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import java.nio.charset.StandardCharsets;

@Component
@RabbitListener(queues = "hello")
public class Receiver {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Caso: el convertidor/producer ha enviado un String simple
    @RabbitHandler
    public void receiveString(String in) {
        // Intento de deserializar si el String es JSON que corresponde a Usuario
        Usuario maybeUser = tryParseUsuario(in);
        if (maybeUser != null) {
            handleUsuario(maybeUser);
            return;
        }

        // Si no es JSON/Usuario, tratar como texto plano
        System.out.println("[x] Received String: '" + in + "'");
    }

    // Caso: el message converter ya ha convertido directamente a Usuario
    @RabbitHandler
    public void receiveUsuario(Usuario usuario) {
        handleUsuario(usuario);
    }

    // Fallback: si el mensaje llega como un array de bytes (por ejemplo producer envía raw bytes)
    // intentamos convertirlo a UTF-8 String y luego parsearlo como JSON a Usuario.
    @RabbitHandler
    public void receiveBytes(byte[] body) {
        if (body == null || body.length == 0) {
            System.out.println("[x] Received empty byte[] message");
            return;
        }
        String payload = new String(body, StandardCharsets.UTF_8).trim();
        // Primero imprimimos info mínima y el payload
        System.out.println("[x] Received byte[] payload as String: '" + payload + "'");
        // Intentamos parsear a Usuario
        Usuario maybeUser = tryParseUsuario(payload);
        if (maybeUser != null) {
            handleUsuario(maybeUser);
            return;
        }
        // Si no es JSON válido de Usuario, se queda como texto
        System.out.println("[x] Received byte[] could not be parsed as Usuario; treating as text: '" + payload + "'");
    }

    // Manejo centralizado del objeto Usuario
    private void handleUsuario(Usuario usuario) {
        System.out.println("[x] Received JSON usuario: id=" + usuario.getId()
                + ", nombre=" + usuario.getNombre());
        // lógica adicional...
    }

    // Intento seguro de parsear String JSON a Usuario, devuelve null si falla
    private Usuario tryParseUsuario(String payload) {
        // Si el payload viene como bytes convertidos a String con un Charset distinto,
        // asegúrate de que el producer use UTF-8; aquí asumimos UTF-8/normal.
        try {
            // Opcional: recortar espacios
            String trimmed = payload != null ? payload.trim() : null;
            if (trimmed == null || trimmed.isEmpty()) {
                return null;
            }
            // Intentar parseo
            return objectMapper.readValue(trimmed, Usuario.class);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            // No es JSON válido o no corresponde a Usuario
            return null;
        }
    }
}