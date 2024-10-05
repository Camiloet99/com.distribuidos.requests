package com.distribuidos.requests.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "requests-exchange";
    public static final String QUEUE_NAME = "requests-queue";
    public static final String ROUTING_KEY = "requests-routing-key";

    // Configurar la cola
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME, true); // Queue duradera
    }

    // Configurar el exchange
    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    // Configurar el binding entre el exchange y la cola con la clave de enrutamiento
    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    // Configurar el RabbitTemplate, que será el objeto que enviará los mensajes
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }
}