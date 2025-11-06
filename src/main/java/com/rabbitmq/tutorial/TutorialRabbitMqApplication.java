package com.rabbitmq.tutorial;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TutorialRabbitMqApplication {

    @Profile("usage_message")
    @Bean
    CommandLineRunner usage() {
        return args -> {
            System.out.println("This app uses Spring Profiles to control its behavior.\n");
            System.out.println("Sample usage: java -jar rabbit-tutorials.jar --spring.profiles.active=hello-world,sender");
            System.out.println("And then: java -jar rabbit-tutorials.jar --spring.profiles.active=hello-world,reciever");
        };
    }

    @Profile("!usage_message")
    @Bean
    CommandLineRunner tutorial() {
        return new TutorialRabbitMqRunner();
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(TutorialRabbitMqApplication.class, args);
    }
}
