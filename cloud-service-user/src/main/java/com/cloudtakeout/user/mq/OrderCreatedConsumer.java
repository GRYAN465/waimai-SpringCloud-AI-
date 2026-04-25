package com.cloudtakeout.user.mq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedConsumer {

    @RabbitListener(queues = "takeout.order.created.queue")
    public void onOrderCreated(String body) {
        System.out.println("[user-service] receive order created event: " + body);
    }
}
