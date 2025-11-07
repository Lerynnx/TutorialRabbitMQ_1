package com.rabbitmq.tutorial.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.tutorial.model.Usuario;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import java.nio.charset.StandardCharsets;

/**
 * Componente receptor de mensajes desde RabbitMQ.
 *
 * Esta clase usa @RabbitListener para suscribirse a la cola "hello" y
 * varios métodos anotados con @RabbitHandler para delegar según el tipo
 * de payload recibido:
 * - String: cuando el mensaje se entrega como texto
 * - Usuario: cuando el MessageConverter ya ha deserializado a la clase
 * - byte[]: fallback para mensajes que lleguen como bytes sin headers
 *
 * El receptor es tolerante: intenta convertir Strings JSON a Usuario y
 * en caso de recibir bytes intenta decodificarlos como UTF-8 y parsearlos.
 */
@Component
@RabbitListener(queues = "hello")
public class Receiver {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Manejador para payloads de tipo String.
     * Si el contenido String contiene JSON que corresponde a Usuario, se
     * intenta deserializar y delegar al manejador de Usuario.
     *
     * @param in payload recibido como String
     */
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

    /**
     * Manejador para payloads ya deserializados a Usuario por el convertidor.
     *
     * @param usuario instancia de Usuario deserializada
     */
    @RabbitHandler
    public void receiveUsuario(Usuario usuario) {
        handleUsuario(usuario);
    }

    /**
     * Fallback para mensajes recibidos como raw byte[]. Intenta convertir a UTF-8
     * y parsear JSON a Usuario; si falla, trata los bytes como texto.
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
        // Intentamos parsear a Usuario
        Usuario maybeUser = tryParseUsuario(payload);
        if (maybeUser != null) {
            handleUsuario(maybeUser);
            return;
        }
        // Si no es JSON válido de Usuario, se queda como texto
        System.out.println("[x] Received byte[] could not be parsed as Usuario; treating as text: '" + payload + "'");
    }

    /**
     * Manejo centralizado del objeto Usuario (lógica de negocio mínima de ejemplo).
     *
     * @param usuario objeto Usuario ya validado/deserializado
     */
    private void handleUsuario(Usuario usuario) {
        System.out.println("[x] Received JSON usuario: id=" + usuario.getId()
                + ", nombre=" + usuario.getNombre());
    }

    /**
     * Intenta parsear un String JSON a Usuario. Devuelve null si falla.
     *
     * @param payload String que puede representar un JSON de Usuario
     * @return Usuario si el parseo es correcto; null en caso contrario
     */
    private Usuario tryParseUsuario(String payload) {
        try {
            String trimmed = payload != null ? payload.trim() : null;
            if (trimmed == null || trimmed.isEmpty()) {
                return null;
            }
            return objectMapper.readValue(trimmed, Usuario.class);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            // No es JSON válido o no corresponde a Usuario
            return null;
        }
    }
}