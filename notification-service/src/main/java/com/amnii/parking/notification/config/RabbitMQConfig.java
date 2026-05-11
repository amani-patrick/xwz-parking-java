package com.amnii.parking.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── Exchanges (must match publisher names) ────────────────────────────────
    public static final String USER_EXCHANGE  = "xwz.user.exchange";
    public static final String ENTRY_EXCHANGE = "xwz.entry.exchange";

    // ── Notification queues ───────────────────────────────────────────────────
    public static final String NOTIF_USER_REGISTERED = "xwz.notif.user.registered";
    public static final String NOTIF_CAR_ENTERED      = "xwz.notif.car.entered";
    public static final String NOTIF_CAR_EXITED       = "xwz.notif.car.exited";

    // ── Routing keys ──────────────────────────────────────────────────────────
    public static final String USER_REGISTERED_KEY = "user.registered";
    public static final String CAR_ENTERED_KEY     = "car.entered";
    public static final String CAR_EXITED_KEY      = "car.exited";

    // Exchanges
    @Bean TopicExchange userExchange()  { return new TopicExchange(USER_EXCHANGE,  true, false); }
    @Bean TopicExchange entryExchange() { return new TopicExchange(ENTRY_EXCHANGE, true, false); }

    // Notification queues (durable)
    @Bean Queue notifUserRegisteredQueue() { return QueueBuilder.durable(NOTIF_USER_REGISTERED).build(); }
    @Bean Queue notifCarEnteredQueue()     { return QueueBuilder.durable(NOTIF_CAR_ENTERED).build(); }
    @Bean Queue notifCarExitedQueue()      { return QueueBuilder.durable(NOTIF_CAR_EXITED).build(); }

    // Bindings
    @Bean
    public Binding notifUserRegisteredBinding(Queue notifUserRegisteredQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(notifUserRegisteredQueue).to(userExchange).with(USER_REGISTERED_KEY);
    }

    @Bean
    public Binding notifCarEnteredBinding(Queue notifCarEnteredQueue, TopicExchange entryExchange) {
        return BindingBuilder.bind(notifCarEnteredQueue).to(entryExchange).with(CAR_ENTERED_KEY);
    }

    @Bean
    public Binding notifCarExitedBinding(Queue notifCarExitedQueue, TopicExchange entryExchange) {
        return BindingBuilder.bind(notifCarExitedQueue).to(entryExchange).with(CAR_EXITED_KEY);
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
