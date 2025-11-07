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
 * Contiene el método main y algunos beans de ayuda para mostrar mensajes
 * de uso y ejecutar el runner del tutorial. Se habilita el soporte de
 * scheduling para permitir que el componente Sender envíe mensajes
 * periódicamente mediante @Scheduled.
 */
@SpringBootApplication
@EnableScheduling
public class TutorialRabbitMqApplication {

    /**
     * Bean que se activa únicamente si no se incluyen los argumentos necesarios
     */
    @Profile("usage_message")
    @Bean
    CommandLineRunner usage() {
        return args -> {
            System.out.println("Forma de ejecutar la aplicación:");
            System.out.println("java -jar rabbit-tutorials.jar --spring.profiles.active=hello-world,sender");
        };
    }

    /**
     * Bean que arranca el runner del tutorial cuando no está activo el perfil
     * "usage_message". El runner mantiene la aplicación vivo durante el
     * tiempo configurado en properties y después cierra el contexto.
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