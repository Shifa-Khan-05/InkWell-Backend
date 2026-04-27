package com.postservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String QUEUE = "post_notification_queue";
    public static final String EXCHANGE = "post_exchange";
    public static final String ROUTING_KEY = "post_routing_key";

    @Bean
    public Queue queue() { return new Queue(QUEUE); }

    @Bean
    public TopicExchange exchange() { return new TopicExchange(EXCHANGE); }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter converter() { return new Jackson2JsonMessageConverter(); }
}