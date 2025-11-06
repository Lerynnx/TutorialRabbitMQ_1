package com.rabbitmq.tutorial;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Runner del tutorial que mantiene la aplicación viva durante un tiempo
 * configurado y luego cierra el contexto. Se usa cuando no está activo el
 * perfil "usage_message" para ejecutar el ejemplo completo.
 */
public class TutorialRabbitMqRunner implements CommandLineRunner {

    /**
     * Duración en milisegundos que la aplicación permanecerá en ejecución
     * antes de cerrarse automáticamente. Se configura desde application.yml
     * (propiedad tutorial.client.duration).
     */
    @Value("${tutorial.client.duration:0}")
    private int duration;

    @Autowired
    private ConfigurableApplicationContext ctx;

    /**
     * Método que se ejecuta al arrancar el contexto. Muestra un mensaje y
     * duerme el hilo principal durante la duración indicada; a continuación
     * cierra el contexto para terminar la aplicación.
     *
     * @param arg0 argumentos de línea de comandos
     * @throws Exception en caso de interrupciones durante el sleep
     */
    @Override
    public void run(String... arg0) throws Exception {
        System.out.println("Ready ... running for " + duration + "ms");
        Thread.sleep(duration);
        ctx.close();
    }
}