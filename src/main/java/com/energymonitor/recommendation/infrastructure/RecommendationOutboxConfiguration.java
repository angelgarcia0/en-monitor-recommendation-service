package com.energymonitor.recommendation.infrastructure;

import com.energymonitor.recommendation.adapter.out.messaging.RecommendationOutboxPublisher;
import com.energymonitor.recommendation.adapter.out.messaging.RecommendationOutboxRelay;
import com.energymonitor.recommendation.adapter.out.messaging.RabbitRecommendationOutboxPublisher;
import com.energymonitor.recommendation.adapter.out.persistence.repository.RecommendationOutboxRepository;
import java.time.Clock;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * The broker side of the recommendation outbox: the exchange the recommendation messages go to,
 * and the relay.
 *
 * <p>Only the exchange is declared here. The queues belong to the consumers, which declare and
 * bind their own; until one exists for a message type, {@link RabbitRecommendationOutboxPublisher} gets
 * the message back and the relay keeps it pending.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RecommendationOutboxProperties.class)
public class RecommendationOutboxConfiguration {

    @Bean
    public TopicExchange recommendationExchange(RecommendationOutboxProperties properties) {
        return new TopicExchange(properties.exchange(), true, false);
    }

    /**
     * A template of its own: being mandatory, with a returns callback, is the outbox's business
     * and must not change how anything else in this service sends.
     */
    @Bean
    public RecommendationOutboxPublisher recommendationOutboxPublisher(ConnectionFactory connections,
                                                   RecommendationOutboxProperties properties) {
        return new RabbitRecommendationOutboxPublisher(new RabbitTemplate(connections), properties.exchange(),
                properties.confirmTimeout());
    }

    @Bean
    public RecommendationOutboxRelay recommendationOutboxRelay(RecommendationOutboxRepository outbox, RecommendationOutboxPublisher publisher,
                                           RecommendationOutboxProperties properties, Clock clock) {
        return new RecommendationOutboxRelay(outbox, publisher, clock, properties.batchSize());
    }

    /** Runs the relay on a fixed delay, so a slow run never overlaps the next one. */
    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    @ConditionalOnProperty(name = "messaging.recommendation.relay-enabled", havingValue = "true", matchIfMissing = true)
    static class RelaySchedule implements SchedulingConfigurer {

        private final RecommendationOutboxRelay relay;
        private final RecommendationOutboxProperties properties;

        RelaySchedule(RecommendationOutboxRelay relay, RecommendationOutboxProperties properties) {
            this.relay = relay;
            this.properties = properties;
        }

        @Override
        public void configureTasks(ScheduledTaskRegistrar registrar) {
            registrar.addFixedDelayTask(relay::relay, properties.pollInterval());
        }
    }
}
