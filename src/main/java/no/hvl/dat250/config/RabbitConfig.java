package no.hvl.dat250.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_NAME = "polls.exchange";
    public static final String SERVER_QUEUE = "pollApp.queue";

    @Bean
    public TopicExchange pollExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue serverQueue() {
        return QueueBuilder.durable(SERVER_QUEUE).build();
    }

    @Bean
    public Binding binding(Queue serverQueue, TopicExchange pollExchange) {
        return BindingBuilder.bind(serverQueue).to(pollExchange).with("poll.*");
    }

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}