package com.energymonitor.recommendation.infrastructure;

import com.energymonitor.recommendation.application.port.out.ConsumptionReaderPort;
import com.energymonitor.recommendation.application.port.out.HomeReaderPort;
import com.energymonitor.recommendation.application.port.out.RecommendationEventPort;
import com.energymonitor.recommendation.application.port.out.RecommendationIdentifierPort;
import com.energymonitor.recommendation.application.port.out.RecommendationPersistencePort;
import com.energymonitor.recommendation.application.usecase.EvaluateRecommendationsService;
import com.energymonitor.recommendation.application.usecase.RecommendationInboxService;
import com.energymonitor.recommendation.domain.service.RecommendationRules;
import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Wires the recommendation use cases, which carry no Spring annotation, so the application layer
 * stays framework-free, and turns on the schedule the hourly evaluation runs on.
 */
@Configuration
@EnableScheduling
public class RecommendationServiceConfiguration {

    @Bean
    RecommendationInboxService recommendationInboxService(RecommendationPersistencePort recommendations,
                                                          HomeReaderPort homes) {
        return new RecommendationInboxService(recommendations, homes);
    }

    @Bean
    EvaluateRecommendationsService evaluateRecommendationsService(
            RecommendationPersistencePort recommendations, ConsumptionReaderPort consumption,
            HomeReaderPort homes, RecommendationEventPort events, RecommendationIdentifierPort identifiers,
            Clock clock,
            @Value("${recommendation.zone:America/Bogota}") String zone,
            @Value("${recommendation.cool-down:P7D}") Duration coolDown) {
        return new EvaluateRecommendationsService(recommendations, consumption, homes, events, identifiers,
                new RecommendationRules(), clock, ZoneId.of(zone), coolDown);
    }
}
