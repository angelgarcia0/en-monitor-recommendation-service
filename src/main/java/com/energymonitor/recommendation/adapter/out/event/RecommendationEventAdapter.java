package com.energymonitor.recommendation.adapter.out.event;

import com.energymonitor.recommendation.adapter.out.messaging.RecommendationOutboxWriter;
import com.energymonitor.recommendation.application.port.out.RecommendationEventPort;
import com.energymonitor.recommendation.domain.model.RecommendationCreated;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Announces a new recommendation as {@code recommendation.created} on the topic exchange
 * {@code messaging.recommendation.exchange}, through the outbox, for the notification service,
 * which delivers it to the members of the home.
 *
 * <p>The body of {@code recommendation.created} is the contract with the notification service,
 * unchanged from the one energy-monitor-backend published: {@code {idRecommendation, homeId,
 * deviceId, type, messageKey, dateTime}}.
 */
@Component
public class RecommendationEventAdapter implements RecommendationEventPort {

    static final String RECOMMENDATION_CREATED = "recommendation.created";

    private final RecommendationOutboxWriter outbox;
    private final Clock clock;

    public RecommendationEventAdapter(RecommendationOutboxWriter outbox, Clock clock) {
        this.outbox = outbox;
        this.clock = clock;
    }

    @Override
    public void publish(RecommendationCreated event) {
        outbox.write(RECOMMENDATION_CREATED, new RecommendationCreatedMessage(
                event.idRecommendation(), event.homeId(), event.deviceId(), event.type().name(),
                event.messageKey(), event.dateTime()), clock.instant());
    }

    /**
     * Body of {@code recommendation.created}.
     *
     * @param deviceId null when the recommendation is about the whole home
     */
    record RecommendationCreatedMessage(String idRecommendation, String homeId, String deviceId,
                                        String type, String messageKey, Instant dateTime) {
    }
}
