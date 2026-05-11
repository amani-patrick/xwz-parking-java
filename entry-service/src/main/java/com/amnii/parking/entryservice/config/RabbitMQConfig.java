package com.amnii.parking.entryservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange
    public static final String ENTRY_EXCHANGE = "xwz.entry.exchange";

    // Queues
    public static final String CAR_ENTERED_QUEUE = "xwz.car.entered";
    public static final String CAR_EXITED_QUEUE  = "xwz.car.exited";

    // Routing keys
    public static final String CAR_ENTERED_KEY = "car.entered";
    public static final String CAR_EXITED_KEY  = "car.exited";

    @Bean
    public TopicExchange entryExchange() {
        return new TopicExchange(ENTRY_EXCHANGE, true, false);
    }

    @Bean
    public Queue carEnteredQueue() {
        return QueueBuilder.durable(CAR_ENTERED_QUEUE).build();
    }

    @Bean
    public Queue carExitedQueue() {
        return QueueBuilder.durable(CAR_EXITED_QUEUE).build();
    }

    @Bean
    public Binding carEnteredBinding(Queue carEnteredQueue, TopicExchange entryExchange) {
        return BindingBuilder.bind(carEnteredQueue).to(entryExchange).with(CAR_ENTERED_KEY);
    }

    @Bean
    public Binding carExitedBinding(Queue carExitedQueue, TopicExchange entryExchange) {
        return BindingBuilder.bind(carExitedQueue).to(entryExchange).with(CAR_EXITED_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
