package com.cloudtakeout.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange("takeout.order.exchange");
    }

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue("takeout.order.created.queue");
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(orderCreatedQueue).to(orderExchange).with("takeout.order.created");
    }
}
