package com.epam.edp.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the same exchange/queue/binding as sprint1 so the report-app
 * can consume events published by the main backend.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange:travel-agency.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue:booking.events.queue}")
    private String queue;

    @Value("${app.rabbitmq.routing-key:booking.event}")
    private String routingKey;

    @Bean
    public TopicExchange bookingExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue bookingEventsQueue() {
        return new Queue(queue, true);
    }

    @Bean
    public Binding bookingEventsBinding(Queue bookingEventsQueue, TopicExchange bookingExchange) {
        return BindingBuilder.bind(bookingEventsQueue).to(bookingExchange).with(routingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                          Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}

