package com.energymonitor.recommendation.adapter.out.messaging;

import com.energymonitor.recommendation.adapter.out.persistence.entity.RecommendationOutboxEntity;
import com.energymonitor.recommendation.adapter.out.persistence.repository.RecommendationOutboxRepository;
import com.energymonitor.recommendation.application.port.out.RecommendationIdentifierPort;
import com.energymonitor.recommendation.infrastructure.RecommendationOutboxProperties;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Writes a recommendation message to the outbox.
 *
 * <p>Called from inside the use case's transaction, so the row is committed or rolled back
 * together with the change it announces. Publishing to the broker is left to
 * {@link RecommendationOutboxRelay}.
 */
@Component
public class RecommendationOutboxWriter {

    private final RecommendationOutboxRepository outbox;
    private final RecommendationIdentifierPort identifiers;
    private final ObjectMapper json;
    private final Clock clock;
    private final boolean discardWritten;

    public RecommendationOutboxWriter(RecommendationOutboxRepository outbox,
                                      RecommendationIdentifierPort identifiers,
                                      ObjectMapper json, Clock clock,
                                      RecommendationOutboxProperties properties) {
        this.outbox = outbox;
        this.identifiers = identifiers;
        this.json = json;
        this.clock = clock;
        this.discardWritten = properties.discardWritten();
    }

    /**
     * @param type       the routing key, such as {@code recommendation.created}
     * @param payload    the body, serialized as JSON
     * @param occurredAt when the change happened
     */
    public void write(String type, Object payload, Instant occurredAt) {
        RecommendationOutboxEntity message = new RecommendationOutboxEntity();
        message.setIdMessage(identifiers.generate());
        message.setMessageType(type);
        message.setPayload(json.writeValueAsString(payload));
        message.setOccurredAt(occurredAt);
        if (discardWritten) {
            message.setDiscardedAt(clock.instant());
        }
        outbox.save(message);
    }
}
