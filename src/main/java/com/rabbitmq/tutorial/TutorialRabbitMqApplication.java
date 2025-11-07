package com.rabbitmq.tutorial;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.rabbitmq.tutorial.runner.TutorialRabbitMqRunner;

/**
 * Clase principal de arranque de la aplicación.
 *
 * Habilita scheduling para el emisor y define beans de ayuda que muestran
 * un mensaje de uso o ejecutan el runner del tutorial. Los componentes
 * que interactúan con RabbitMQ están controlados por los perfiles:
 * - 'sender' para el emisor
 * - 'receiver' para el receptor
 */
@SpringBootApplication
@EnableScheduling
public class TutorialRabbitMqApplication {

    /**
     * Bean que muestra instrucciones cuando el perfil por defecto 'usage_message' está activo.
     */
    @Profile("usage_message")
    @Bean
    CommandLineRunner usage() {
        return args -> {
            System.out.println("-- TUTORIAL_RABBITMQ_1 AUX APP --");
            System.out.println("Para ejecutar la app, use uno de los siguientes perfiles:");
            System.out.println("  'sender'   : para ejecutar el componente que envía mensajes.");
            System.out.println("  'receiver' : para ejecutar el componente que recibe mensajes.");
            System.out.println("Puede combinar ambos perfiles para ejecutar un cliente completo.\n");
            System.out.println("Ejecuciones de ejemplo:");
            System.out.println("java -jar tutorial-rabbitmq.jar --spring.profiles.active=sender,receiver");
            System.out.println("java -jar tutorial-rabbitmq.jar --spring.profiles.active=sender");
            System.out.println("java -jar tutorial-rabbitmq.jar --spring.profiles.active=receiver");
        };
    }

    /**
     * Bean que arranca el runner del tutorial cuando no está activo el perfil
     * "usage_message". Mantiene la aplicación viva durante la duración
     * configurada y después cierra el contexto.
     */
    @Profile("!usage_message")
    @Bean
    CommandLineRunner tutorial() {
        return new TutorialRabbitMqRunner();
    }

    /**
     * Punto de entrada de la aplicación Spring Boot.
     *
     * @param args argumentos de línea de comandos
     * @throws Exception excepciones posibles durante el arranque
     */
    public static void main(String[] args) throws Exception {
        SpringApplication.run(TutorialRabbitMqApplication.class, args);
    }
}