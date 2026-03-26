package com.tracking_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Primary Queue Constants
    public static final String DELIVERY_STATUS_QUEUE    = "delivery.status.queue";
    public static final String DELIVERY_STATUS_EXCHANGE = "delivery.status.exchange";
    public static final String DELIVERY_STATUS_KEY      = "delivery.status.changed";

    // DLQ Constants
    public static final String DELIVERY_STATUS_DLQ      = "delivery.status.dlq";
    public static final String DELIVERY_STATUS_DLX      = "delivery.status.dlx";
    public static final String DELIVERY_STATUS_DLQ_KEY  = "delivery.status.dlq.key";

    // ─── PRIMARY QUEUE SETUP (With DLQ Arguments) ───

    @Bean
    Queue deliveryStatusQueue() {
        return QueueBuilder.durable(DELIVERY_STATUS_QUEUE)
                .withArgument("x-dead-letter-exchange", DELIVERY_STATUS_DLX)
                .withArgument("x-dead-letter-routing-key", DELIVERY_STATUS_DLQ_KEY)
                .build();
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

    // ─── DLQ SETUP ───

    @Bean
    Queue deliveryStatusDlq() {
        return QueueBuilder.durable(DELIVERY_STATUS_DLQ).build();
    }

    @Bean
    DirectExchange deliveryStatusDlx() {
        return new DirectExchange(DELIVERY_STATUS_DLX);
    }

    @Bean
    Binding dlqBinding() {
        return BindingBuilder
                .bind(deliveryStatusDlq())
                .to(deliveryStatusDlx())
                .with(DELIVERY_STATUS_DLQ_KEY);
    }

    // ─── MESSAGE CONVERTERS ───

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