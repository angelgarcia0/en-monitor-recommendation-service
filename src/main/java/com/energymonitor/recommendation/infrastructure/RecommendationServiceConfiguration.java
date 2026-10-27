package com.energymonitor.recommendation.infrastructure;

import com.energymonitor.recommendation.application.port.out.HomeReaderPort;
import com.energymonitor.recommendation.application.port.out.RecommendationPersistencePort;
import com.energymonitor.recommendation.application.usecase.RecommendationInboxService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the recommendation use cases, which carry no Spring annotation, so the application layer
 * stays framework-free.
 */
@Configuration
public class RecommendationServiceConfiguration {

    @Bean
    RecommendationInboxService recommendationInboxService(RecommendationPersistencePort recommendations,
                                                          HomeReaderPort homes) {
        return new RecommendationInboxService(recommendations, homes);
    }
}
