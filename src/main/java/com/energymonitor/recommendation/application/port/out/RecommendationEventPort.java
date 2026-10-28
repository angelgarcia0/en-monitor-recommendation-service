package com.energymonitor.recommendation.application.port.out;

import com.energymonitor.recommendation.domain.model.RecommendationCreated;

public interface RecommendationEventPort {

    void publish(RecommendationCreated event);
}
