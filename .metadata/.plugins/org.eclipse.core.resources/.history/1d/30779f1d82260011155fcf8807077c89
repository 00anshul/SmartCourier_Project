package com.delivery_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String DELIVERY_STATUS_QUEUE    = "delivery.status.queue";
    public static final String DELIVERY_STATUS_EXCHANGE = "delivery.status.exchange";
    public static final String DELIVERY_STATUS_KEY      = "delivery.status.changed";

    @Bean
    Queue deliveryStatusQueue() {
        return new Queue(DELIVERY_STATUS_QUEUE, true);
    }

    @Bean
    TopicExchange deliveryStatusExchange() {
        return new TopicExchange(DELIVERY_STATUS_EXCHANGE);
    }

    @Bean
    Binding deliveryStatusBinding() {
        return BindingBuilder
                .bind(deliveryStatusQueue())
                .to(deliveryStatusExchange())
                .with(DELIVERY_STATUS_KEY);
    }

    @Bean
    JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}